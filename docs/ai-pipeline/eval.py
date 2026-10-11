#!/usr/bin/env python3
"""Golden-scenario runner for the AI pipeline (docs/AI_PIPELINE_ROADMAP.md §4).

Each scenario replays a historical snapshot with the current pipeline files and grades the
result against criteria whose outcome is already known. See docs/ai-pipeline/README.md.

  eval.py list
  eval.py run <scenario> [--runs N] [--variant V] [--keep] [--judge-model M]
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
    if "answers" in config:
        # Multi-turn scenarios (architect, developer, product owner) need a simulated user.
        raise ScenarioError(f"{name}: scripted answers are not supported by this runner yet")
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
    """Creates the snapshot worktree and returns (worktree, cwd, prompt)."""
    run_dir.mkdir(parents=True, exist_ok=True)
    worktree = run_dir / "worktree"
    git("worktree", "add", "--detach", str(worktree), config["snapshot"])
    write_sdk_dir(worktree)
    if config["mode"] == "inside":
        # The skills under test are today's: copy them in, uncommitted.
        shutil.copytree(REPO / ".claude" / "skills", worktree / ".claude" / "skills", dirs_exist_ok=True)
        shutil.copytree(REPO / ".claude" / "agents", worktree / ".claude" / "agents", dirs_exist_ok=True)
        shutil.copy2(REPO / "CLAUDE.md", worktree / "CLAUDE.md")
    prompt = config["prompt"].replace("{{WORKTREE}}", str(worktree))
    prompt = prompt.replace("{{BASE}}", config.get("base", ""))
    if extra:
        prompt = f"{prompt.rstrip()}\n\n{extra}\n"
    (run_dir / "prompt.md").write_text(prompt)
    cwd = REPO if config["mode"] == "outside" else worktree
    return worktree, cwd, prompt


def write_sdk_dir(worktree):
    """Gradle needs sdk.dir. Copy only that line: the root local.properties also holds the API key."""
    source = REPO / "local.properties"
    if not source.is_file():
        return
    lines = [l for l in source.read_text().splitlines() if l.startswith("sdk.dir=")]
    (worktree / "local.properties").write_text("\n".join(lines) + "\n")


def remove(worktree):
    subprocess.run(["git", "worktree", "remove", "--force", str(worktree)], cwd=REPO, capture_output=True)


# ── running ───────────────────────────────────────────────────────────────────────────────


def run_claude(config, cwd, prompt, run_dir):
    command = ["claude", "-p", prompt, "--output-format", "stream-json", "--verbose",
               "--permission-mode", "dontAsk", "--no-session-persistence"]
    if config.get("agent"):
        command += ["--agent", config["agent"]]
    if config.get("tools"):
        command += ["--tools", config["tools"]]
    if config.get("allowed_tools"):
        command += ["--allowedTools", *config["allowed_tools"]]
    denied = [f"Read(//{REPO.as_posix().lstrip('/')}/{p})" for p in config.get("forbidden_paths", [])]
    if denied:
        command += ["--disallowedTools", *denied]
    transcript = run_dir / "transcript.jsonl"
    started = time.monotonic()
    with transcript.open("w") as out:
        try:
            subprocess.run(command, cwd=cwd, stdout=out, stderr=subprocess.STDOUT,
                           timeout=config.get("timeout_minutes", 20) * 60)
        except subprocess.TimeoutExpired:
            pass
    events = [json.loads(l) for l in transcript.read_text().splitlines() if l.startswith("{")]
    result = next((e for e in reversed(events) if e.get("type") == "result"), None)
    if result is None:
        raise ScenarioError(f"no result event (timed out or crashed) — see {transcript}")
    if result.get("is_error"):
        raise ScenarioError(f"run failed: {result.get('result')}")
    output = result.get("result") or ""
    (run_dir / "output.md").write_text(output)
    usage = result.get("usage", {})
    metrics = {
        "duration_s": round(time.monotonic() - started),
        "cost_usd": result.get("total_cost_usd"),
        "turns": result.get("num_turns"),
        "tokens": sum(usage.get(k, 0) for k in ("input_tokens", "cache_creation_input_tokens",
                                                "cache_read_input_tokens", "output_tokens")),
        "permission_denials": len(result.get("permission_denials") or []),
    }
    return output, events, metrics


def tool_uses(events):
    for event in events:
        if event.get("type") != "assistant":
            continue
        for block in event.get("message", {}).get("content", []):
            if block.get("type") == "tool_use":
                yield block.get("name", ""), block.get("input", {})


def tool_inputs(events):
    for _, tool_input in tool_uses(events):
        yield json.dumps(tool_input)


def leaks(config, events):
    """Forbidden paths the run touched, whichever tool it used."""
    names = [Path(p).name for p in config.get("forbidden_paths", [])]
    return sorted({n for i in tool_inputs(events) for n in names if n in i})


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
    command = ["claude", "-p", prompt, "--output-format", "json", "--tools", "",
               "--no-session-persistence", "--json-schema", json.dumps(schema)]
    if model:
        command += ["--model", model]
    with tempfile.TemporaryDirectory() as empty:
        reply = json.loads(subprocess.run(command, cwd=empty, check=True, capture_output=True, text=True).stdout)
    if reply.get("is_error"):
        raise ScenarioError(f"judge failed: {reply.get('result')}")
    grades = (reply.get("structured_output") or json.loads(reply["result"]))["grades"]
    by_id = {g["id"]: g for g in grades}
    missing = [i for i in ids if i not in by_id]
    if missing:
        raise ScenarioError(f"judge left out {', '.join(missing)}")
    return [by_id[i] for i in ids]


def grade_run(config, variant, output, events, model):
    grades = judge(config, variant, output, model)
    if config.get("forbidden_paths"):
        touched = leaks(config, events)
        grades.append({"id": "LEAK", "pass": not touched,
                       "evidence": f"touched {', '.join(touched)}" if touched else "no forbidden path touched"})
    if config.get("read_only"):
        changed = writes(events)
        grades.append({"id": "READONLY", "pass": not changed,
                       "evidence": "; ".join(changed) if changed else "no file-changing tool call"})
    levels = {c["id"]: c["level"] for c in config["criteria"]} | {"LEAK": "required", "READONLY": "required"}
    for g in grades:
        g["level"] = levels[g["id"]]
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
    lines += ["", "| Run | Duration | Tokens | Cost | Denials |", "|---|---|---|---|---|"]
    for i, r in enumerate(runs):
        m = r.get("metrics", {})
        if not m:
            lines.append(f"| {i + 1} | — | — | — | — |")
            continue
        lines.append(f"| {i + 1} | {m['duration_s']} s | {m['tokens']} | "
                     f"${m['cost_usd'] or 0:.2f} | {m['permission_denials']} |")
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
    worktree, cwd, _ = setup(config, Path(args.dir).resolve(), extra)
    print(f"worktree: {worktree}\ncwd:      {cwd}\nprompt:   {Path(args.dir).resolve() / 'prompt.md'}")
    print(f"remove with: git worktree remove --force {worktree}")


def cmd_grade(args):
    config = load(args.scenario)
    variant, _ = variant_of(config, args.variant)
    events = []
    if args.transcript:
        events = [json.loads(l) for l in Path(args.transcript).read_text().splitlines() if l.startswith("{")]
    grades = grade_run(config, variant, Path(args.output).read_text(), events, args.judge_model)
    verdict, summary = summarise(config, variant, [{"grades": grades}])
    print(summary)
    return 0 if verdict == "PASS" else 1


def cmd_run(args):
    config = load(args.scenario)
    variant, extra = variant_of(config, args.variant)
    stamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    out_dir = Path(tempfile.gettempdir()) / "ai-pipeline-evals" / config["name"] / f"{stamp}-{variant}"
    runs = []
    for i in range(args.runs or config["runs"]):
        run_dir = out_dir / f"run-{i + 1}"
        print(f"[{config['name']}] run {i + 1}: {run_dir}", flush=True)
        record = {}
        worktree = None
        try:
            worktree, cwd, prompt = setup(config, run_dir, extra)
            output, events, record["metrics"] = run_claude(config, cwd, prompt, run_dir)
            record["grades"] = grade_run(config, variant, output, events, args.judge_model)
        except (ScenarioError, subprocess.CalledProcessError) as error:
            record["error"] = str(error)
        finally:
            if worktree and not args.keep:
                remove(worktree)
        (run_dir / "grade.json").write_text(json.dumps(record, indent=2))
        runs.append(record)
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
    p.add_argument("--judge-model")
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
