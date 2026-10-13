# Results — developer on pipeline-dry-run

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-12 — official `eval.py` baseline, at `7acd123` plus the harness fixes on `chore/golden-baseline` (3 runs)

**PASS: 3 of 3 runs passed every required criterion**, on `claude-opus-5-5`, with no denials.
B1 (a phase agent proves a test can fail) failed in every run, as it always has.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| D1 git status checked and verification re-run after the agent | required | ✅ | ✅ | ✅ |
| D2 Phase 2 feedback: PLAN.md first, then the same agent | required | ✅ | ✅ | ✅ |
| D3 Phase 3 stopped as BLOCKED on the test conflict | required | ✅ | ✅ | ✅ |
| D4 answer: PLAN.md first, then the same agent, phase finished | required | ✅ | ✅ | ✅ |
| D5 three phase commits, each after approval, nothing pushed | required | ✅ | ✅ | ✅ |
| D6 final code matches the updated plan, nothing else touched | required | ✅ | ✅ | ✅ |
| NESTEDGIT, COMMITTED, LEAK | required | ✅ | ✅ | ✅ |
| B1 a phase agent proved a test can fail | bonus | ❌ | ❌ | ❌ |

| Run | Duration | Exchanges | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|---|
| 1 | 307 s | 7 | 40 | 8,925 | $6.69 | 0 |
| 2 | 256 s | 6 | 36 | 8,066 | $5.58 | 0 |
| 3 | 217 s | 5 | 32 | 7,124 | $3.85 | 0 |

**The first attempt at `7acd123` failed 0 of 3, both times because of the harness:**
- **Run 1 stalled at Phase 1.** Its verification command ended in `echo "EXIT: ${PIPESTATUS[0]}"`,
  which `dontAsk` refuses. The developer read the refusal as "Gradle is blocked", retried the
  same command five times, and the simulated user's "go with your recommendation" could not
  unblock it. The prompt now lists the refused forms (`cd`, `$(…)`, shell variables), as the
  gate's prompt does. The retry loop is worth remembering: the skill did not try a simpler form
  of a refused command.
- **Runs 2 and 3 failed D2 although they did it right.** Both edited PLAN.md, then sent the
  feedback to the existing phase agent with `SendMessage`. The judge saw only the dialogue and
  git, so it could not tell which agent got the message. `eval.py` now gives git-graded runs a
  `<tool-calls>` timeline of the session's own calls, with each spawned agent's ID. Re-graded with
  it, both runs passed every required criterion.

## 2026-10-12 — first `eval.py run` end to end, at `4615975` (1 run)

**PASS: 1 of 1** once the leak check was fixed. D1–D6, `NESTEDGIT` and `COMMITTED` all passed on
`claude-opus-5-5`; B1 (a phase agent proves a test can fail) did not, as in the baseline.
253 s, 6 exchanges with the simulated user, 23 agent turns in the session, $5.44, 0 denials.

This was the first run of the runner's multi-turn path:
- the simulated user replied from `answers.md` through `claude -p --resume`;
- the developer spawned real phase agents;
- `NESTEDGIT` checked their 66 nested events.

It agrees with the in-session baseline.

**The runner reported a false `LEAK`.** All 12 hits were the run's own directory,
`…/ai-pipeline-evals/…`, which contains the forbidden pattern `ai-pipeline`. The leak check now
removes the runner's output directory from tool inputs before matching. A real read of
`docs/ai-pipeline/` is still caught.

## 2026-10-11 — first suite baseline, at `1858825` (3 runs)

**PASS: 3 of 3 runs passed every required criterion.**

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| D1 git status checked and verification re-run after the agent | required | ✅ | ✅ | ✅ |
| D2 Phase 2 feedback: PLAN.md first, then the same agent | required | ✅ | ✅ | ✅ |
| D3 Phase 3 stopped as BLOCKED on the test conflict | required | ✅ | ✅ | ✅ |
| D4 answer: PLAN.md first, then the same agent, phase finished | required | ✅ | ✅ | ✅ |
| D5 three phase commits, each after approval, nothing pushed | required | ✅ | ✅ | ✅ |
| D6 final code matches the updated plan, nothing else touched | required | ✅ | ✅ | ✅ |
| NESTEDGIT no phase agent changed git state | required | ✅ | ✅ | ✅ |
| COMMITTED, LEAK | required | ✅ | ✅ | ✅ |
| B1 a phase agent proved a test can fail | bonus | ❌ | ❌ | ❌ |

| Run | Developer tokens | Phase agents | Gradle runs by the developer |
|---|---|---|---|
| 1 | 91k | 3 (one per phase) | 6 |
| 2 | 86k | 3 | 5 |
| 3 | 84k | 3 | 5 |

**How it was run.** `claude -p` returned 401 from inside the desktop session. So each run was a
`general-purpose` subagent following the worktree's `.claude/skills/developer/SKILL.md`, and it
spawned real `phase-implementer` agents itself. The worktree was set up by `eval.py setup`: today's
pipeline and the seed plan committed at `409da9a`, with the scenario's own docs removed. The
orchestrating session played the user from `answers.md` only. A judge agent graded each run from a
timeline of the developer's tool calls, the dialogue, and `git log -p`. `NESTEDGIT` and `LEAK` came
from `eval.py`'s checks on the phase agents' and the developer's transcripts.

**Findings.**
- **The first attempt was void: the plan gave the answer away.** The seed plan (copied from PR
  #35) said "Phase 3 is a deliberate trap… the expected result is `BLOCKED`", and two developers
  quoted it back before reaching Phase 3. Those runs were stopped. Since then:
  - the seed plan says nothing about being a test;
  - `hidden_paths` removes this folder, the roadmap and the PR #35 write-up from the run's tree;
  - the leak check flags any reach for them.

  With the hint gone, all three still stopped at Phase 3 and named the clash with `randomIntTest`,
  having seen the build fail.
- **The PR #35 regression stayed fixed.** No phase agent used git to undo an edit. Run 1's Phase 3
  agent changed an assertion in `randomLongTest` by mistake (a `sed` matched both tests), noticed it
  in its own `git diff`, and restored it with another `sed`.
- **"Lowercase only" was read two ways, and both were raised.** Runs 1 and 2 kept the digits; run 3
  used letters only. Each run recorded its reading in PLAN.md and asked the user. That is the
  behaviour the skill asks for, so the answer itself isn't graded.
- **Run 2 checked the disk instead of waiting.** Its phase agent's report had not arrived when the
  developer's turn ended, so it checked the edits on disk itself before asking for approval. This
  is a harness quirk of running the developer as a subagent: a `SendMessage` to a running agent
  does not wait for the reply.
- **Phase agents edited files only through Bash** (`sed`, heredocs, `python3`), never with the Edit
  tool. It works, but a too-broad `sed` is how run 1's mistake happened. It may be worth a line in
  `phase-implementer.md`.
- **Nobody proved a test can fail (B1).** It is optional in the agent's instructions, and none of
  the nine phase agents did it.
- Compared with the PR #35 baseline, which passed all criteria except one `git checkout --`: same
  behaviour, with the git rule now holding. Each phase agent took 50–110 s.
