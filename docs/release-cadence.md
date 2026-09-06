# Release cadence — TASK-290

The default release cadence is **slow and deliberate**. This app is not competing on feature velocity.

## What triggers a release

**Patch release (`x.y.Z`)**
- Confirmed rendering defect fix (Category A per `docs/content-corrections.md`).
- Confirmed content correction requiring a new dataset (Category C).
- Security advisory in a shipped runtime dependency.
- Compatibility fix for a Google Play policy deadline (targetSdk bumps, etc.).

**Minor release (`x.Y.0`)**
- A validated POST-MVP feature from the scope list has completed its own phase gates.
- Never for "we could add X" unless X was pre-scoped.

**Major release (`X.0.0`)**
- A charter-level change (opening ADRs) — not anticipated for years.

## What does NOT trigger a release

- "Users would love X" without X being in POST-MVP scope. Log for future consideration; do not ship.
- Dependency updates for their own sake. Update only when a specific reason exists (security, compatibility, or a specific bug fix in the newer version).
- UI trends, framework migrations, or engagement features.
- Any A/B testing infrastructure. There is no experimentation platform in this app.

## Release process (all release types)

1. Bug/feature is on a feature branch; PR merged to `main` after review.
2. Version bump commit on `main` (`versionCode` + `versionName`).
3. Full CI green (assemble + spotless + unit tests + eventually instrumented).
4. `git tag v<x.y.z>` — signed if a signing key is configured.
5. Manual sanity test on the maintainer's device.
6. Upload AAB to Play Console internal track. Smoke test.
7. Promote to production with staged rollout: 10% → 25% → 50% → 100% over ~7 days.
8. Watch Play Console vitals at each stage (see `docs/monitoring-cadence.md`).

Any single stage showing crash-free rate < 99.5% halts rollout for triage.

## Version-number rules

Semantic versioning with the following interpretations:

- **Content changes** always bump at minimum a patch version.
- **App code changes without content changes** bump patch (bug) or minor (feature).
- The dataset version is independent (`quran-<x.y.z>.db`) and follows its own semver — most app releases keep the same dataset. A dataset bump inside a patch release is allowed and expected for content corrections.

The `versionCode` is a monotonically increasing integer that Play uses for ordering; `versionName` is the human-facing string. `android/app/build.gradle.kts` **derives `versionCode` from `versionName`** using the formula `(major * 10000) + (minor * 100) + patch`, so you edit `appVersionName` once and both stay in sync.

Example: `1.0.0` → `versionCode = 10000`. `1.0.1` → `10001`. `1.1.0` → `10100`. `2.0.0` → `20000`.

Verification: after any bump, run `./gradlew :app:versionCode` (or inspect the generated APK's manifest) and confirm the derived code matches the intended semver.

## What years look like

- Year 1: several patch releases as content corrections and small bug fixes emerge from real usage. One minor release maybe, only if a POST-MVP feature ships.
- Year 2+: mostly patch releases. Minor releases only when a specific pre-scoped item is validated and ready.

## Reference

- ADR-0020 (release strategy) — signing + rollout.
- Charter §16 (correctness beats cleverness) — informs the "do not upgrade for its own sake" rule.
