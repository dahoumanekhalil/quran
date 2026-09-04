# ADR-0013: Fully offline for MVP core Quran content

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 4

## Context and problem statement

The app's core value is reading the Quran; that must never depend on network connectivity, credentials, or a backend the project doesn't control (charter #7). Later features (audio, translations) may involve downloads, but MVP does not.

## Decision drivers

- Charter #7 — reading must never require a network.
- Charter #5 / #6 — no third-party trackers, no telemetry without opt-in.
- Charter #8 — content integrity is highest priority; runtime downloads would introduce a corruption / tampering vector.
- Cold-start performance — bundled content skips a first-run download.

## Options considered

### Option A — Fully bundled (recommended)

The entire Quran dataset ships in the APK/AAB assets. First launch verifies the bundled artifact hash, then copies to internal storage (see ADR-0014). No network calls involved in Quran content.

### Option B — First-run download from a controlled CDN

Smaller install size. Introduces first-run friction, corruption risk, CDN dependency. Contradicts charter #7 for the first launch.

### Option C — Hybrid (base content bundled, extras downloadable)

Useful in the future for audio, tafsir, translations — not needed for MVP.

## Decision

**Fully bundled (Option A) for MVP.**

The frozen `quran-1.0.0.db` (~3.2 MB) is added to `assets/quran/`. First-run: verify against a bundled SHA-256; copy to internal storage; open read-only. No network permission is requested by the MVP.

## Consequences

- Positive: works instantly on first launch, offline forever, no CDN cost, no correctness risk from download failures, no `INTERNET` permission in the manifest.
- Negative: APK/AAB size grows by ~3.2 MB — trivially acceptable at the MVP's total size.
- Follow-up: ADR-0014 details the asset packaging and first-run copy strategy.

## References

- Data v1.0.0 hash: `data/output/RELEASE/CONTENT_MANIFEST.md`.
- Charter principles #5, #6, #7, #8.
