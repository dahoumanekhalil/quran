# Contributing

The project is early-stage and closed to external contributions for now. Once the MVP ships, this document will describe the contribution model.

For the current maintainers:

## Workflow

- The roadmap in [`PROJECT_TASKS.md`](PROJECT_TASKS.md) is authoritative. Do not start work that is not represented as a task.
- Every non-trivial architectural decision is recorded as an ADR under [`docs/adr/`](docs/adr/) using [`docs/adr/0000-template.md`](docs/adr/0000-template.md).
- Every principle in [`docs/CHARTER.md`](docs/CHARTER.md) is non-negotiable. If a proposed change violates a principle, revise the change.

## Commits

- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/). Examples:
  - `feat(reader): render bismillah on page 1`
  - `fix(search): normalize alef-maksura in query pipeline`
  - `docs(adr): record ADR-021 quran source`
  - `chore(build): bump gradle wrapper`
- Reference the relevant `TASK-NNN` in the commit body when applicable.

## Code style

- Kotlin (Android app) and Python (data pipeline) — formatted per project config (`.editorconfig`, ktlint via Spotless, `ruff` for Python once configured).
- No commented-out code. Delete or move to a branch.
- No trailing whitespace, LF line endings, UTF-8 without BOM.

## Data changes

- The frozen dataset (`data/output/RELEASE/quran-<version>.db`) is immutable. Any change to the Quran text or metadata requires bumping the content version and re-running the full Phase 1 pipeline; see the Change Policy in `data/output/RELEASE/CONTENT_MANIFEST.md`.

## Third-party dependencies

- Adding a runtime dependency requires an ADR justifying the addition, the license, and the maintenance burden. Prefer standard library and Google-supported libraries.
- Adding a build dependency (Gradle plugin, etc.) requires at least a brief note in the relevant ADR.

## Security and privacy

- Do not add third-party analytics, telemetry, or crash-reporting SDKs without an ADR and an explicit user-facing opt-in.
- Never commit secrets, API keys, or signing keys. `.gitignore` excludes common patterns; verify before staging.
