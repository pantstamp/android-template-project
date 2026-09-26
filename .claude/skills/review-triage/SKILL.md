---
name: review-triage
description: >
  Walks through a pull request's inline review comments one at a time, showing the code, the
  suggestion and an assessment, and waits for the user's decision on each; then applies the
  accepted fixes, verifies, commits, pushes, replies on each thread, and checks for comments
  newer than the last commit before merge. Use when the user wants to triage, go through or
  address PR review comments or findings.
---

# Review Triage

Not every review finding deserves a fix, and some are wrong. The user decides each one; this
skill makes each decision informed and makes sure none is missed.

## Inputs

- **PR number** — if not given, use the PR for the current branch (`gh pr view --json number`).

## Workflow

### 1. Fetch

```bash
gh api repos/{owner}/{repo}/pulls/{n}/comments --paginate \
  --jq '.[] | {id, user: .user.login, path, line, created_at, in_reply_to_id, body}'
gh api repos/{owner}/{repo}/issues/{n}/comments --jq '.[] | {user: .user.login, body}'
```

Top-level comments (`in_reply_to_id == null`) are findings; replies are discussion. Tell the
user how many findings there are and their severities.

### 2. One finding per message

Order them so a finding another depends on comes first (e.g. a state change before the preview
that uses it); otherwise most severe first. For each:

- **Location** as a clickable `path:line`, and the relevant code
- **The claim**, in plain words
- **Your assessment** — verify the claim against the code before agreeing. Say *valid*,
  *partly valid* or *wrong*, and why. Check whether the suggested fix would break something
  the reviewer didn't see.
- **Proposed change** — the concrete code or test
- Ask: accept, reject, or change?

Wait for the decision. Apply an accepted change before presenting the next finding; for a new
test, confirm it fails when the behaviour it guards is broken.

### 3. Verify, commit, push

After the last finding, run the CI mirror from the pre-pr-checklist skill (Part 2), then:

```bash
git add {files changed for the accepted findings}
git commit -m "{type}({feature-name}): address review feedback on PR #{n}"
git push
```

Only with the user's go-ahead: reply on each thread with what changed and the commit hash
(`gh api -X POST repos/{owner}/{repo}/pulls/{n}/comments/{id}/replies -f body=…`). For a
rejected finding, reply with the reason. Leave resolving threads to the user.

### 4. Before merge — late comments

A second review round can land after the fix commit, and it is easy to merge past. Before the
user merges:

```bash
gh api repos/{owner}/{repo}/pulls/{n}/comments \
  --jq '.[] | select(.created_at > "{last commit time}") | {path, line, body}'
```

Anything returned is unaddressed by definition — triage it the same way.
