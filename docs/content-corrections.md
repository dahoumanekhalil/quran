# Content correction process — TASK-288

The Quran text in this app is treated as immutable religious material (Charter Principle §8). This document is the process for what happens if a defect is nevertheless found.

## Reporting channel

Reports arrive via one of:

- GitHub Issues on the project repository (public, with template).
- Email to the address in the About screen (private, for reporters who prefer it).

Each report **must** include:

- Exact Surah and Ayah reference (e.g., 2:255).
- Page number in the app (visible in the reader chrome).
- The specific character or word alleged to differ, and the reference source (a specific printed Mushaf or authoritative digital text).
- Screenshot of the app's rendering.

Reports without at least an ayah reference + a reference source are triaged as "insufficient" and closed with a request for more information. No exceptions — content changes without independent verification are worse than the defect they claim to fix.

## Triage — first 24 hours

The maintainer classifies the report into one of four categories:

**A. Rendering defect** — the underlying byte-for-byte text in `data/output/RELEASE/quran-1.0.0.db` is correct, but the app is misdisplaying it (missing character in font, wrong shaping, wrong ayah marker). Fix in code + font-family selection, no dataset change.

**B. Diacritic-only variance** — Tanzil and KFGQPC lineages differ in diacritic placement on some words. These are documented as expected in `data/output/intermediate/cross-source-diff-report.md`. Not defects. Close with a link to that report.

**C. Confirmed consonantal defect** — the skeleton (consonantal letters, ignoring diacritics) does not match the reference. **This is release-blocking.** Escalate.

**D. Ambiguous** — needs additional expert review. Route to a knowledgeable reviewer (list maintained in `docs/reviewers.md` — TBD by TASK-289) with a 7-day SLA.

## For confirmed Category C defects

1. Do not patch content in application code. Ever.
2. Reproduce with the exact ayah and reference source.
3. Correct the source data at the pipeline stage that introduced the error. If Tanzil is wrong, upstream the report to the Tanzil project; if quran-meta is wrong, upstream to that project. In the meantime, apply a documented override in `data/pipeline/overrides.json` with a full audit trail (source of the correction, reviewer, date).
4. Re-run the full Phase 1 pipeline. All validation gates (structural, Unicode, cross-source) must pass.
5. Bump the dataset version: `quran-1.0.0.db` → `quran-1.0.1.db`. Update `QuranAsset.CONTENT_VERSION` and `CONTENT_SHA256`.
6. Bump the app `versionCode` and `versionName`.
7. Update `CONTENT_MANIFEST.md` with the change record.
8. Run the app regression on the new dataset.
9. Ship as a patch release.

## Rendering-only defects (Category A)

- Fix in code (e.g., choose a different font that has the glyph, or adjust rendering).
- Bump app `versionCode` and `versionName` only. Dataset unchanged.
- Standard release process.

## Dry run

Per TASK-288 acceptance criteria, at least one dry run must be performed before v1.0 ships.

**Automated (minimum bar):** run `scripts/content-correction-dry-run.py`. It injects a synthetic consonantal defect on Ayat al-Kursi (2:255) in-memory and verifies that the pipeline's skeleton-normalizer detects it — proving the cross-source-diff Phase 1 gate would refuse the mutated corpus. This is a fast (< 1 s) sanity check and should be re-run whenever the normalizer changes.

**Full workflow (release gate before v1.0):** the automated check exercises only the skeleton normalizer. The full end-to-end walk-through additionally proves:

- The `QuranDb` SHA-256 gate on the Android side refuses to open a tampered `quran.db`.
- The `QuranAsset.CONTENT_SHA256` bump procedure works when a corrected dataset is produced.
- The app fails cleanly and safely if the pipeline output diverges from the expected hash.

Steps for the full walk-through:

1. Duplicate `data/output/RELEASE/quran-1.0.0.db` to a scratch location; edit one ayah's `text_uthmani` by hand (SQLite browser is fine).
2. Compute the new SHA-256 with `sha256sum` (or PowerShell `Get-FileHash`).
3. Copy the tampered DB into `android/app/src/main/assets/quran/quran.db` **without** updating `QuranAsset.CONTENT_SHA256`.
4. Build the debug APK. Install on device.
5. Verify: the app throws on first `QuranDb.database()` access and does **not** silently open the tampered content.
6. Restore the untampered DB. Rebuild. Verify normal operation.

Dry-run results archived at `data/output/dry-runs/<date>-content-correction.md` before Phase 17 freeze.

## Never

- Never edit `text_uthmani` in the SQLite artifact directly.
- Never silently "fix" content in an app patch without a corresponding dataset version bump.
- Never accept a correction without independent expert review for consonantal changes.
- Never accept a correction that is "just a diacritic" without checking Cross-Source Diff first — most such reports are Category B.

## References

- Charter Principle §8 (`docs/CHARTER.md`) — content immutability.
- ADR-0021 (Quran source) — Tanzil selection and provenance.
- Phase 1 pipeline (`data/pipeline/pipeline.py`) — validation gates.
- `data/output/RELEASE/CONTENT_MANIFEST.md` — currently frozen dataset.
