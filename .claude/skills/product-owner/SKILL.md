---
name: product-owner
description: >
  Run a structured product owner / business analyst session for a new feature.
  Takes raw feature requirements, interviews the human to uncover gaps and edge cases,
  and produces a SPEC.md with user stories, acceptance criteria, and out-of-scope items.
  Use this skill whenever the user wants to define, scope, or spec out a new feature —
  including phrases like "new feature", "requirements", "write a spec", "BA session",
  "product owner", "user stories", "acceptance criteria", or "let's scope this out".
  Also trigger when the user pastes or references raw feature requirements and wants
  them refined into a structured specification.
---

# Product Owner / Business Analyst

This skill runs a structured requirements interview and produces a feature specification.
It exists because jumping straight to architecture or code without a clear spec leads to
rework, missed edge cases, and scope creep. The interview catches ambiguities early,
when they're cheap to fix.

## Inputs

The user provides:
- **Feature name** — a short identifier (e.g., "watched-movies", "user-profiles")
- **Raw requirements** — free-form text describing what the feature should do

If the user doesn't provide a feature name, infer one from the requirements and confirm it.

## Workflow

### 1. Acknowledge and set context

Read `CLAUDE.md` if it exists — it contains project conventions that inform feasibility
and constraints. Then read any existing specs in `docs/features/` to understand the
project's current feature set and avoid contradictions.

Tell the user you'll interview them one topic at a time, then produce a SPEC.md.

### 2. Analyze requirements for gaps

Before asking questions, silently identify:
- Ambiguous terms or undefined behaviors
- Missing error / unhappy path handling
- Implicit assumptions about platform behavior
- Android-specific concerns: offline behavior, lifecycle handling, permissions,
  deep links, back navigation, accessibility, unhappy paths when there are network or database errors, etc.

### 3. Interview — one topic at a time

Ask clarifying questions **one topic per message**. This keeps the conversation focused
and avoids overwhelming the user with a wall of questions.

For each topic:
- State what's unclear or what assumption you're making
- Propose a default if you have a reasonable one ("I'd suggest X because Y — does that work?")
- Wait for the user's answer before moving to the next topic

Cover these categories (skip any that don't apply to the feature):
- **Core behavior**: What exactly happens? What are the triggers and outcomes?
- **Edge cases**: Empty states, boundary values, concurrent operations
- **Error handling**: Network failures, database errors, invalid input
- **Android lifecycle**: Screen rotation, process death, back navigation
- **Offline behavior**: What works offline? What's cached? Sync strategy?
- **Accessibility**: Content descriptions, touch targets, screen reader support
- **Data concerns**: Storage limits, migration, data loss scenarios
- **UI/UX open questions**: Naming, visual treatment, interaction patterns

### 4. Produce SPEC.md

Once all topics are covered, generate a structured specification and save it to:

```
docs/features/{feature-name}/SPEC.md
```

Create the directory if it doesn't exist.

### SPEC.md structure

```markdown
# Feature: {Feature Name}

## Overview
One paragraph summarizing what this feature does and why.

## User Stories
For each story:
- **As a** [user type], **I want** [action], **so that** [benefit]
- **Acceptance Criteria:**
  - [ ] Criterion 1
  - [ ] Criterion 2

## Edge Cases
Bulleted list of edge cases and how each should be handled.

## Error Handling
What happens when things go wrong — network errors, database failures, invalid state.

## Out of Scope
Explicitly list what this feature does NOT include, to prevent scope creep.

## Open Questions (if any)
Anything that still needs a decision, with the options discussed.
```

### 5. Confirm with the user

After saving SPEC.md, tell the user the file path and ask them to review it.
If they request changes, update the file and confirm again.

## Example

**User says:** "I want to add a watched movies feature. When a user rates a movie,
save it locally and show it in a new tab."

**You do:**
1. Read CLAUDE.md for project conventions
2. Identify gaps: What does the tab look like? Can ratings be changed? What about offline?
3. Ask: "Should the user be able to edit their rating from the watched tab, or is it read-only there?"
4. (Continue one topic at a time until all gaps are covered)
5. Save `docs/features/watched-movies/SPEC.md`
6. "I've saved the spec to `docs/features/watched-movies/SPEC.md` — take a look and let me know if anything needs adjusting."
