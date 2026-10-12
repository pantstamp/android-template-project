#!/usr/bin/env python3
"""Golden-scenario runner for the AI pipeline (docs/AI_PIPELINE_ROADMAP.md §4).

Each scenario replays a historical snapshot with the current pipeline files and grades the
result against criteria whose outcome is already known. See docs/ai-pipeline/README.md.

  eval.py list
  eval.py run <scenario> [--runs N] [--variant V] [--keep] [--judge-model M] [--user-model M]
  eval.py setup <scenario> <dir>         # worktree + rendered prompt, no model call
  eval.py grade <scenario> <output.md> [--variant V] [--transcript FILE]

Needs a `claude` CLI that is logged in (`claude -p "hi"` must answer).
"""

import argparse
import datetime
import json
import re
import shutil
import subprocess
import sys
import tempfile
import time
import uuid
from pathlib import Path

HERE = Path(__file__).resolve().parent
SCENARIOS = HERE / "scenarios"
REPO = Path(
    subprocess.run(
        ["git", "-C", str(HERE), "rev-parse", "--show-toplevel"],
        check=True, capture_output=True, text=True,
    ).stdout.strip()
)
CRITERION = re.compile(r"^- \[([A-Z0-9]+)\] \((required|bonus)(?:, ([a-z0-9-]+))?\)")
REQUIRED_FILES = ("scenario.json", "prompt.md", "criteria.md")
EVAL_GIT = ["-c", "user.name=ai-pipeline-eval", "-c", "user.email=eval@localhost"]
# Pinned so results compare across machines and CLI versions: the CLI's own default model
# changes, and the first CLI run silently used an older one than the recorded baselines.
DEFAULT_MODEL = "claude-opus-5-5"
DEFAULT_USER_MODEL = "claude-sonnet-5-5"
# Errors no retry will fix: stop the whole invocation at the first one.
FATAL_ERRORS = {
    "Failed to authenticate": "the claude CLI is not logged in. Run `claude auth login`, "
                              "check that `claude -p \"hi\"` answers",
    "does not support this model": "the claude CLI is too old for the pinned model. Run "
                                   "`claude update` (or pass an older --model)",
}


class ScenarioError(Exception):
    pass


# ── scenario files ────────────────────────────────────────────────────────────────────────


def load(name):
    folder = SCENARIOS / name
    missing = [f for f in REQUIRED_FILES if not (folder / f).is_file()]
    if missing:
        raise ScenarioError(f"{name}: missing {', '.join(missing)} in {folder}")
    config = json.loads((folder / "scenario.json").read_text())
    for key in ("snapshot", "mode", "runs"):
        if key not in config:
            raise ScenarioError(f"{name}: scenario.json has no '{key}'")
    if config["mode"] not in ("outside", "inside"):
        raise ScenarioError(f"{name}: mode must be 'outside' or 'inside'")
    if config.get("answers"):
        answers = folder / config["answers"]
        if not answers.is_file():
            raise ScenarioError(f"{name}: answers file {answers} is missing")
        config["answers_text"] = answers.read_text()
    config["name"] = name
    config["folder"] = folder
    config["prompt"] = (folder / "prompt.md").read_text()
    config["criteria_text"] = (folder / "criteria.md").read_text()
    config["criteria"] = parse_criteria(config["criteria_text"], name)
    return config


def parse_criteria(text, name):
    criteria = []
    for line in text.splitlines():
        match = CRITERION.match(line)
        if match:
            criteria.append({"id": match[1], "level": match[2], "variant": match[3]})
    if not criteria:
        raise ScenarioError(f"{name}: criteria.md has no '- [ID] (level) …' lines")
    ids = [c["id"] for c in criteria]
    if len(ids) != len(set(ids)):
        raise ScenarioError(f"{name}: duplicate criterion ids in criteria.md")
    return criteria


def variant_of(config, requested):
    variants = config.get("variants", {"default": ""})
    variant = requested or config.get("default_variant") or next(iter(variants))
    if variant not in variants:
        raise ScenarioError(f"{config['name']}: unknown variant '{variant}' ({', '.join(variants)})")
    return variant, variants[variant]


def graded(config, variant):
    return [c for c in config["criteria"] if c["variant"] in (None, variant)]


# ── workspace ─────────────────────────────────────────────────────────────────────────────


def git(*args, cwd=REPO):
    return subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True, text=True).stdout


def setup(config, run_dir, extra):
    """Creates the snapshot worktree. Returns a dict with worktree, branch, cwd, scratch,
    start (the commit the run begins from) and prompt."""
    run_dir.mkdir(parents=True, exist_ok=True)
    worktree = run_dir / "worktree"
    scratch = run_dir / "scratch"
    scratch.mkdir(exist_ok=True)
    branch = None
    if config.get("branch"):
        # Branch names are shared by every worktree of the repo, so each run gets its own.
        tag = re.sub(r"[^A-Za-z0-9-]", "-", f"{run_dir.parent.name}-{run_dir.name}")
        branch = f"{config['branch']}-eval-{tag}"
        git("worktree", "add", "-b", branch, str(worktree), config["snapshot"])
    else:
        git("worktree", "add", "--detach", str(worktree), config["snapshot"])
    write_sdk_dir(worktree)
    if config["mode"] == "inside":
        # The skills under test are today's. Committed, so the run starts from a clean tree.
        shutil.copytree(REPO / ".claude" / "skills", worktree / ".claude" / "skills", dirs_exist_ok=True)
        shutil.copytree(REPO / ".claude" / "agents", worktree / ".claude" / "agents", dirs_exist_ok=True)
        shutil.copy2(REPO / "CLAUDE.md", worktree / "CLAUDE.md")
        git("add", "-f", ".claude/skills", ".claude/agents", "CLAUDE.md", cwd=worktree)
    for target, source in config.get("seed_files", {}).items():
        # Inputs the run starts from (a plan to execute), committed like an earlier stage would.
        (worktree / target).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(config["folder"] / source, worktree / target)
        git("add", "-f", target, cwd=worktree)
    for path in config.get("hidden_paths", []):
        # Files in the snapshot that describe the scenario itself; the run must not find them.
        if (worktree / path).exists():
            git("rm", "-r", "-q", path, cwd=worktree)
    if config["mode"] == "inside" or config.get("seed_files") or config.get("hidden_paths"):
        git(*EVAL_GIT, "commit", "-q", "--allow-empty", "-m", "eval: use the current pipeline", cwd=worktree)
    start = git("rev-parse", "HEAD", cwd=worktree).strip()
    prompt = config["prompt"]
    for key, value in {"WORKTREE": worktree, "BASE": config.get("base", ""), "BRANCH": branch or "",
                       "SCRATCH": scratch}.items():
        prompt = prompt.replace("{{" + key + "}}", str(value))
    if extra:
        prompt = f"{prompt.rstrip()}\n\n{extra}\n"
    (run_dir / "prompt.md").write_text(prompt)
    cwd = REPO if config["mode"] == "outside" else worktree
    return {"worktree": worktree, "branch": branch, "cwd": cwd, "scratch": scratch, "start": start,
            "prompt": prompt}


def write_sdk_dir(worktree):
    """Gradle needs sdk.dir. Copy only that line: the root local.properties also holds the API key."""
    source = REPO / "local.properties"
    if not source.is_file():
        return
    lines = [l for l in source.read_text().splitlines() if l.startswith("sdk.dir=")]
    (worktree / "local.properties").write_text("\n".join(lines) + "\n")


def remove(workspace):
    subprocess.run(["git", "worktree", "remove", "--force", str(workspace["worktree"])],
                   cwd=REPO, capture_output=True)
    if workspace.get("branch"):
        subprocess.run(["git", "branch", "-D", workspace["branch"]], cwd=REPO, capture_output=True)


def committed(config, workspace):
    """Whether the run reached its end: the graded file committed, or enough commits made."""
    path = config.get("done_when_committed")
    if path:
        log = git("log", "--format=%H", f"{workspace['start']}..HEAD", "--", path, cwd=workspace["worktree"])
        return bool(log.strip())
    count = config.get("done_when_commits")
    if count:
        log = git("log", "--format=%H", f"{workspace['start']}..HEAD", cwd=workspace["worktree"])
        return len(log.split()) >= count
    return False


# ── running ───────────────────────────────────────────────────────────────────────────────


def claude_turn(config, workspace, message, transcript, session, first):
    """One `claude -p` call. Returns (final text, events, result event)."""
    command = ["claude", "-p", message, "--output-format", "stream-json", "--verbose",
               "--permission-mode", "dontAsk"]
    if session is None:
        command.append("--no-session-persistence")
    elif first:
        command += ["--session-id", session]
    else:
        command += ["--resume", session]
    command += ["--model", config["run_model"]]
    if config.get("agent"):
        command += ["--agent", config["agent"]]
    if config.get("tools"):
        command += ["--tools", config["tools"]]
    if config.get("allowed_tools"):
        command += ["--allowedTools", *config["allowed_tools"]]
    if config["mode"] == "inside":
        command += ["--add-dir", str(workspace["scratch"])]
    else:
        # The run starts in this repo but works in the worktree, which is outside it.
        command += ["--add-dir", str(workspace["worktree"])]
    denied = [f"Read(//{REPO.as_posix().lstrip('/')}/{p})" for p in config.get("forbidden_paths", [])]
    if denied:
        command += ["--disallowedTools", *denied]
    with tempfile.TemporaryFile("w+") as out:
        try:
            subprocess.run(command, cwd=workspace["cwd"], stdout=out, stderr=subprocess.STDOUT,
                           timeout=config.get("timeout_minutes", 20) * 60)
        except subprocess.TimeoutExpired:
            pass
        out.seek(0)
        lines = out.read().splitlines()
    with transcript.open("a") as log:
        log.write("\n".join(lines) + "\n")
    events = [json.loads(l) for l in lines if l.startswith("{")]
    result = next((e for e in reversed(events) if e.get("type") == "result"), None)
    if result is None:
        raise ScenarioError(f"no result event (timed out or crashed) — see {transcript}")
    if result.get("is_error"):
        raise ScenarioError(f"run failed: {result.get('result')}")
    return result.get("result") or "", events, result


SIMULATED_USER = """You play the user in a scripted session with a coding assistant. Your
script is below. Reply with exactly what the user would type next, following the script; when
the script does not cover the assistant's question, use the script's fallback answer. Never
add ideas, designs or hints of your own.

<script>
{answers}
</script>

The conversation so far, oldest first:

<conversation>
{conversation}
</conversation>
"""


def simulated_reply(config, dialogue, model):
    conversation = "\n\n".join(f"[{who}]\n{text}" for who, text in dialogue)
    prompt = SIMULATED_USER.format(answers=config["answers_text"], conversation=conversation)
    schema = {"type": "object", "properties": {"reply": {"type": "string"}}, "required": ["reply"]}
    return structured(prompt, schema, model or DEFAULT_USER_MODEL)["reply"]


def run_dialogue(config, workspace, run_dir, user_model):
    """Runs the scenario to its end. Returns (output to grade, events, metrics)."""
    transcript = run_dir / "transcript.jsonl"
    transcript.write_text("")
    multi_turn = bool(config.get("answers_text"))
    session = str(uuid.uuid4()) if multi_turn else None
    dialogue, events, results = [], [], []
    message = workspace["prompt"]
    started = time.monotonic()
    for turn in range(config.get("max_turns", 1) if multi_turn else 1):
        text, turn_events, result = claude_turn(config, workspace, message, transcript, session, turn == 0)
        events += turn_events
        results.append(result)
        dialogue += [("user", message), ("assistant", text)]
        if not multi_turn or committed(config, workspace):
            break
        message = simulated_reply(config, dialogue, user_model)
    else:
        if multi_turn and not committed(config, workspace):
            dialogue.append(("runner", f"stopped after {config.get('max_turns')} turns"))
    (run_dir / "dialogue.md").write_text("\n\n".join(f"## {who}\n\n{text}" for who, text in dialogue))
    output = render_output(config, workspace, dialogue)
    (run_dir / "output.md").write_text(output)
    init = next((e for e in events if e.get("type") == "system" and e.get("subtype") == "init"), {})
    metrics = {
        "duration_s": round(time.monotonic() - started),
        "cost_usd": sum(r.get("total_cost_usd") or 0 for r in results),
        "exchanges": len(results),
        "agent_turns": sum(r.get("num_turns") or 0 for r in results),
        "output_tokens": sum(r.get("usage", {}).get("output_tokens", 0) for r in results),
        "permission_denials": sum(len(r.get("permission_denials") or []) for r in results),
        "model": init.get("model", config["run_model"]),
    }
    return output, events, metrics


def render_output(config, workspace, dialogue):
    """What the judge reads: the final message, or the dialogue plus the file under test."""
    path = config.get("grade_file")
    if config.get("grade_git"):
        # What the run left in git: its commits with messages, and the change they make.
        tree = workspace["worktree"]
        log = git("log", "--format=%h %s%n%b", f"{workspace['start']}..HEAD", cwd=tree)
        diff = git("diff", workspace["start"], "HEAD", cwd=tree)
        status = git("status", "--short", cwd=tree)
        conversation = "\n\n".join(f"[{who}]\n{text}" for who, text in dialogue)
        return (f"<dialogue>\n{conversation}\n</dialogue>\n\n<git-log>\n{log}</git-log>\n\n"
                f"<git-diff>\n{diff}</git-diff>\n\n<git-status>\n{status}</git-status>\n")
    if not path:
        return dialogue[-1][1]
    try:
        content = git("show", f"HEAD:{path}", cwd=workspace["worktree"])
    except subprocess.CalledProcessError:
        file = workspace["worktree"] / path
        content = file.read_text() if file.is_file() else "(the file does not exist)"
    conversation = "\n\n".join(f"[{who}]\n{text}" for who, text in dialogue)
    return f"<dialogue>\n{conversation}\n</dialogue>\n\n<file path=\"{path}\">\n{content}\n</file>\n"


# ── deterministic checks ──────────────────────────────────────────────────────────────────


def tool_uses(events):
    for event in events:
        if event.get("type") != "assistant":
            continue
        for block in event.get("message", {}).get("content", []):
            if block.get("type") == "tool_use":
                yield block.get("name", ""), block.get("input", {})


def tool_inputs(events):
    """Inputs of the tools that read; what a run writes may name anything."""
    for name, tool_input in tool_uses(events):
        if name not in ("Write", "Edit", "NotebookEdit"):
            yield json.dumps(tool_input)


EVAL_ROOT = Path(tempfile.gettempdir()) / "ai-pipeline-evals"


def leaks(config, events):
    """Forbidden files or refs the run reached for, whichever tool it used. The runner's own
    output directory is removed first: every worktree path contains it, and a pattern such as
    `ai-pipeline` would otherwise match every command the run makes."""
    patterns = config.get("forbidden_paths", []) + config.get("forbidden_patterns", [])
    own = sorted({str(EVAL_ROOT), str(EVAL_ROOT.resolve())}, key=len, reverse=True)
    found = set()
    for tool_input in tool_inputs(events):
        for root in own:
            tool_input = tool_input.replace(root, "")
        found.update(p for p in patterns if p in tool_input)
    return sorted(found)


QUOTED = re.compile(r"'[^']*'|\"(?:\\.|[^\"\\])*\"")
REDIRECT = re.compile(r"(?<![0-9&<])>>?\s*(?!&|/dev/null)[^\s|;&]")
MUTATING = re.compile(r"(^|[;&|]\s*)(rm|mv|cp|tee|touch|mkdir)\s|\bgit\b[^;&|]*\b(add|commit|checkout|stash|"
                      r"reset|restore|clean|rebase|merge|push)(?![-\w])|spotlessApply|\bsed\s+-i")


def writes(events):
    """Tool calls that change files, for scenarios whose agent must stay read-only."""
    found = []
    for name, tool_input in tool_uses(events):
        if name in ("Edit", "Write", "NotebookEdit"):
            found.append(f"{name} {tool_input.get('file_path', '')}")
        elif name == "Bash":
            command = tool_input.get("command", "")
            bare = QUOTED.sub("''", command)
            if REDIRECT.search(bare) or MUTATING.search(bare):
                found.append(command if len(command) < 160 else command[:157] + "...")
    return found


NESTED_GIT = re.compile(r"\bgit\b[^;&|]*\b(commit|checkout|restore|reset|stash|rebase|push|switch|clean)(?![-\w])")


def nested_git(events):
    """History-changing or edit-undoing git commands run by a nested agent (a phase implementer).
    The outer session may commit; an agent it spawns may not."""
    found = []
    for event in events:
        if event.get("type") != "assistant" or not event.get("parent_tool_use_id"):
            continue
        for block in event.get("message", {}).get("content", []):
            if block.get("type") == "tool_use" and block.get("name") == "Bash":
                command = block.get("input", {}).get("command", "")
                if NESTED_GIT.search(QUOTED.sub("''", command)):
                    found.append(command if len(command) < 160 else command[:157] + "...")
    return found


def checks(config, events, workspace):
    found = []
    if config.get("forbidden_paths") or config.get("forbidden_patterns"):
        touched = leaks(config, events)
        found.append({"id": "LEAK", "pass": not touched,
                      "evidence": f"reached for {', '.join(touched)}" if touched else "nothing forbidden"})
    if config.get("nested_no_git"):
        used = nested_git(events)
        found.append({"id": "NESTEDGIT", "pass": not used,
                      "evidence": "; ".join(used) if used else "no nested agent changed git state"})
    if config.get("read_only"):
        changed = writes(events)
        found.append({"id": "READONLY", "pass": not changed,
                      "evidence": "; ".join(changed) if changed else "no file-changing tool call"})
    if (config.get("done_when_committed") or config.get("done_when_commits")) and workspace:
        done = committed(config, workspace)
        target = config.get("done_when_committed") or f"{config['done_when_commits']} commits"
        found.append({"id": "COMMITTED", "pass": done,
                      "evidence": f"{target}: " + ("reached" if done else "not reached")})
    return found


# ── grading ───────────────────────────────────────────────────────────────────────────────

JUDGE = """You grade one run of an automated scenario. Below are the pass criteria, then the
run's output. For each criterion id listed under "Grade these", decide whether the output meets
it. Judge only what the output says; do not reward a criterion for being plausible. Quote the
part of the output that decides it (or say what is missing) as evidence, in at most 2 sentences.

Grade these: {ids}

<criteria>
{criteria}
</criteria>

<output>
{output}
</output>
"""


def structured(prompt, schema, model):
    command = ["claude", "-p", prompt, "--output-format", "json", "--tools", "",
               "--no-session-persistence", "--json-schema", json.dumps(schema)]
    if model:
        command += ["--model", model]
    with tempfile.TemporaryDirectory() as empty:
        reply = json.loads(subprocess.run(command, cwd=empty, check=True, capture_output=True, text=True).stdout)
    if reply.get("is_error"):
        raise ScenarioError(f"model call failed: {reply.get('result')}")
    return reply.get("structured_output") or json.loads(reply["result"])


def judge(config, variant, output, model):
    ids = [c["id"] for c in graded(config, variant)]
    schema = {
        "type": "object",
        "properties": {"grades": {"type": "array", "items": {
            "type": "object",
            "properties": {"id": {"type": "string", "enum": ids}, "pass": {"type": "boolean"},
                           "evidence": {"type": "string"}},
            "required": ["id", "pass", "evidence"],
        }}},
        "required": ["grades"],
    }
    prompt = JUDGE.format(ids=", ".join(ids), criteria=config["criteria_text"], output=output)
    grades = structured(prompt, schema, model)["grades"]
    by_id = {g["id"]: g for g in grades}
    missing = [i for i in ids if i not in by_id]
    if missing:
        raise ScenarioError(f"judge left out {', '.join(missing)}")
    return [by_id[i] for i in ids]


def grade_run(config, variant, output, events, model, workspace=None):
    grades = judge(config, variant, output, model) + checks(config, events, workspace)
    levels = {c["id"]: c["level"] for c in config["criteria"]}
    for g in grades:
        g["level"] = levels.get(g["id"], "required")
    return grades


def run_passed(grades):
    return all(g["pass"] for g in grades if g["level"] == "required")


# ── reporting ─────────────────────────────────────────────────────────────────────────────


def summarise(config, variant, runs):
    total = len(runs)
    passed = sum(1 for r in runs if r.get("grades") and run_passed(r["grades"]))
    verdict = "PASS" if passed == total else "WARN" if passed == total - 1 and total > 1 else "FAIL"
    lines = [f"# {config['name']} — {variant}", "",
             f"**{verdict}**: {passed} of {total} runs passed every required criterion.", "",
             "| Criterion | Level | " + " | ".join(f"Run {i + 1}" for i in range(total)) + " |",
             "|---|---|" + "---|" * total]
    ids = [g["id"] for g in next((r["grades"] for r in runs if r.get("grades")), [])]
    for cid in ids:
        cells = []
        for r in runs:
            g = next((g for g in r.get("grades", []) if g["id"] == cid), None)
            cells.append("—" if g is None else "✅" if g["pass"] else "❌")
        level = next(g["level"] for r in runs for g in r.get("grades", []) if g["id"] == cid)
        lines.append(f"| {cid} | {level} | " + " | ".join(cells) + " |")
    lines += ["", "Exchanges are user messages sent; agent turns are the model's own steps; denials are",
              "tool calls the permission rules refused, which usually means `allowed_tools` is too narrow.", "",
              "| Run | Model | Duration | Exchanges | Agent turns | Output tokens | Cost | Denials |",
              "|---|---|---|---|---|---|---|---|"]
    for i, r in enumerate(runs):
        m = r.get("metrics", {})
        if not m:
            lines.append(f"| {i + 1} | — | — | — | — | — | — | — |")
            continue
        lines.append(f"| {i + 1} | {m['model']} | {m['duration_s']} s | {m['exchanges']} | {m['agent_turns']} | "
                     f"{m['output_tokens']} | ${m['cost_usd'] or 0:.2f} | {m['permission_denials']} |")
    errors = [(i + 1, r["error"]) for i, r in enumerate(runs) if r.get("error")]
    if errors:
        lines += ["", "Errors:"] + [f"- Run {i}: {e}" for i, e in errors]
    failing = [(i + 1, g) for i, r in enumerate(runs) for g in r.get("grades", []) if not g["pass"]]
    if failing:
        lines += ["", "Failures:"] + [f"- Run {i}, {g['id']}: {g['evidence']}" for i, g in failing]
    return verdict, "\n".join(lines) + "\n"


# ── commands ──────────────────────────────────────────────────────────────────────────────


def cmd_list(_):
    for folder in sorted(SCENARIOS.iterdir()):
        if folder.is_dir():
            try:
                config = load(folder.name)
                print(f"{folder.name:28} {config.get('description', '')}")
            except ScenarioError as error:
                print(f"{folder.name:28} (not runnable: {error})")


def cmd_setup(args):
    config = load(args.scenario)
    _, extra = variant_of(config, args.variant)
    workspace = setup(config, Path(args.dir).resolve(), extra)
    for key in ("worktree", "branch", "cwd", "scratch", "start"):
        print(f"{key + ':':10}{workspace[key]}")
    print(f"{'prompt:':10}{Path(args.dir).resolve() / 'prompt.md'}")
    print(f"remove with: git worktree remove --force {workspace['worktree']}"
          + (f" && git branch -D {workspace['branch']}" if workspace["branch"] else ""))


def cmd_grade(args):
    config = load(args.scenario)
    variant, _ = variant_of(config, args.variant)
    events = []
    if args.transcript:
        events = [json.loads(l) for l in Path(args.transcript).read_text().splitlines() if l.startswith("{")]
    grades = grade_run(config, variant, Path(args.output).read_text(), events, args.judge_model or DEFAULT_MODEL)
    verdict, summary = summarise(config, variant, [{"grades": grades}])
    print(summary)
    return 0 if verdict == "PASS" else 1


def cmd_run(args):
    config = load(args.scenario)
    variant, extra = variant_of(config, args.variant)
    config["run_model"] = args.model or config.get("model") or DEFAULT_MODEL
    judge_model = args.judge_model or DEFAULT_MODEL
    user_model = args.user_model or DEFAULT_USER_MODEL
    stamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    out_dir = Path(tempfile.gettempdir()) / "ai-pipeline-evals" / config["name"] / f"{stamp}-{variant}"
    runs = []
    for i in range(args.runs or config["runs"]):
        run_dir = out_dir / f"run-{i + 1}"
        print(f"[{config['name']}] run {i + 1}: {run_dir}", flush=True)
        record = {}
        workspace = None
        try:
            workspace = setup(config, run_dir, extra)
            output, events, record["metrics"] = run_dialogue(config, workspace, run_dir, user_model)
            record["grades"] = grade_run(config, variant, output, events, judge_model, workspace)
        except (ScenarioError, subprocess.CalledProcessError) as error:
            record["error"] = str(error)
        finally:
            if workspace and not args.keep:
                remove(workspace)
        (run_dir / "grade.json").write_text(json.dumps(record, indent=2))
        runs.append(record)
        fatal = next((hint for marker, hint in FATAL_ERRORS.items() if marker in record.get("error", "")), None)
        if fatal:
            print(f"Stopping: {fatal}, then run again.", flush=True)
            break
    verdict, summary = summarise(config, variant, runs)
    (out_dir / "summary.md").write_text(summary)
    print(summary)
    print(f"Results: {out_dir}")
    return 0 if verdict == "PASS" else 1


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("list").set_defaults(func=cmd_list)
    p = sub.add_parser("run")
    p.add_argument("scenario")
    p.add_argument("--runs", type=int)
    p.add_argument("--variant")
    p.add_argument("--keep", action="store_true", help="keep the worktrees")
    p.add_argument("--model", help=f"model for the stage under test (default {DEFAULT_MODEL})")
    p.add_argument("--judge-model", help=f"model grading the output (default {DEFAULT_MODEL})")
    p.add_argument("--user-model", help=f"model playing the user in multi-turn scenarios (default {DEFAULT_USER_MODEL})")
    p.set_defaults(func=cmd_run)
    p = sub.add_parser("setup")
    p.add_argument("scenario")
    p.add_argument("dir")
    p.add_argument("--variant")
    p.set_defaults(func=cmd_setup)
    p = sub.add_parser("grade")
    p.add_argument("scenario")
    p.add_argument("output")
    p.add_argument("--variant")
    p.add_argument("--transcript")
    p.add_argument("--judge-model")
    p.set_defaults(func=cmd_grade)
    args = parser.parse_args()
    try:
        sys.exit(args.func(args) or 0)
    except ScenarioError as error:
        sys.exit(f"error: {error}")


if __name__ == "__main__":
    main()
