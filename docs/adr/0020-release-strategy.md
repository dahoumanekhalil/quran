# ADR-0020: Play App Signing, SemVer, staged rollout

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 17

## Context and problem statement

The app needs a release channel, a signing model, a version-number scheme, and a rollout policy. These decisions are hard to reverse once the first production build is signed.

## Decision drivers

- Charter #11 — extreme stability; any regression must be catchable early.
- Signing key security — the upload key must be recoverable / rotatable.
- Rollout must allow controlled exposure and easy halt.

## Options considered

### Signing

- **Play App Signing** (recommended) — Google holds the app-signing key; developer holds the upload key. Upload key can be rotated. Standard for new apps.
- Manual signing with a locally held keystore — full control, but loss of the key means the app cannot be updated ever. Rejected.

### Versioning

- **SemVer for `versionName` (`major.minor.patch`) + monotonic `versionCode`** (recommended). Every build in the pipeline bumps `versionCode`; `versionName` bumped per release.
- Calendar versioning (`2026.09.04`) — clean but obscures scope of change.

### Rollout

- **Staged: internal → closed → open testing → production, with % rollout in production, starting at 10% and expanding as crash-free ratio holds** (recommended).
- All-at-once — high blast radius on regressions.

## Decision

**Play App Signing + SemVer + staged rollout.**

- Upload key generated locally, stored securely, referenced from `signing.properties` (gitignored). Google holds the app-signing key via Play App Signing.
- `versionCode` monotonically increments per CI build. `versionName` is `<major>.<minor>.<patch>` (charter release cadence: v1.0.0 MVP, v1.x post-MVP, v2.x future).
- Rollout: internal (self) → closed test (small group) → open test (broader) → production 10% → 25% → 50% → 100%, contingent on **crash-free sessions ≥ 99.9%** and no critical bug reports at each step.
- Rollback: halt rollout via Play Console + revert the offending commit + release patch.

## Consequences

- Positive: key loss is recoverable; regressions have a small blast radius; version numbers convey change scope.
- Negative: staged rollout slows down "everyone gets the update" by a few days per release — a feature, not a bug, for a reader that must not crash.
- Follow-up: TASK-125 (Phase 18) implements the CI signing wiring; TASK-127 does the internal → closed → open sequence.

## References

- Play App Signing: https://support.google.com/googleplay/android-developer/answer/9842756
