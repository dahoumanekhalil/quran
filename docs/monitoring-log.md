# Monitoring log

Append-only log of the reviews defined in `docs/monitoring-cadence.md`.

Format: each entry starts with a heading `## YYYY-MM-DD — Weekly | Monthly | Quarterly | Ad-hoc`.
Write "clean week" (or equivalent) if there's nothing to note. Silence is not signal.

## 2026-09-06 — Ad-hoc (bootstrap)

Log file created as part of the Phase 19 audit close. No Play Store presence yet — nothing to review. First real weekly entry will land after the internal-testing release (TASK-269).

Reference state at bootstrap:
- App version: `0.1.0` (versionCode `10`)
- Dataset: `quran-1.0.0.db`, SHA-256 `0ba78b6ab99f57a5688adc572f35ccf0568716a48d4424378d73cfd91c0b093e`
- Runtime deps: as inventoried in `docs/dependencies.md` (last audited 2026-09-06)
- Permissions in merged manifest: zero positive; four `tools:node="remove"` denials.
- Crash-free rate: N/A (no Play presence).
- Open Play policy strikes: none.

## Template — copy for each entry

```
## YYYY-MM-DD — Weekly

Crash-free users:      __.__% (target ≥ 99.5%)
ANR rate:              __.__% per user per week (target < 0.1%)
Top crash clusters:    (list up to 3, or "none")
Battery / wakeups:     (should be nil — we hold no wake locks)

Actions:
  - (any hotfix triggered? Reference the follow-up ticket or "none.")
```

```
## YYYY-MM-DD — Monthly

Android Security Bulletin (YYYY-MM): (link + one-line summary of relevance to our surface)
Play Console app health: (any notification since last month? "none" if clean)

Actions:
  - (upgrade / triage / none)
```

```
## YYYY-MM-DD — Quarterly

Dependency delta from previous audit:  (diff libs.versions.toml)
Advisories intervening this quarter:   (per dep)
Forbidden-groups grep on releaseRuntimeClasspath: (pass / fail — see docs/dependencies.md § Forbidden)

Decisions:
  - Hold: (list deps + rationale)
  - Upgrade: (list deps + reason + test-plan reference)
```

```
## YYYY-MM-DD — Ad-hoc

Trigger: (Play policy deadline / vitals alert / user report / etc.)
Assessment:
Action:
```
