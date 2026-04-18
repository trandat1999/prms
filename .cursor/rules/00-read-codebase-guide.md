---
description: Always load the PRMS codebase guide before doing any task
alwaysApply: true
---

# Read Guide First

Before analyzing, planning, or editing code in this repository:

1. Read `docs/CURSOR_CODEBASE_GUIDE.md`.
2. Use it to identify the impacted backend/frontend area.
3. Open only the relevant files for the current task instead of scanning the whole repo.

When a task spans both backend and frontend:

- preserve the `BaseResponse` API contract
- keep DTO/model/enum names aligned across both sides
- check auth, i18n, and shared input usage when relevant

If the guide and the live code disagree, trust the live code and update the guide as part of the task.
