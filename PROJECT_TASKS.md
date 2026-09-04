# PROJECT_TASKS.md — Digital Mushaf (Android Quran Application)

> A chronological, dependency-aware engineering roadmap for building an offline-first, Mushaf-style Android Quran application from zero to Google Play release.
> This document is the single source of truth for planning, sequencing, and executing all engineering work on the project.

---

## Project Status

**Current Phase:** Phase 2 — Typography & Rendering Prototype (queued; blocked on ADR-0023 CI opt-in and TASK-025 font selection)
**Current Task:** TASK-025 (Phase 2 kickoff)
**Completed Tasks:** TASK-001..TASK-024 (Phase 0 backfilled 2026-09-04; Phase 1 frozen as content v1.0.0)
**Blocked Tasks:** TASK-030 (Android project init) — blocked until developer opts in to cloud CI (per ADR-0023)
**Critical Issues:** None
**Release Readiness:** Not Ready

### Phase Progress Tracker

- [x] Phase 0 — Project Preparation
- [x] Phase 1 — Quran Data Foundation (frozen: `data/output/RELEASE/quran-1.0.0.db`)
- [ ] Phase 2 — Typography & Rendering Prototype
- [ ] Phase 3 — Android Foundation
- [ ] Phase 4 — Core Data & Domain
- [ ] Phase 5 — Reader Engine
- [ ] Phase 6 — Quran Navigation
- [ ] Phase 7 — Home Experience
- [ ] Phase 8 — Search
- [ ] Phase 9 — Bookmarks & Reading State
- [ ] Phase 10 — Settings
- [ ] Phase 11 — Accessibility
- [ ] Phase 12 — Performance Engineering
- [ ] Phase 13 — Security & Privacy
- [ ] Phase 14 — Full QA
- [ ] Phase 15 — Visual Refinement
- [ ] Phase 16 — Real Device Validation
- [ ] Phase 17 — Release Candidate
- [ ] Phase 18 — Google Play Launch
- [ ] Phase 19 — Post-Launch Maintenance

Task status legend:

- **Not Started** — task has not been picked up yet
- **In Progress** — actively being implemented
- **Blocked** — cannot proceed due to an unresolved dependency or issue
- **Completed** — implementation, tests, and acceptance criteria are all satisfied

---

# Project Vision

We are building a **digital Mushaf**, not a conventional text-reading application.

When a user opens the application, the intended experience is:

1. Open the app.
2. Immediately return to the Quran at their last reading position.
3. Read comfortably.
4. Turn pages naturally, as if handling a physical Mushaf.
5. Remain undistracted, focused only on the content.

The application must feel: **calm, elegant, minimal, respectful, fast, reliable, timeless, and comfortable for long reading sessions.**

The Quran must always remain the visual and functional center of the application. Every pixel, animation, control, and code decision must serve that center. Anything that competes with the reading experience must be removed, hidden, or redesigned.

Visual philosophy priority order:

**Content > Reading Comfort > Navigation > Controls > Decoration.**

There is no dashboard, no engagement funnel, no marketing surface, no gamification, no productivity metaphor. There is only the Mushaf and the smallest, quietest set of tools required to navigate it.

---

# Product Principles

These principles are **non-negotiable** and must be honored by every engineering decision.

1. **No advertisements.** Not now, not ever.
2. **No subscriptions.** No paid tiers, no premium features.
3. **No commercial pressure.** No upsells, no prompts to rate/share/upgrade.
4. **No mandatory user accounts.** Anonymous, local-first usage is the baseline.
5. **No tracking.** No third-party analytics SDKs, no behavioral profiling.
6. **No intrusive telemetry.** Any diagnostic collection must be opt-in, minimal, and locally observable.
7. **Full offline operation for core Quran content.** Reading must never require a network.
8. **Quran content integrity is the highest priority.** Content is treated as immutable religious material.
9. **Reading experience takes priority over secondary features.**
10. **Performance matters on low-end devices**, not only flagships.
11. **The application must be extremely stable.** Crashes in the reader are release-blocking.
12. **UI must remain simple and respectful.** No experimental interaction patterns.
13. **External dependencies are introduced carefully**, only when clearly justified.
14. **Every major architectural decision is documented** as an Architecture Decision Record (ADR).
15. **Every feature must justify its presence.** If it does not serve reading, it does not ship.
16. **Correctness beats cleverness.** Boring, verified code beats novel, unverified code.

---

# Scope

## MVP (must ship in v1.0)

- Complete Quran (114 Surahs, all Ayahs).
- Mushaf-style page-based reading using standard Madani mushaf pagination (604 pages).
- Previous / next page navigation.
- Surah list and navigation.
- Direct page navigation (jump to page number).
- Juz navigation.
- Reading position restoration (last-read page + ayah where applicable).
- Bookmarks (add, list, remove, jump).
- Offline Arabic Quran search.
- Correct Arabic RTL rendering with a Mushaf-quality font.
- Light and Dark themes.
- Reading settings (theme, keep screen on, minimal essentials only).
- Fully offline operation for all MVP features.
- Baseline accessibility (TalkBack labels, adequate touch targets, contrast).
- Responsive layouts for the most common Android phone form factors.
- Stable state restoration across process death and configuration changes.
- App information / About page (version, dataset source and hash, credits, licenses, privacy statement).

## POST-MVP (targeted for v1.1 – v1.3)

- Hizb / Rub markers in navigation.
- Ayah-level bookmarks with notes.
- Reading progress statistics (private, on-device only).
- Verse-of-the-day widget.
- Configurable page-turn animation styles.
- Landscape / tablet-optimized layouts.
- Multiple Mushaf fonts (opt-in).
- Basic haptic feedback for page turns.

## FUTURE (v2+, not to be considered during MVP planning)

- Audio recitation (multiple qaris) with offline downloads.
- Translation and tafsir modules (strictly separated from the Mushaf view).
- Ayah sharing as image cards.
- Bookmark synchronization across devices (only via explicit user-initiated export/import).
- Wear OS companion.

Scope creep into the MVP is explicitly disallowed. Any post-MVP or future idea that appears during MVP execution must be logged as a candidate for a later phase, not implemented.

---

# Architecture Decisions

This section lists the architectural decision points that must be resolved **before** implementation begins. Each is handled as an ADR task in Phase 0.

Decisions to be documented (each as an ADR):

- ADR-001: Programming language (Kotlin vs. Java vs. multi-language).
- ADR-002: Android UI toolkit (Jetpack Compose vs. classic Views vs. hybrid).
- ADR-003: Minimum SDK, target SDK, compile SDK.
- ADR-004: Architecture pattern (MVVM, MVI, Clean layers, unidirectional data flow).
- ADR-005: Navigation solution (Navigation Compose, Jetpack Navigation, custom).
- ADR-006: State management (ViewModel + StateFlow, MVI store, other).
- ADR-007: Local persistence (Room, SQLite direct, DataStore, files, hybrid).
- ADR-008: Dependency injection (Hilt, Koin, manual, other).
- ADR-009: Concurrency model (Coroutines + Flow, RxJava, other).
- ADR-010: Data layer architecture (repositories, sources, mappers).
- ADR-011: Domain layer architecture (use cases vs. thin services).
- ADR-012: UI layer architecture (screen state, event handling).
- ADR-013: Offline-first strategy (bundled asset vs. first-run import vs. hybrid).
- ADR-014: Quran content asset packaging (raw JSON, prebuilt SQLite, custom binary).
- ADR-015: Font packaging strategy (bundled TTF/OTF, downloadable font, split by density).
- ADR-016: Reader paging approach (ViewPager2, HorizontalPager, custom paged renderer).
- ADR-017: Text rendering approach (native TextView, Compose Text, Canvas draw, other).
- ADR-018: Testing strategy (unit / integration / instrumented split, tooling).
- ADR-019: Build system (Gradle Kotlin DSL, version catalog, module structure).
- ADR-020: Release strategy (single AAB, split APKs, Play App Signing, rollout policy).

Every ADR must record: context, options considered, decision, consequences, and a review date. ADRs live in `/docs/adr/NNNN-title.md` in the repository.

Selection criteria applied to every architectural choice:

- Long-term maintainability
- Performance on low-end devices
- Stability under real-world use
- Testability
- Offline correctness
- Android version compatibility
- Ease of future expansion
- Minimal external dependency footprint

Kotlin + Jetpack Compose are candidates, not assumptions.

---

# Definition of Done

A **task** is complete only when all of the following hold:

- Implementation exists and is committed.
- Every acceptance criterion is objectively satisfied.
- Relevant automated tests pass locally and in CI.
- Regression risk on dependent areas has been checked.
- Documentation (ADR, README, inline notes at genuine complexity points) is updated where necessary.
- Downstream dependencies remain valid.
- No known critical issue introduced by the task remains unresolved.

The **project** is complete only when:

- Quran content is verified end-to-end.
- Rendering is correct across the target device matrix.
- The reading experience is polished and feels like a Mushaf.
- Offline mode fully works for MVP features.
- Search works accurately.
- Bookmarks work reliably.
- Reading position restoration works across all lifecycle scenarios.
- Baseline accessibility works.
- Performance targets are met on the low-end reference device.
- Known critical crashes are eliminated.
- Privacy review is complete.
- The release AAB is verified via clean install and upgrade install.
- Google Play preparation is complete.

---

# Phase 0 — Project Preparation

Goal: establish decisions, guardrails, and tooling. **No feature code is written in this phase.**

## TASK-001 — Charter the project and lock the vision

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** —
**Status:** Not Started

**Objective:**
Produce a short, signed project charter that captures vision, principles, MVP scope, and non-goals, and reference this `PROJECT_TASKS.md` as the operational plan.

**Why now:**
Every downstream decision (architecture, dependencies, UX) must be reconcilable with the vision. Without a locked charter, scope drift is inevitable.

**Implementation:**
Create `/docs/CHARTER.md` containing: vision, principles (verbatim from this document), MVP scope, POST-MVP, FUTURE, explicit non-goals, and a link to this roadmap. Add a review cadence (revisit before every phase transition).

**Acceptance Criteria:**
- `docs/CHARTER.md` exists.
- Charter enumerates all product principles.
- Charter defines MVP, POST-MVP, FUTURE, and explicit non-goals.
- Charter is referenced from repository `README.md`.

**Validation:**
Peer read of the charter; verify all principles from this file are present.

**Output:**
`docs/CHARTER.md`, updated `README.md`.

**Blocks:**
TASK-002, all subsequent tasks.

---

## TASK-002 — Establish repository and documentation skeleton

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-001
**Status:** Not Started

**Objective:**
Create the empty repository structure with directories for documentation, ADRs, data pipeline, prototypes, and Android app, without any application code yet.

**Why now:**
Provides a stable location for ADRs, dataset artifacts, and prototype outputs to be produced in Phase 0–2 before the Android app is even initialized.

**Implementation:**
Create directories: `/docs`, `/docs/adr`, `/data`, `/data/sources`, `/data/pipeline`, `/data/output`, `/prototype`, `/android` (placeholder), `/scripts`. Add `.gitignore` covering build outputs, IDE files, secrets, and dataset intermediate files. Add `README.md`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`.

**Acceptance Criteria:**
- Repository structure exists with the directories above.
- `.gitignore` excludes build artifacts and secrets.
- `README.md` describes the project purpose and points to charter and roadmap.

**Validation:**
Fresh clone; verify no unwanted files are tracked.

**Output:**
Repository skeleton, base documentation.

**Blocks:**
TASK-003 onward.

---

## TASK-003 — Author ADR template and process

**Priority:** High
**Phase:** Phase 0
**Depends on:** TASK-002
**Status:** Not Started

**Objective:**
Define a standard ADR format and enforce that every architectural decision (ADR-001..ADR-020) uses it.

**Why now:**
All Phase 0 decisions will be recorded as ADRs. The template must exist before any ADR is written.

**Implementation:**
Create `/docs/adr/0000-template.md` with sections: Context, Decision Drivers, Options Considered, Decision, Consequences, Review Date. Create `/docs/adr/README.md` describing the numbering convention and the review process.

**Acceptance Criteria:**
- ADR template exists and is unambiguous.
- ADR index README explains the process.

**Validation:**
Draft one throwaway ADR from the template and verify usability.

**Output:**
`/docs/adr/0000-template.md`, `/docs/adr/README.md`.

**Blocks:**
TASK-004 through TASK-011 (all ADR tasks).

---

## TASK-004 — ADR-001..ADR-003: Language, UI toolkit, SDK levels

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-003
**Status:** Not Started

**Objective:**
Decide and document: primary language, UI toolkit, minSdk/targetSdk/compileSdk.

**Why now:**
Every subsequent decision — dependencies, testing tools, rendering approach — depends on these.

**Implementation:**
Author `docs/adr/0001-language.md`, `docs/adr/0002-ui-toolkit.md`, `docs/adr/0003-sdk-levels.md`. Evaluate Kotlin vs. Java (favoring Kotlin for null-safety, coroutines, ecosystem), evaluate Jetpack Compose vs. Views (with attention to Compose's RTL and Arabic shaping maturity vs. mature `TextView` rendering), and select SDK levels balancing reach (recommended minSdk 24 for ~99% reach) and modern API access (recommended targetSdk = current Google Play requirement at release time).

**Acceptance Criteria:**
- Three ADRs merged, each with alternatives explicitly considered.
- Decisions include justification tied to Arabic rendering, reading UX, and offline requirements.

**Validation:**
Review each ADR against the ADR template checklist.

**Output:**
ADR-001, ADR-002, ADR-003.

**Blocks:**
TASK-005..TASK-011, TASK-030 (project init).

---

## TASK-005 — ADR-004..ADR-006: Architecture, navigation, state management

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-004
**Status:** Not Started

**Objective:**
Decide the app-wide architecture pattern, navigation library, and state-management approach.

**Why now:**
These constrain how every screen is built. Deciding after coding leads to inconsistent screens and painful rewrites.

**Implementation:**
Author `docs/adr/0004-architecture.md` (recommend Clean-layered MVVM with unidirectional data flow), `docs/adr/0005-navigation.md`, `docs/adr/0006-state-management.md`. Explicitly account for state survival across process death and RTL-aware navigation.

**Acceptance Criteria:**
- Three ADRs merged.
- Each ADR explicitly discusses process death and configuration change survival.

**Validation:**
Walk through a sample screen flow (reader → surah list → reader) on paper against each choice.

**Output:**
ADR-004, ADR-005, ADR-006.

**Blocks:**
TASK-006, TASK-030, all UI tasks.

---

## TASK-006 — ADR-007..ADR-009: Persistence, DI, concurrency

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-005
**Status:** Not Started

**Objective:**
Decide local persistence, dependency injection, and concurrency model.

**Why now:**
The data layer and reader engine cannot be built without these.

**Implementation:**
Author `docs/adr/0007-persistence.md` (recommend Room for user data, prebuilt SQLite asset for Quran content, DataStore for lightweight settings), `docs/adr/0008-di.md`, `docs/adr/0009-concurrency.md`.

**Acceptance Criteria:**
- Three ADRs merged with justifications.
- Persistence ADR explicitly separates immutable Quran content from mutable user state.

**Validation:**
Cross-check persistence choice against offline-first requirement and asset packaging strategy.

**Output:**
ADR-007, ADR-008, ADR-009.

**Blocks:**
TASK-007, TASK-050 (data layer).

---

## TASK-007 — ADR-010..ADR-012: Data, domain, UI layer architecture

**Priority:** High
**Phase:** Phase 0
**Depends on:** TASK-006
**Status:** Not Started

**Objective:**
Define layer boundaries and responsibilities.

**Why now:**
Setting boundaries before code prevents cross-layer leakage that is very expensive to untangle later.

**Implementation:**
Author `docs/adr/0010-data-layer.md`, `docs/adr/0011-domain-layer.md`, `docs/adr/0012-ui-layer.md`. Each ADR defines allowed inputs, outputs, and forbidden dependencies.

**Acceptance Criteria:**
- Three ADRs merged.
- Explicit rules: UI never touches persistence directly; domain has no Android dependencies; data layer exposes only domain-friendly types.

**Validation:**
Static-analysis rules will later be configured (in TASK-034) to enforce these boundaries.

**Output:**
ADR-010, ADR-011, ADR-012.

**Blocks:**
TASK-050+, TASK-034.

---

## TASK-008 — ADR-013..ADR-015: Offline strategy, content packaging, font packaging

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-007
**Status:** Not Started

**Objective:**
Decide how Quran content and fonts are shipped and loaded.

**Why now:**
Determines APK/AAB size, first-launch behavior, and update mechanics. Blocks the data pipeline and the rendering prototype.

**Implementation:**
Author `docs/adr/0013-offline-strategy.md` (recommend fully bundled dataset in assets, verified on first launch, copied to internal storage), `docs/adr/0014-content-packaging.md` (recommend prebuilt read-only SQLite for the Quran content), `docs/adr/0015-font-packaging.md` (recommend bundled OTF, single primary font in MVP).

**Acceptance Criteria:**
- Three ADRs merged.
- Trade-offs on AAB size vs. first-launch complexity are quantified with estimates.

**Validation:**
Estimate final AAB size implication and verify it is acceptable.

**Output:**
ADR-013, ADR-014, ADR-015.

**Blocks:**
Phase 1 (data pipeline), Phase 2 (rendering prototype), TASK-030.

---

## TASK-009 — ADR-016..ADR-017: Reader paging and text rendering approach

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-004, TASK-008
**Status:** Not Started

**Objective:**
Choose the paging mechanism (e.g., HorizontalPager, ViewPager2, custom) and the text rendering surface (Compose Text, native TextView, Canvas draw).

**Why now:**
The rendering prototype in Phase 2 must implement the chosen approach. Making this decision after prototyping wastes work.

**Implementation:**
Author `docs/adr/0016-paging.md` and `docs/adr/0017-text-rendering.md`. Explicitly analyze Arabic shaping, RTL support, accessibility, memory, state restoration, and page-transition smoothness for each option.

**Acceptance Criteria:**
- Two ADRs merged.
- Each option is rated against the criteria list above.
- Chosen approach is testable with a small prototype in Phase 2.

**Validation:**
Reviewed against Phase 2 prototype acceptance criteria.

**Output:**
ADR-016, ADR-017.

**Blocks:**
TASK-025 (rendering prototype), Phase 5.

---

## TASK-010 — ADR-018..ADR-019: Testing and build system

**Priority:** High
**Phase:** Phase 0
**Depends on:** TASK-005
**Status:** Not Started

**Objective:**
Standardize the testing stack and the Gradle build system.

**Why now:**
Testing tasks are interleaved with implementation from Phase 3 onward. Build system decisions cannot be retrofitted cleanly.

**Implementation:**
Author `docs/adr/0018-testing.md` (unit: JUnit + Kotest/Truth; instrumented: Espresso or Compose UI test; integration: Robolectric where appropriate) and `docs/adr/0019-build-system.md` (Gradle Kotlin DSL, version catalog `libs.versions.toml`, multi-module structure).

**Acceptance Criteria:**
- Two ADRs merged.
- Module boundaries described and justified.

**Validation:**
Verified against the module topology proposed in Phase 3.

**Output:**
ADR-018, ADR-019.

**Blocks:**
TASK-030, TASK-034.

---

## TASK-011 — ADR-020: Release strategy

**Priority:** High
**Phase:** Phase 0
**Depends on:** TASK-008, TASK-010
**Status:** Not Started

**Objective:**
Document release channels, signing, versioning, and rollout policy.

**Why now:**
Version scheme and signing model must be in place before any release build is produced.

**Implementation:**
Author `docs/adr/0020-release-strategy.md` covering Play App Signing, semantic version scheme (`major.minor.patch` + monotonic `versionCode`), staged rollout (internal → closed → open → production with gradual %), and rollback procedure.

**Acceptance Criteria:**
- ADR-020 merged.
- Clear rules for version bumps and rollback.

**Validation:**
Reviewed against Phase 18 requirements.

**Output:**
ADR-020.

**Blocks:**
Phase 17, Phase 18.

---

## TASK-012 — Choose and document the Quran content source

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-008
**Status:** Not Started

**Objective:**
Select an authoritative Quran text source and document its provenance and license.

**Why now:**
The rest of Phase 1 depends on knowing exactly which source we import. Content integrity begins at the source.

**Implementation:**
Evaluate sources such as the Tanzil.net Uthmani text, the King Fahd Complex data, and the QuranComplex text. Document: exact rendition (Uthmani Hafs `an` Asim), Unicode version, source URL, license, checksum of the specific downloaded artifact, and archival copy under `/data/sources/` with a fetched-on date. Record the decision as `docs/adr/0021-quran-source.md`.

**Acceptance Criteria:**
- ADR-021 merged, source selected and justified.
- Original source file archived under `/data/sources/<source-name>/<version>/` with a SHA-256 hash.
- License terms recorded and compatible with the app's distribution model.

**Validation:**
Independent read-through: another reviewer can reproduce the download and verify the hash.

**Output:**
ADR-021, `/data/sources/` archive, license notes.

**Blocks:**
TASK-013, Phase 1.

---

## TASK-013 — Choose the pagination reference (Madani mushaf 604 pages)

**Priority:** Critical
**Phase:** Phase 0
**Depends on:** TASK-012
**Status:** Not Started

**Objective:**
Select and document the pagination reference (the physical Mushaf whose 604-page layout the app will follow) and secure page-to-ayah mapping data.

**Why now:**
The reader is page-based. Without an authoritative page mapping, page boundaries cannot be validated.

**Implementation:**
Select the standard King Fahd Complex Madani mushaf (15 lines per page, 604 pages) as the pagination reference (or an equivalent). Locate a machine-readable page-to-ayah mapping. Archive under `/data/sources/pagination/` with hashes. Record as `docs/adr/0022-pagination.md`.

**Acceptance Criteria:**
- ADR-022 merged.
- Page mapping archived with hash.
- Every page in the mapping has a defined first ayah and last ayah.

**Validation:**
Spot-check 20 random pages against a printed Madani mushaf reference.

**Output:**
ADR-022, `/data/sources/pagination/*`.

**Blocks:**
TASK-018, Phase 1 pipeline validation.

---

## TASK-014 — Configure repository tooling (formatting, license header, commit hygiene)

**Priority:** Medium
**Phase:** Phase 0
**Depends on:** TASK-002
**Status:** Not Started

**Objective:**
Enforce a consistent baseline for formatting and commit hygiene from day one.

**Why now:**
Retrofitting formatting to a large codebase creates noisy diffs. Establishing it early keeps history clean.

**Implementation:**
Configure `.editorconfig`, a formatter (ktlint/spotless once Kotlin is confirmed), commit-message conventions (Conventional Commits recommended), and a `LICENSES/` directory for third-party licenses. Add a `NOTICE` file.

**Acceptance Criteria:**
- Formatting config present and enforceable.
- License and NOTICE files created.
- Contribution guide references these.

**Validation:**
Run the formatter on the current (empty) tree; must succeed.

**Output:**
Formatting config, license scaffolding.

**Blocks:**
TASK-034.

---

# Phase 1 — Quran Data Foundation

Goal: produce a frozen, verified, immutable Quran dataset with strict structural and Unicode validation. **No Android code depends on this being final until Phase 4.**

## TASK-015 — Define the canonical Quran data model

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-012, TASK-013
**Status:** Not Started

**Objective:**
Document the schema for Surahs, Ayahs, Pages, Juz, Hizb, Rub, and their relationships, independent of any storage format.

**Why now:**
The data schema must exist before the import pipeline can produce anything meaningful.

**Implementation:**
Author `docs/data-model.md` defining entities and fields:
- `Surah { number 1..114, name_ar, name_transliteration, revelation_place, ayah_count, bismillah_pre }`.
- `Ayah { surah_number, ayah_number_in_surah, ayah_global_index 1..6236, text_uthmani, page_number, juz_number, hizb_number, rub_number, sajda? }`.
- `Page { number 1..604, first_ayah_global_index, last_ayah_global_index, surahs_on_page[], juz_starts_on_page[] }`.
- `Juz { number 1..30, first_ayah_global_index, last_ayah_global_index }`.
- `Hizb`, `Rub` analogously.

Define invariants: total ayahs = 6236 (Kufi count used by Hafs); total pages = 604; total juz = 30; total hizb = 60; total rub = 240.

**Acceptance Criteria:**
- `docs/data-model.md` defines every entity, field, and invariant.
- All numeric constants cited with references.

**Validation:**
Peer review against pagination and source ADRs.

**Output:**
`docs/data-model.md`.

**Blocks:**
TASK-016 onward.

---

## TASK-016 — Build the import pipeline scaffold

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-015
**Status:** Not Started

**Objective:**
Create a deterministic, reproducible pipeline that ingests raw source files and produces validated intermediate structured data.

**Why now:**
Every validation and packaging step downstream reads the pipeline's intermediate output.

**Implementation:**
Under `/data/pipeline/` create a small language-agnostic tool (Python or Kotlin script — decide via ADR-023 at this task). The pipeline reads `/data/sources/`, produces `/data/output/intermediate/*.json` (per-surah + master index). The pipeline must be reproducible: same input hashes must yield same output hashes. Log every transformation step.

**Acceptance Criteria:**
- Running the pipeline twice on the same inputs produces byte-identical outputs.
- Pipeline emits a manifest with input hashes and output hashes.

**Validation:**
Run pipeline twice; diff outputs; must be empty. Run against a mutated input; hashes must change.

**Output:**
Pipeline scripts, `manifest.json`, intermediate outputs.

**Blocks:**
TASK-017.

---

## TASK-017 — Import Quran text with byte-preserving fidelity

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-016
**Status:** Not Started

**Objective:**
Ingest the selected Uthmani text and store each ayah's exact Unicode string without normalization, trimming, whitespace collapsing, or transliteration.

**Why now:**
Any silent transformation here will corrupt the Quran display. This must be enforced before any structural or rendering work.

**Implementation:**
Pipeline stage that reads the source (XML/plain text), parses into `Ayah` records, and preserves the original Unicode byte-for-byte. Explicitly forbid `.trim()`, `.replace()`, Unicode normalization (NFC/NFD/NFKC/NFKD), and any character replacement. Log the SHA-256 hash of the concatenated corpus.

**Acceptance Criteria:**
- Corpus hash reproducible and recorded.
- Automated test: for every ayah, the stored `text_uthmani` equals the source bytes at the correct offsets.

**Validation:**
Compare 50 randomly selected ayahs byte-for-byte against source. Assert no normalization has occurred.

**Output:**
Intermediate ayah records with untouched text.

**Blocks:**
TASK-018 onward.

---

## TASK-018 — Attach page, juz, hizb, rub metadata

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-013, TASK-017
**Status:** Not Started

**Objective:**
Map every ayah to its Madani page number, juz, hizb, and rub.

**Why now:**
Downstream reader, navigation, and search all require accurate positional metadata.

**Implementation:**
Merge the pagination mapping into ayah records. Build reverse indices (page → ayah range, juz → ayah range, etc.). Handle boundary ayahs (ayahs that span pages are conceptually a single ayah shown across two lines, but page-membership is defined by starting position — document exactly).

**Acceptance Criteria:**
- Every ayah has non-null page, juz, hizb, rub values within valid ranges.
- Reverse indices are dense and complete (no page missing, no juz missing).

**Validation:**
Automated: verify `sum(ayahs on page p for p in 1..604) == 6236`; verify no ayah appears on two pages.

**Output:**
Enriched intermediate records; page/juz/hizb/rub indices.

**Blocks:**
TASK-019.

---

## TASK-019 — Structural validation suite

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-018
**Status:** Not Started

**Objective:**
Automated tests that assert every structural invariant.

**Why now:**
Structural errors must fail the pipeline before any packaging occurs.

**Implementation:**
Test suite asserting:
- 114 Surahs, ordered 1..114 with no gaps.
- Ayah counts per Surah match the canonical Hafs table (e.g., Al-Fatiha=7, Al-Baqarah=286, ..., An-Nas=6). Total = 6236.
- 604 pages, 30 juz, 60 hizb, 240 rub.
- Every ayah references a valid page, juz, hizb, rub.
- Every page has ≥ 1 ayah; every juz starts at a documented ayah.
- Global ayah indices are 1..6236 with no duplicates.
- Surah 9 (At-Tawbah) has no pre-Bismillah (`bismillah_pre = false`); all others = true.
- Sajda ayahs match the standard list (14 or 15 depending on school — decide and document).

**Acceptance Criteria:**
- All structural assertions pass.
- Pipeline exits non-zero if any assertion fails.

**Validation:**
Deliberately corrupt one ayah count in a test fixture; verify pipeline fails.

**Output:**
Structural validation suite, pipeline gate.

**Blocks:**
TASK-020.

---

## TASK-020 — Unicode and typography validation suite

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-019
**Status:** Not Started

**Objective:**
Automated tests that assert Unicode correctness of Quranic text.

**Why now:**
Encoding corruption is silent and catastrophic. It must be detected before content is frozen.

**Implementation:**
Test suite that:
- Confirms every character in the corpus falls within the expected Unicode blocks: Arabic (U+0600–U+06FF), Arabic Supplement (U+0750–U+077F), Arabic Extended-A (U+08A0–U+08FF), Arabic Presentation Forms-A/B where legitimately used, Quranic annotation symbols (U+0610–U+061A, U+06D6–U+06ED), and whitespace only where expected.
- Rejects any C0/C1 control characters other than allowed.
- Verifies presence of expected Quranic marks (small high seen, small high madda, sajdah symbol, etc.) at expected positions.
- Detects trailing/leading whitespace anomalies in ayah text.
- Detects duplicate consecutive ayahs.
- Verifies encoding is valid UTF-8 with no BOM inside text fields.

**Acceptance Criteria:**
- All Unicode assertions pass.
- Pipeline emits a Unicode profile report (counts per block, list of exotic characters found).

**Validation:**
Inject a stray Latin character into a test fixture; verify pipeline fails.

**Output:**
Unicode validation suite, `unicode-profile.md` report.

**Blocks:**
TASK-021.

---

## TASK-021 — Cross-source verification pass

**Priority:** High
**Phase:** Phase 1
**Depends on:** TASK-020
**Status:** Not Started

**Objective:**
Sanity-check the imported corpus against a second independent Quran text source.

**Why now:**
Catches source-specific defects that internal validation alone cannot detect.

**Implementation:**
Fetch a second Uthmani Hafs source, apply the same import (byte-preserving) into a separate intermediate, and diff the two. Any diffs must be classified as: (a) known legitimate encoding variance to be documented, or (b) a defect to be resolved before proceeding.

**Acceptance Criteria:**
- Diff report exists and is reviewed.
- All non-trivial diffs have written resolutions.

**Validation:**
Manual review of the diff report by at least two people.

**Output:**
`cross-source-diff-report.md`.

**Blocks:**
TASK-022.

---

## TASK-022 — Package the Quran dataset as a prebuilt SQLite asset

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-021
**Status:** Not Started

**Objective:**
Produce the final read-only `quran.db` bundled with the app.

**Why now:**
This artifact is what the Android app will consume in Phase 4.

**Implementation:**
Pipeline stage that writes `/data/output/quran.db` with tables: `surahs`, `ayahs`, `pages`, `juz`, `hizb`, `rub`, `sajda`. Include indices on `(surah_number, ayah_number_in_surah)`, `page_number`, `juz_number`, and a full-text search table (FTS4/FTS5) for the diacritics-stripped text — actual search index build may happen in TASK-023. Set the DB as read-only. Record schema version.

**Acceptance Criteria:**
- `quran.db` file produced deterministically.
- Row counts match invariants (6236 ayahs, 114 surahs, 604 pages, etc.).
- SHA-256 of `quran.db` recorded in the pipeline manifest.

**Validation:**
Open `quran.db` in a SQLite browser; verify counts and spot-check ayahs.

**Output:**
`/data/output/quran.db`, updated manifest.

**Blocks:**
TASK-023, TASK-055 (Android data source).

---

## TASK-023 — Build the search index with normalized text

**Priority:** High
**Phase:** Phase 1
**Depends on:** TASK-022
**Status:** Not Started

**Objective:**
Add a normalized (diacritics-stripped, Alef/Ya/Ta-Marbuta-normalized) text column and full-text search index, without altering the original `text_uthmani`.

**Why now:**
Search requires a normalized index. It must be built once, at content-freeze time, not at runtime.

**Implementation:**
Add a `search_text` column on `ayahs` populated by a documented normalization function: remove all Arabic diacritics (harakat: U+064B..U+0652, U+0670, U+0640, Quranic marks list documented), normalize Alef variants (أ إ آ → ا), normalize Ya (ى → ي), Ta Marbuta (ة → ه), remove tatweel. Populate FTS5 virtual table with `search_text`. Keep `text_uthmani` untouched.

**Acceptance Criteria:**
- `search_text` present for every ayah.
- FTS search returns expected results for a documented list of test queries (with and without diacritics).
- Original `text_uthmani` unchanged (hash before/after equal for that column).

**Validation:**
Test suite runs a set of ~30 canonical queries and asserts expected ayah global indices are returned.

**Output:**
Updated `quran.db` with search index, query test suite.

**Blocks:**
Phase 8.

---

## TASK-024 — Content Integrity Gate

**Priority:** Critical
**Phase:** Phase 1
**Depends on:** TASK-019, TASK-020, TASK-021, TASK-022, TASK-023
**Status:** Not Started

**Objective:**
Freeze the dataset. Produce a signed, immutable release candidate for content.

**Why now:**
Downstream Android work (Phase 4+) must consume an immutable content artifact. Changing it later invalidates rendering tests.

**Implementation:**
Produce `/data/output/RELEASE/quran-<version>.db` and `quran-<version>.db.sha256`. Publish a `CONTENT_MANIFEST.md` listing: source ADR, pagination ADR, schema version, all invariants, all validation reports, and the SHA-256. Tag the repository at this commit as `content-v1.0.0`. Any change after this point requires a new content version and a re-run of Phase 1 validations.

**Acceptance Criteria:**
- Content manifest exists and is complete.
- Repository tag applied.
- All validation suites pass in CI (once CI exists) or locally with logged output.

**Validation:**
Independent reviewer confirms the manifest against the artifact hash.

**Output:**
`quran-<version>.db`, `.sha256`, `CONTENT_MANIFEST.md`, git tag.

**Blocks:**
TASK-055, TASK-124.

---

# Phase 2 — Typography & Rendering Prototype

Goal: prove that the chosen text-rendering stack can reproduce Quran text beautifully and reliably on real Android devices, **before** committing to full reader development.

## TASK-025 — Select and license the primary Mushaf font

**Priority:** Critical
**Phase:** Phase 2
**Depends on:** TASK-008
**Status:** Not Started

**Objective:**
Choose a Mushaf-quality font whose license permits embedding in a freely-distributed Android app.

**Why now:**
The rendering prototype must use the actual production font, or its findings will not transfer.

**Implementation:**
Evaluate candidates (e.g., KFGQPC Uthmanic Hafs, Amiri Quran, Scheherazade New, Al Mushaf) against: license compatibility with free app distribution, Unicode coverage of every character in the corpus, Mushaf-quality shaping, availability of pixel-density-optimized weights. Document as `docs/adr/0023-font.md`. Archive font files and license under `/assets/fonts-source/`.

**Acceptance Criteria:**
- Font chosen with clear, embeddable license.
- Font file archived with license text and hash.
- License file referenced from `NOTICE`.

**Validation:**
Confirm every character present in the corpus (from TASK-020's Unicode profile) is covered by the font.

**Output:**
ADR-023, archived font, updated NOTICE.

**Blocks:**
TASK-026.

---

## TASK-026 — Build a minimal rendering prototype (throwaway Android module)

**Priority:** Critical
**Phase:** Phase 2
**Depends on:** TASK-009, TASK-024, TASK-025
**Status:** Not Started

**Objective:**
Build a small, isolated Android app that renders full Quran pages using the chosen paging + text rendering approach and the chosen font, loaded from the frozen dataset.

**Why now:**
This is the highest-risk technical bet in the whole project. It must be validated before Phase 3 architecture is finalized.

**Implementation:**
Under `/prototype/rendering/` create a minimal Android app (this may be scaffolded ahead of the main app since it is throwaway). It must: load `quran-v1.0.0.db`, render at least pages 1, 2, 3, 50, 300, 600, 604 using the chosen approach, support horizontal paging, and expose toggles for font size and line spacing. It must run on both a low-end device and a modern device.

**Acceptance Criteria:**
- Prototype builds and runs.
- All 7 target pages render without missing glyphs, tofu boxes, or broken shaping.
- Page turns respond in under 100 ms on the low-end reference device.
- Text is unambiguously RTL, correctly shaped, and matches a reference Madani mushaf image side-by-side.

**Validation:**
Side-by-side comparison with authoritative Madani mushaf page images for pages 1, 2, 50, 300, 604. A knowledgeable reviewer signs off.

**Output:**
Prototype app, screenshots, side-by-side comparison report.

**Blocks:**
TASK-027, TASK-028, TASK-029.

---

## TASK-027 — Stress the prototype: densities, screen sizes, Android versions

**Priority:** Critical
**Phase:** Phase 2
**Depends on:** TASK-026
**Status:** Not Started

**Objective:**
Verify rendering quality and performance across a representative device matrix.

**Why now:**
Rendering that only works on the dev machine is not proof of feasibility.

**Implementation:**
Run the prototype on: a 5" 720p low-end phone (Android minSdk), a 6" 1080p mid-range phone (Android target-1), a 6.7" 1440p flagship (Android target), and an emulator matrix for API levels between minSdk and targetSdk. Capture screenshots of pages 1, 300, 604 on each. Document defects.

**Acceptance Criteria:**
- Screenshot matrix produced.
- No defects rated Critical or High remain unresolved.
- Rendering quality assessed as "Mushaf-acceptable" on every device.

**Validation:**
Reviewer signs off on the matrix. Defects logged in `/prototype/rendering/DEFECTS.md`.

**Output:**
Device screenshot matrix, defect log.

**Blocks:**
TASK-028.

---

## TASK-028 — Long-session rendering stability test

**Priority:** High
**Phase:** Phase 2
**Depends on:** TASK-027
**Status:** Not Started

**Objective:**
Ensure rendering remains stable, memory-bounded, and free of degradation over long sessions.

**Why now:**
Reading sessions of 30+ minutes are common. Leaks or GC storms would be discovered too late in production.

**Implementation:**
On the low-end reference device, run an automated script that swipes through all 604 pages twice, then rapidly ping-pongs between pages 1 and 604 for 20 minutes. Monitor memory, dropped frames, and any thrown exceptions.

**Acceptance Criteria:**
- No exceptions, no ANRs.
- Steady-state memory usage after warmup does not grow monotonically.
- Dropped frames per page turn < 2 on average on the low-end device.

**Validation:**
Automated log with metrics captured. Reviewer signs off.

**Output:**
Long-session test report.

**Blocks:**
TASK-029.

---

## TASK-029 — Rendering acceptance decision and lock

**Priority:** Critical
**Phase:** Phase 2
**Depends on:** TASK-028
**Status:** Not Started

**Objective:**
Confirm that the chosen rendering stack passes acceptance and lock it as the production approach — or explicitly return to TASK-009 for reconsideration.

**Why now:**
Phase 3 cannot begin under uncertainty about the rendering stack.

**Implementation:**
Author `docs/adr/0024-rendering-lock.md` recording the acceptance decision. If the prototype failed acceptance, this task instead documents the failure and prescribes returning to TASK-009 to select an alternative — and the roadmap must not proceed to Phase 3 until acceptance is achieved.

**Acceptance Criteria:**
- ADR-024 merged.
- If accepted: production reader may proceed. If rejected: return-to-ADR-016/017 tasks are scheduled.

**Validation:**
Cross-signed by the technical lead.

**Output:**
ADR-024.

**Blocks:**
Phase 3.

---

# Phase 3 — Android Foundation

Goal: initialize the production Android project, set up modules, build config, static analysis, and CI. No feature code yet.

## TASK-030 — Initialize the Android project

**Priority:** Critical
**Phase:** Phase 3
**Depends on:** TASK-004, TASK-005, TASK-006, TASK-008, TASK-010, TASK-011, TASK-029
**Status:** Not Started

**Objective:**
Create the production Android project under `/android/` per the chosen stack and SDK levels.

**Why now:**
All foundation decisions are locked and the rendering approach is validated.

**Implementation:**
Create a fresh Android project using the language + toolkit from ADR-001/002 and the SDK levels from ADR-003. Configure package name (application ID reservation deferred to Phase 18 but working ID chosen here, e.g., `app.mushaf.reader`). Configure Gradle Kotlin DSL, version catalog `libs.versions.toml`, and Java/Kotlin toolchain.

**Acceptance Criteria:**
- Project builds a debug APK on the reference machine.
- Version catalog in use; no hard-coded dependency versions in module scripts.

**Validation:**
`./gradlew :app:assembleDebug` succeeds cleanly.

**Output:**
`/android/` project skeleton.

**Blocks:**
TASK-031 onward.

---

## TASK-031 — Define module topology

**Priority:** Critical
**Phase:** Phase 3
**Depends on:** TASK-030
**Status:** Not Started

**Objective:**
Split the codebase into modules that mirror the architectural layers.

**Why now:**
Fixing module boundaries early prevents cyclic dependencies and enforces the layer rules from ADR-010..012.

**Implementation:**
Create modules (per ADR-019): `:app` (entrypoint), `:core:designsystem`, `:core:ui`, `:core:common`, `:core:testing`, `:data:content` (Quran DB access), `:data:user` (bookmarks, settings, reading state), `:domain`, `:feature:reader`, `:feature:home`, `:feature:navigation`, `:feature:search`, `:feature:bookmarks`, `:feature:settings`, `:feature:about`. Enforce that `:domain` has zero Android dependencies.

**Acceptance Criteria:**
- All modules exist and build.
- Dependency direction rules enforced at Gradle level (e.g., `:domain` cannot depend on Android).

**Validation:**
`./gradlew build` succeeds; dependency graph inspection confirms rules.

**Output:**
Module structure.

**Blocks:**
TASK-032 onward.

---

## TASK-032 — Configure build variants and signing scaffold

**Priority:** High
**Phase:** Phase 3
**Depends on:** TASK-030
**Status:** Not Started

**Objective:**
Set up `debug`, `staging`, and `release` build variants with distinct application IDs and signing configuration hooks.

**Why now:**
Prevents the app from being installed alongside itself in QA and prepares for Play App Signing.

**Implementation:**
Configure Gradle variants. Debug and staging use suffixes (`.debug`, `.staging`). Signing keys are referenced from `gradle.properties` / environment (never committed). Release variant is unsigned locally; signing happens via Play App Signing.

**Acceptance Criteria:**
- Three variants build.
- No secrets in git.

**Validation:**
`./gradlew assembleDebug assembleStaging assembleRelease` all produce artifacts (release unsigned).

**Output:**
Build variants configured.

**Blocks:**
Phase 17, Phase 18.

---

## TASK-033 — Wire dependency injection scaffold

**Priority:** High
**Phase:** Phase 3
**Depends on:** TASK-031
**Status:** Not Started

**Objective:**
Set up DI (per ADR-008) with empty module bindings for each layer.

**Why now:**
Every feature module will need DI. Establishing it once avoids per-feature retrofits.

**Implementation:**
Add DI framework, define modules `AppModule`, `DataModule`, `DomainModule`, `UiModule`. Provide a single test-friendly composition point.

**Acceptance Criteria:**
- App builds and launches with an empty activity.
- DI graph resolves in a smoke unit test.

**Validation:**
Instrumented smoke test: application starts without exceptions.

**Output:**
DI scaffold.

**Blocks:**
All data / feature tasks.

---

## TASK-034 — Static analysis, formatting, and layer-boundary enforcement

**Priority:** High
**Phase:** Phase 3
**Depends on:** TASK-014, TASK-031
**Status:** Not Started

**Objective:**
Configure ktlint/Spotless, Android Lint baseline, Detekt (or equivalent), and module-boundary rules.

**Why now:**
Cheapest time to introduce these is before any feature code exists.

**Implementation:**
Add tooling to the Gradle build. Configure Detekt rules including cyclomatic complexity, function length, and a custom rule (or convention) preventing cross-layer imports (e.g., `:domain` cannot import `android.*`). Baseline any residual warnings.

**Acceptance Criteria:**
- `./gradlew check` runs all tools and passes.
- Layer violations produce build failures in a targeted test.

**Validation:**
Introduce a deliberate violation in a scratch branch; verify build fails.

**Output:**
Static analysis configuration.

**Blocks:**
TASK-035.

---

## TASK-035 — Configure test infrastructure

**Priority:** High
**Phase:** Phase 3
**Depends on:** TASK-033, TASK-034
**Status:** Not Started

**Objective:**
Set up unit, instrumented, and screenshot test scaffolding across modules.

**Why now:**
Tests are interleaved with implementation from Phase 4 onward.

**Implementation:**
Set up JUnit + Truth/Kotest for unit tests; Robolectric for JVM-side Android tests where needed; Espresso and/or Compose UI testing for instrumented tests; a screenshot test framework (e.g., Paparazzi if using Compose) for rendering tests. Create `:core:testing` with shared fixtures.

**Acceptance Criteria:**
- Sample unit, instrumented, and screenshot tests exist and pass.
- Test tasks run for every module and are wired into `check`.

**Validation:**
CI (once established) or local `./gradlew check connectedCheck` succeeds.

**Output:**
Test scaffold.

**Blocks:**
Phase 4+ testing tasks.

---

## TASK-036 — Set up CI pipeline

**Priority:** High
**Phase:** Phase 3
**Depends on:** TASK-035
**Status:** Not Started

**Objective:**
Automate build, static analysis, and unit tests on every push and pull request.

**Why now:**
Regressions found in CI are cheap; regressions found weeks later are expensive.

**Implementation:**
Configure CI (GitHub Actions / GitLab CI / equivalent) with jobs: `assemble`, `check` (static analysis + unit tests), and a nightly job that also runs instrumented tests on emulators. Cache Gradle. Fail fast on layer-boundary violations.

**Acceptance Criteria:**
- CI runs on every PR.
- CI failures block merges.

**Validation:**
Open a scratch PR with a failing test; verify CI blocks.

**Output:**
CI configuration files.

**Blocks:**
Every subsequent task benefits from this but none are strictly blocked.

---

## TASK-037 — App application class, base theme, and empty entrypoint

**Priority:** Medium
**Phase:** Phase 3
**Depends on:** TASK-033
**Status:** Not Started

**Objective:**
Create a stable, minimal entrypoint that boots the app to an empty placeholder screen.

**Why now:**
Provides a real surface against which Phase 4 wiring can be integration-tested.

**Implementation:**
Create the `Application` subclass, initialize DI, configure a base theme (light-only for now), and set up a placeholder screen that displays app version + dataset hash (both hardcoded stubs at this stage).

**Acceptance Criteria:**
- App launches to placeholder screen on a device.
- No crashes, no leaked warnings in Logcat.

**Validation:**
Manual smoke test on emulator.

**Output:**
Application entrypoint.

**Blocks:**
Phase 4 wiring.

---

# Phase 4 — Core Data & Domain

Goal: implement the layered access to Quran content and user data, without any UI dependency.

## TASK-050 — Bundle the frozen Quran DB as an app asset

**Priority:** Critical
**Phase:** Phase 4
**Depends on:** TASK-024, TASK-037
**Status:** Not Started

**Objective:**
Package `quran-<version>.db` into the app's assets and expose a controlled copy-on-first-launch flow to internal storage.

**Why now:**
The data source cannot open a DB from `assets/` directly (for write-capable operations) and needs a stable path for SQLite.

**Implementation:**
Place `quran-<version>.db` in `assets/content/`. Implement `ContentAssetInstaller` that on first launch (or when dataset version changes) copies the DB from assets to internal storage, verifies its SHA-256 against a bundled expected hash, and refuses to proceed if mismatched. Store dataset version + hash in encrypted-shared or a small DataStore.

**Acceptance Criteria:**
- App launches, DB is copied, hash verified.
- Second launch is fast (no copy).
- Deliberately corrupting the copy on disk triggers a re-install on next launch.

**Validation:**
Unit test the installer with a fake asset + fake destination. Instrumented test verifies real install path.

**Output:**
Asset installer and version manager.

**Blocks:**
TASK-051.

---

## TASK-051 — Read-only content data source (Room over prebuilt DB)

**Priority:** Critical
**Phase:** Phase 4
**Depends on:** TASK-050
**Status:** Not Started

**Objective:**
Expose type-safe access to the Quran content DB via Room's `createFromAsset` (or an equivalent pre-populated DB mechanism).

**Why now:**
All content queries in the app funnel through this source.

**Implementation:**
Define Room entities matching the prebuilt schema. DAOs for: `SurahDao`, `AyahDao`, `PageDao`, `JuzDao`, `HizbDao`, `RubDao`, `SearchDao`. Configure Room with the installed DB, `fallbackToDestructiveMigration = false`, `openHelperFactory` that opens read-only. Verify at startup that DB schema version matches expected.

**Acceptance Criteria:**
- All DAOs compile and expose the expected queries.
- Startup schema check fails fast on mismatch.
- Basic integration test reads Surah 1, verifies 7 ayahs.

**Validation:**
Integration test runs against the bundled DB and asserts known values (Al-Fatiha, Ayat al-Kursi, last ayah of Al-Baqarah).

**Output:**
`:data:content` module with source + DAOs.

**Blocks:**
TASK-052, Phase 5, Phase 6, Phase 8.

---

## TASK-052 — Content repository and domain models

**Priority:** Critical
**Phase:** Phase 4
**Depends on:** TASK-051
**Status:** Not Started

**Objective:**
Wrap DAOs in repositories that expose domain-friendly types (no Room annotations leak beyond `:data:content`).

**Why now:**
The reader engine and navigation depend on domain types, not persistence types.

**Implementation:**
In `:domain` define pure Kotlin models `Surah`, `Ayah`, `Page`, `Juz`, `Hizb`, `Rub`. In `:data:content` implement `ContentRepositoryImpl` that maps entities → domain and exposes: `getPage(pageNumber): Page`, `getSurah(number): Surah`, `getAyahsForPage(pageNumber): List<Ayah>`, `getPageForAyah(ayahGlobalIndex): Int`, `listSurahs()`, `listJuz()`.

**Acceptance Criteria:**
- All queries return domain types.
- No Room type escapes `:data:content`.
- Unit tests cover the mapping.

**Validation:**
Static check (import inspection) that `:domain` and downstream modules never import Room.

**Output:**
Repository + domain models.

**Blocks:**
TASK-053, Phase 5, Phase 6.

---

## TASK-053 — User data persistence (bookmarks, reading state, settings)

**Priority:** Critical
**Phase:** Phase 4
**Depends on:** TASK-037
**Status:** Not Started

**Objective:**
Provide writable local storage for user-generated data, strictly separated from the immutable Quran DB.

**Why now:**
Bookmarks, reading position, and settings all need durable storage. Keeping this separate from `quran.db` protects content integrity.

**Implementation:**
In `:data:user`, create a second Room database `user.db` with tables `bookmarks`, `reading_position`, `settings`. Use DataStore for lightweight primitive prefs (theme, keep-screen-on). Provide repositories `BookmarksRepository`, `ReadingStateRepository`, `SettingsRepository`. Support migrations.

**Acceptance Criteria:**
- User DB created on first use, isolated from Quran DB.
- CRUD operations for bookmarks, get/set for reading position and settings.
- Migration test scaffold present (empty for now, but wired).

**Validation:**
Integration tests on Robolectric or instrumented for each repository.

**Output:**
`:data:user` module.

**Blocks:**
Phase 9, Phase 10.

---

## TASK-054 — Domain use cases

**Priority:** High
**Phase:** Phase 4
**Depends on:** TASK-052, TASK-053
**Status:** Not Started

**Objective:**
Implement thin use cases that encode business rules the UI must not know.

**Why now:**
Prevents leaking business rules into ViewModels.

**Implementation:**
Use cases: `GetPageUseCase`, `GetSurahListUseCase`, `GetJuzListUseCase`, `ResolveReadingPositionUseCase` (returns a Page even when no saved position exists, defaulting to page 1), `SaveReadingPositionUseCase`, `ToggleBookmarkUseCase`, `ListBookmarksUseCase`, `SearchQuranUseCase`. Each is a pure class with clear inputs and outputs.

**Acceptance Criteria:**
- All use cases have unit tests.
- No Android imports in `:domain`.

**Validation:**
Unit tests + import inspection.

**Output:**
Use cases in `:domain`.

**Blocks:**
Phase 5+ ViewModels.

---

## TASK-055 — Content self-check on app start

**Priority:** High
**Phase:** Phase 4
**Depends on:** TASK-050, TASK-051
**Status:** Not Started

**Objective:**
On every cold start, verify the DB hash and structural invariants (114 surahs, 6236 ayahs, 604 pages).

**Why now:**
Detects a corrupted or tampered content DB before the user opens the reader.

**Implementation:**
`ContentIntegrityChecker` runs on start (off the main thread). Records last-check result in DataStore with timestamp. If the check fails, the app enters a safe-mode screen that offers to reinstall the DB from assets.

**Acceptance Criteria:**
- Check runs once per cold start with negligible impact on TTI.
- Deliberate corruption triggers safe-mode.
- Recovery flow reinstalls the DB and returns to normal.

**Validation:**
Instrumented test corrupts the DB and asserts safe-mode is shown.

**Output:**
Integrity checker + safe-mode screen (minimal).

**Blocks:**
Release.

---

# Phase 5 — Reader Engine

Goal: build the beating heart of the app — a smooth, memory-safe, restorable page-based reader.

## TASK-070 — Reader ViewModel and state model

**Priority:** Critical
**Phase:** Phase 5
**Depends on:** TASK-054
**Status:** Not Started

**Objective:**
Define the reader's unidirectional state model: which page is current, adjacent pages, loading, error, and controls-visibility.

**Why now:**
The rendering surface is meaningless without state driving it. State model first, UI second.

**Implementation:**
`ReaderViewModel` exposes `ReaderState { currentPageNumber: Int, isControlsVisible: Boolean, isLoading: Boolean, error: ReaderError? }`. Handles events: `OpenApp`, `GoToPage(n)`, `Next`, `Previous`, `ToggleControls`, `PersistPosition`. Uses `ResolveReadingPositionUseCase` on init.

**Acceptance Criteria:**
- ViewModel unit-tested for every event.
- State survives configuration change via `SavedStateHandle`.

**Validation:**
Unit tests + a Robolectric test simulating rotation.

**Output:**
Reader state model + ViewModel.

**Blocks:**
TASK-071.

---

## TASK-071 — Reader page renderer

**Priority:** Critical
**Phase:** Phase 5
**Depends on:** TASK-029, TASK-052, TASK-070
**Status:** Not Started

**Objective:**
Implement the single-page render composable/view that displays one Mushaf page from a `Page` domain object using the locked rendering approach and the Mushaf font.

**Why now:**
This is the direct production of the prototype work, using the production data path.

**Implementation:**
`MushafPage(page: Page)` renders: page background, Surah header (only when the page starts a Surah), Bismillah treatment (only where appropriate — not before At-Tawbah, and only when a Surah begins on this page), justified 15-line body, ayah-end markers, page number in footer. Font, sizes, and spacings pulled from a `ReaderTypography` object.

**Acceptance Criteria:**
- Renders pages 1, 2, 50, 300, 604 correctly (matches prototype quality).
- No hardcoded strings in the composable; all content sourced from `Page`.
- Screenshot tests capture reference pages.

**Validation:**
Screenshot test suite compares against approved reference images for at least 10 pages.

**Output:**
Single-page renderer + typography config.

**Blocks:**
TASK-072.

---

## TASK-072 — Horizontal paged reader with preloading

**Priority:** Critical
**Phase:** Phase 5
**Depends on:** TASK-071
**Status:** Not Started

**Objective:**
Wrap the page renderer in the chosen paging container so the user can swipe through all 604 pages.

**Why now:**
Reading is inherently multi-page. Preloading of neighbors is required for smooth turns.

**Implementation:**
Paging container preloads N-1 and N+1 pages. Uses the chosen paging component (per ADR-016). Handles RTL swipe direction correctly (in a Mushaf, "next" page in reading order is on the left; the paging must reflect this). Reports the current page to the ViewModel with debounced persistence (see TASK-076).

**Acceptance Criteria:**
- Swipe forward and backward across all 604 pages without missed frames on the low-end reference device.
- Correct RTL swipe semantics.
- Memory usage bounded (no growing retention of off-screen pages).

**Validation:**
Instrumented test swipes 1 → 604 and back; asserts final state and captures memory metrics.

**Output:**
Reader paging container.

**Blocks:**
TASK-073.

---

## TASK-073 — Reader lifecycle: state restoration and process death

**Priority:** Critical
**Phase:** Phase 5
**Depends on:** TASK-072
**Status:** Not Started

**Objective:**
The reader must return the user to the exact page after configuration changes, backgrounding, and process death.

**Why now:**
State loss during reading is a class-A defect.

**Implementation:**
Persist current page in `SavedStateHandle` for configuration changes. Persist to `ReadingStateRepository` (debounced, ~500 ms) for process death survival. On start, `ResolveReadingPositionUseCase` returns the persisted page (or page 1 for a first launch).

**Acceptance Criteria:**
- Rotation preserves the page.
- Kill-from-recents preserves the page.
- Fresh install opens on page 1.

**Validation:**
Instrumented tests for each scenario, including forced process death (`adb shell am kill`).

**Output:**
Restoration logic + tests.

**Blocks:**
Phase 9 depends on this working correctly.

---

## TASK-074 — Reader gesture and control-visibility model

**Priority:** High
**Phase:** Phase 5
**Depends on:** TASK-072
**Status:** Not Started

**Objective:**
Define the minimal, intentional interaction model for the reader.

**Why now:**
Interactions must be defined before controls are drawn, to avoid accidental gesture conflicts.

**Implementation:**
Rules:
- Swipe left/right: next/previous page (RTL-aware).
- Single tap on the center 60% of the page: toggle controls (top and bottom bars).
- Tap on top bar / bottom bar controls: navigation actions (per Phase 6).
- Long-press: **not used in MVP** to avoid accidental triggers.
- Double-tap: **not used in MVP**.
- Back button: hides controls if visible; otherwise navigates back per system convention.

Document as `docs/reader-interactions.md`.

**Acceptance Criteria:**
- Tap zones and behaviors documented.
- Instrumented tests for tap-toggles-controls and swipe-changes-page.

**Validation:**
Manual UX walkthrough + tests.

**Output:**
Interaction model + tests + docs.

**Blocks:**
TASK-075, TASK-090.

---

## TASK-075 — Immersive reading mode

**Priority:** High
**Phase:** Phase 5
**Depends on:** TASK-074
**Status:** Not Started

**Objective:**
When controls are hidden, the reader enters an edge-to-edge, distraction-free presentation.

**Why now:**
Belongs with the reader engine because it's a fundamental aspect of the reading surface.

**Implementation:**
Use edge-to-edge insets. Hide system bars when controls are hidden; show them (translucent) when controls are visible. Respect user's system gesture nav. Optional (via settings, but wired now with a default): keep screen on while reading.

**Acceptance Criteria:**
- Smooth enter/exit transitions.
- No content clipped under system bars.
- Works on both 3-button and gesture nav.

**Validation:**
Manual test on gesture-nav and 3-button-nav devices; screenshot tests.

**Output:**
Immersive mode integration.

**Blocks:**
Phase 15 refinement.

---

## TASK-076 — Debounced reading-position persistence

**Priority:** High
**Phase:** Phase 5
**Depends on:** TASK-073
**Status:** Not Started

**Objective:**
Persist the current page in a way that survives process death without hammering storage.

**Why now:**
Without debouncing, every swipe would write to disk; without persistence, kills lose position.

**Implementation:**
Coalesce writes with a 500 ms window. Always flush on `ON_STOP`. Write includes: page number, timestamp, dataset version.

**Acceptance Criteria:**
- Swipe rapidly through 100 pages; storage receives at most one write per debounce window (and one on stop).
- Kill mid-read; on restart the page is within 1 of the last read page.

**Validation:**
Instrumented test observes writes and simulates kill.

**Output:**
Persistence layer.

**Blocks:**
Phase 9 acceptance.

---

# Phase 6 — Quran Navigation

Goal: implement the navigation surfaces that let the user jump within the Mushaf without leaving the reading context.

## TASK-085 — Surah list screen

**Priority:** Critical
**Phase:** Phase 6
**Depends on:** TASK-054, TASK-070
**Status:** Not Started

**Objective:**
A scrollable, calm list of all 114 Surahs, with Arabic name, translit, revelation place, and ayah count.

**Why now:**
The most common navigation intent besides "continue reading".

**Implementation:**
`SurahListScreen` observes `GetSurahListUseCase`. Tapping a Surah opens the reader at the first page of that Surah. RTL-appropriate list ordering (the number can appear on the right for RTL locales). No decorative imagery.

**Acceptance Criteria:**
- All 114 surahs listed correctly with accurate metadata.
- Tapping a surah opens the correct page.
- Scroll position preserved on rotation and back-navigation.

**Validation:**
Instrumented test taps a random surah and asserts the reader lands on the correct page.

**Output:**
Surah list screen.

**Blocks:**
TASK-100.

---

## TASK-086 — Juz list screen

**Priority:** High
**Phase:** Phase 6
**Depends on:** TASK-054, TASK-070
**Status:** Not Started

**Objective:**
A list of 30 Juz with their first-page numbers and first-surah:ayah references.

**Why now:**
Juz navigation is standard in every Mushaf app and is used for daily reading plans.

**Implementation:**
`JuzListScreen` similar to Surah list. Tapping a Juz opens the reader at the Juz's first page.

**Acceptance Criteria:**
- 30 Juz displayed with correct starting positions.
- Tapping navigates correctly.

**Validation:**
Instrumented test.

**Output:**
Juz list screen.

**Blocks:**
TASK-100.

---

## TASK-087 — Direct page-jump control

**Priority:** High
**Phase:** Phase 6
**Depends on:** TASK-072
**Status:** Not Started

**Objective:**
Allow the user to jump to a specific page number (1..604) from the reader controls.

**Why now:**
Essential for coordinated group reading and revisiting.

**Implementation:**
A minimal dialog or bottom sheet with a numeric input and a subtle validation state. Invalid values are rejected without alarming red UI. On confirm, the reader animates to the target page.

**Acceptance Criteria:**
- Values 1..604 accepted; out-of-range rejected quietly.
- Reader lands on the exact page.

**Validation:**
Instrumented test for valid and invalid inputs.

**Output:**
Page-jump control.

**Blocks:**
—

---

## TASK-088 — "Return to reading" navigation contract

**Priority:** High
**Phase:** Phase 6
**Depends on:** TASK-073, TASK-085, TASK-086
**Status:** Not Started

**Objective:**
Wherever the user goes (surah list, juz list, bookmarks, search), a back navigation should always return them to their current reading page — never lose reading context.

**Why now:**
Preserving reading context is the fundamental UX contract of the app.

**Implementation:**
Every non-reader screen keeps the reader on the back stack. Navigating to a specific page (from surah list, etc.) updates the reader's target page but does not push a new reader instance. Deep-links resolve to the reader with the requested page.

**Acceptance Criteria:**
- Back from any non-reader screen returns to the reader on the last read page.
- Explicit jumps update the reader's current page.

**Validation:**
Instrumented tests for each navigation path.

**Output:**
Navigation contract + tests.

**Blocks:**
Phase 8, Phase 9 UI.

---

# Phase 7 — Home Experience

Goal: a home surface that gently returns the user to the Quran.

## TASK-100 — Minimal home screen

**Priority:** Critical
**Phase:** Phase 7
**Depends on:** TASK-085, TASK-086, TASK-088
**Status:** Not Started

**Objective:**
A calm home surface with a large, primary "Continue reading" action, and quiet secondary access to Surahs, Juz, Search, Bookmarks.

**Why now:**
The home screen is what the user sees before they enter the reader (only if they don't skip it — see TASK-101).

**Implementation:**
Simple vertical layout: large "Continue reading — Page N, Surah X" card at the top (or a whole-hero button); below, small entries for Surahs, Juz, Search, Bookmarks, About. No banners, no illustrations, no counters, no promotional surfaces.

**Acceptance Criteria:**
- Continue-reading shows the correct page and surah for the persisted position.
- All secondary destinations open correctly.

**Validation:**
Instrumented test taps continue-reading and lands on the correct page.

**Output:**
Home screen.

**Blocks:**
TASK-101.

---

## TASK-101 — "Open directly to Quran" launch behavior

**Priority:** Critical
**Phase:** Phase 7
**Depends on:** TASK-073, TASK-100
**Status:** Not Started

**Objective:**
Launching the app opens the Reader on the last-read page directly. The home screen is one back-press away, not the default surface.

**Why now:**
This is the core promise: "open the app → return to the Quran."

**Implementation:**
Default start destination = Reader. Home is reachable from a reader control (e.g., the top bar). First-ever launch: open the Reader at page 1 (no onboarding, no splash beyond the standard Android 12+ splash).

**Acceptance Criteria:**
- Fresh install: launching goes straight to page 1 in the Reader.
- Returning users: launching goes straight to their last-read page.
- Home is reachable and functional but is not the default.

**Validation:**
Instrumented tests for both fresh-install and returning-user scenarios.

**Output:**
Launch-flow wiring.

**Blocks:**
Phase 15 refinement.

---

# Phase 8 — Search

Goal: offline Arabic search that is accurate and fast.

## TASK-115 — Search query normalization

**Priority:** Critical
**Phase:** Phase 8
**Depends on:** TASK-023
**Status:** Not Started

**Objective:**
Client-side query normalization must mirror the index-time normalization exactly.

**Why now:**
Any mismatch between query normalization and index normalization silently degrades results.

**Implementation:**
Implement `NormalizeSearchQuery` in `:domain` that applies the exact same transformations as TASK-023: strip diacritics, normalize Alef variants, normalize Ya, Ta Marbuta, remove tatweel. Cover with unit tests using the same corpus of test queries.

**Acceptance Criteria:**
- Identical output to the index-time normalizer on a shared test corpus.
- Empty and whitespace-only queries handled cleanly.

**Validation:**
Shared unit-test corpus of ~50 (input, expected-normalized) pairs.

**Output:**
Normalizer + shared tests.

**Blocks:**
TASK-116.

---

## TASK-116 — Search use case and ranking

**Priority:** Critical
**Phase:** Phase 8
**Depends on:** TASK-115, TASK-051
**Status:** Not Started

**Objective:**
Execute FTS queries against the Quran DB and rank results.

**Why now:**
Ranking must be defined once so behavior is consistent across UI iterations.

**Implementation:**
`SearchQuranUseCase(query: String): List<SearchHit>` where `SearchHit { ayah: Ayah, snippet: String, matchPositions: List<IntRange> }`. Ranking: FTS bm25 rank + surah/ayah tiebreak. Snippet is the untouched `text_uthmani` with match positions computed by re-normalizing and locating substrings.

**Acceptance Criteria:**
- Fixed benchmark queries return expected top hits.
- Snippets contain the original diacritized text, not the normalized text.

**Validation:**
Benchmark test set of ~30 queries with expected top-3 results.

**Output:**
Search use case + tests.

**Blocks:**
TASK-117.

---

## TASK-117 — Search screen and interaction

**Priority:** High
**Phase:** Phase 8
**Depends on:** TASK-116, TASK-088
**Status:** Not Started

**Objective:**
A calm search UI with a single input, quiet empty state, and tappable results.

**Why now:**
The last step to make search usable.

**Implementation:**
Debounced input (~300 ms). Empty query → simple hint. No results → quiet message, not alarming. Tapping a result opens the reader at the ayah's page (highlighting of the specific ayah is deferred to POST-MVP unless trivially achievable). Long queries handled without UI jank.

**Acceptance Criteria:**
- Common queries return results in under 200 ms on the low-end device.
- No jank while typing.
- Result tap navigates correctly.

**Validation:**
Instrumented tests + performance measurement.

**Output:**
Search screen.

**Blocks:**
—

---

# Phase 9 — Bookmarks & Reading State

Goal: reliable bookmarks and reading position, with clear scope semantics.

## TASK-130 — Bookmark semantics decision

**Priority:** Critical
**Phase:** Phase 9
**Depends on:** TASK-053
**Status:** Not Started

**Objective:**
Explicitly decide whether MVP bookmarks are page-scoped, ayah-scoped, or both.

**Why now:**
The data model, UI, and interactions all pivot on this choice.

**Implementation:**
Author `docs/adr/0025-bookmark-scope.md`. Recommend **page-scoped bookmarks** for MVP (simpler UI, matches physical Mushaf ribbon), with ayah-scoped bookmarks deferred to POST-MVP. Data model retains a nullable `ayah_global_index` to allow forward compatibility without migration.

**Acceptance Criteria:**
- ADR-025 merged.
- Data model reflects the choice.

**Validation:**
Data model reviewed for forward compatibility.

**Output:**
ADR-025.

**Blocks:**
TASK-131.

---

## TASK-131 — Bookmark CRUD and duplicate handling

**Priority:** High
**Phase:** Phase 9
**Depends on:** TASK-130
**Status:** Not Started

**Objective:**
Implement add / remove / list / jump-to bookmark operations.

**Why now:**
Foundation for the bookmarks screen and reader control.

**Implementation:**
Repository operations: `add(pageNumber)`, `remove(pageNumber)`, `list(): Flow<List<Bookmark>>`, `isBookmarked(pageNumber): Flow<Boolean>`. Adding an already-bookmarked page is a no-op. Storage sorted by created-at descending by default.

**Acceptance Criteria:**
- All operations pass repository tests.
- Duplicate adds do not create duplicate rows.

**Validation:**
Instrumented repository tests.

**Output:**
Bookmark repository logic.

**Blocks:**
TASK-132.

---

## TASK-132 — Bookmark UI: reader affordance and bookmarks screen

**Priority:** High
**Phase:** Phase 9
**Depends on:** TASK-131, TASK-072, TASK-088
**Status:** Not Started

**Objective:**
A discreet bookmark toggle on the reader (visible only when controls are shown) and a clean bookmarks list.

**Why now:**
Depends on the reader controls and navigation contract.

**Implementation:**
Reader top-bar affordance shows a filled/outlined bookmark icon reflecting current-page state. Bookmarks screen lists page number + surah context + saved date. Tap → open reader at that page (via the navigation contract). Swipe to delete with an undo snackbar.

**Acceptance Criteria:**
- Toggle reflects state accurately across page swipes.
- List updates in real time on add/remove.

**Validation:**
Instrumented tests for toggle, list update, and delete-undo.

**Output:**
Bookmark UI.

**Blocks:**
—

---

## TASK-133 — Reading position edge cases

**Priority:** Critical
**Phase:** Phase 9
**Depends on:** TASK-076, TASK-055
**Status:** Not Started

**Objective:**
Guarantee reading-position correctness in all lifecycle and data-migration scenarios.

**Why now:**
This is the second most-loved feature after page reading itself. It must be bulletproof.

**Implementation:**
Test matrix for reading position:
- Fresh install → page 1.
- Normal exit → resumes.
- Force stop (`adb shell am force-stop`) → resumes.
- Battery kill / OOM → resumes.
- Rotation → resumes.
- Split-screen entry → resumes.
- Backup/restore reset → resumes at page 1 (data cleared).
- Dataset version bump with same page semantics → resumes at same page.
- Dataset schema bump with different page semantics → fall back to page 1, log event.

Codify each with an automated test where feasible.

**Acceptance Criteria:**
- All test-matrix scenarios pass.
- Documented behavior for each edge case.

**Validation:**
Automated + manual matrix.

**Output:**
Test suite + edge-case documentation.

**Blocks:**
Phase 14.

---

# Phase 10 — Settings

Goal: a small, deliberate set of settings that each justify their existence.

## TASK-145 — Settings screen scaffold and theme setting

**Priority:** High
**Phase:** Phase 10
**Depends on:** TASK-053
**Status:** Not Started

**Objective:**
A single settings screen with theme control (Light / Dark / Follow System).

**Why now:**
Theme is the highest-value setting; the scaffold is trivially extensible.

**Implementation:**
`SettingsScreen` with grouped list. Theme selection persists to DataStore. App-wide theme observes DataStore and applies via the design system.

**Acceptance Criteria:**
- Theme change is instant and persists across restart.
- Follow-system reacts to OS theme changes without app restart.

**Validation:**
Instrumented tests for persistence and reaction to system-theme change.

**Output:**
Settings scaffold + theme.

**Blocks:**
TASK-146.

---

## TASK-146 — Reading settings (font size, keep screen on)

**Priority:** High
**Phase:** Phase 10
**Depends on:** TASK-145, TASK-071
**Status:** Not Started

**Objective:**
Minimal reading-related preferences.

**Why now:**
Every long-session reader wants at least text scale and keep-screen-on.

**Implementation:**
Options: Reading Text Size (Small / Medium / Large — 3 discrete steps, chosen to preserve line breaks per Mushaf page), Keep Screen On (on/off). Font size steps are constrained so that the 15-line Mushaf page layout is not broken. Settings apply live to the reader.

**Acceptance Criteria:**
- Changing size does not break page layout.
- Keep-screen-on flag is set/cleared correctly on reader entry/exit.

**Validation:**
Instrumented tests + screenshot tests for each size.

**Output:**
Reading settings.

**Blocks:**
Phase 15.

---

## TASK-147 — About screen

**Priority:** Medium
**Phase:** Phase 10
**Depends on:** TASK-145
**Status:** Not Started

**Objective:**
Show app version, dataset version and hash, credits, font license, source attributions, and privacy statement.

**Why now:**
Required for release; small enough to slot in here.

**Implementation:**
Static content assembled at build time (versions injected from Gradle; dataset hash read from the manifest).

**Acceptance Criteria:**
- All attributions present.
- Privacy statement is present and truthful.

**Validation:**
Manual review + a snapshot test.

**Output:**
About screen.

**Blocks:**
Phase 18.

---

# Phase 11 — Accessibility

Goal: baseline accessibility that improves usability without compromising the Mushaf experience.

## TASK-160 — Semantics and TalkBack labels

**Priority:** High
**Phase:** Phase 11
**Depends on:** TASK-100, TASK-085, TASK-086, TASK-117, TASK-132
**Status:** Not Started

**Objective:**
Every actionable element has a meaningful, localized content description.

**Why now:**
Requires all screens to exist first.

**Implementation:**
Audit every interactive element. Add labels: "Continue reading page 42, Surah Al-Anfal", "Bookmark this page", "Open Surah list", "Search the Quran", etc. Reader page itself exposes a semantic label with page number and Surah context (but the ayah text is not read aloud by TalkBack in MVP; announcing full Arabic verses via TTS is outside MVP scope and would misspeak the text).

**Acceptance Criteria:**
- Accessibility scanner reports zero critical issues.
- Manual TalkBack walk of all screens succeeds.

**Validation:**
Google Accessibility Scanner + manual TalkBack test.

**Output:**
Content descriptions across the app.

**Blocks:**
TASK-161.

---

## TASK-161 — Touch targets, contrast, and font scaling

**Priority:** High
**Phase:** Phase 11
**Depends on:** TASK-160
**Status:** Not Started

**Objective:**
Meet WCAG-inspired baselines for touch target size (≥ 48 dp), color contrast, and system font scaling for UI chrome (Mushaf text scale is user-controlled via TASK-146, not tied to OS font scale).

**Why now:**
These are cross-screen concerns best fixed in a single pass after all UI exists.

**Implementation:**
Audit and fix touch targets. Verify light and dark themes meet contrast targets for all UI chrome. Ensure UI chrome respects `fontScale` up to 1.3x without breaking layouts (Mushaf text does NOT respond to OS fontScale to keep page layout stable; this is documented and intentional).

**Acceptance Criteria:**
- Automated scanner passes for touch targets and contrast.
- Manual test at fontScale 1.3x shows no chrome breakage.

**Validation:**
Scanner + manual.

**Output:**
Fixes across UI.

**Blocks:**
Phase 14.

---

## TASK-162 — Reduced motion and RTL correctness

**Priority:** Medium
**Phase:** Phase 11
**Depends on:** TASK-161
**Status:** Not Started

**Objective:**
Respect system "remove animations" preference. Verify RTL layout correctness on Arabic system locale.

**Why now:**
Small but real accessibility wins.

**Implementation:**
Query system animator scale. If ≈ 0, disable non-essential animations (page transitions become instant). Set the whole app locale-aware for RTL and verify layouts under Arabic system locale.

**Acceptance Criteria:**
- With animations disabled, no essential animations remain.
- Under Arabic locale, all screens render correctly RTL.

**Validation:**
Manual test on device with both settings.

**Output:**
RTL and motion fixes.

**Blocks:**
Phase 14.

---

# Phase 12 — Performance Engineering

Goal: measurable, budgeted performance on real low-end hardware.

## TASK-175 — Define performance budgets

**Priority:** High
**Phase:** Phase 12
**Depends on:** TASK-072
**Status:** Not Started

**Objective:**
Set explicit budgets that the app must not exceed.

**Why now:**
Optimization without budgets is unbounded.

**Implementation:**
Author `docs/performance-budgets.md`:
- Cold start (low-end device) → time to first reader frame: ≤ 1500 ms.
- Warm start: ≤ 500 ms.
- Page turn latency (finger lift → next page committed): ≤ 100 ms.
- Search query (typical): ≤ 200 ms.
- Steady-state reader memory: ≤ 120 MB.
- APK/AAB install size: ≤ 30 MB (adjust based on ADR-014/015 outcomes).

**Acceptance Criteria:**
- Budgets documented and referenced by test tasks.

**Validation:**
Reviewed by tech lead.

**Output:**
`docs/performance-budgets.md`.

**Blocks:**
TASK-176.

---

## TASK-176 — Startup optimization

**Priority:** High
**Phase:** Phase 12
**Depends on:** TASK-175
**Status:** Not Started

**Objective:**
Meet the cold-start budget on the reference low-end device.

**Why now:**
Startup dictates first impression and is hardest to fix late.

**Implementation:**
Use Android App Startup for lightweight initialization. Move dataset install + integrity check off the main thread; show the reader immediately if the DB is already installed. Baseline Profiles (if using Compose) to warm up hot paths.

**Acceptance Criteria:**
- Cold start under budget on the reference device.
- Warm start under budget.

**Validation:**
Macrobenchmark or trace-based measurement recorded in `startup-metrics.md`.

**Output:**
Startup optimizations, metrics.

**Blocks:**
TASK-177.

---

## TASK-177 — Reader frame timing and jank hunt

**Priority:** High
**Phase:** Phase 12
**Depends on:** TASK-176
**Status:** Not Started

**Objective:**
Ensure page-turn transitions and general reader interaction hit their budgets.

**Why now:**
Jank in the reader ruins the entire premise.

**Implementation:**
Use Android GPU rendering profile, FrameTiming API, and Perfetto traces. Fix identified hitches (over-recomposition, text measurement on main, off-thread bitmap decodes, etc.).

**Acceptance Criteria:**
- No dropped frames > 16 ms during steady swiping on the reference device.
- Page turn latency under budget.

**Validation:**
Trace files archived in `/perf/`.

**Output:**
Fixes + traces.

**Blocks:**
TASK-178.

---

## TASK-178 — Search performance and index warmup

**Priority:** Medium
**Phase:** Phase 12
**Depends on:** TASK-117
**Status:** Not Started

**Objective:**
Search meets its latency budget across a benchmark set.

**Implementation:**
Warm FTS on first search open. Cache last N query results. Ensure snippet generation is off-main.

**Acceptance Criteria:**
- Benchmark queries within budget.

**Validation:**
Benchmark suite runs and is archived.

**Output:**
Search perf improvements.

**Blocks:**
—

---

## TASK-179 — APK/AAB size budget

**Priority:** Medium
**Phase:** Phase 12
**Depends on:** TASK-050, TASK-025
**Status:** Not Started

**Objective:**
Meet the install size budget through resource shrinking, R8, and asset audit.

**Implementation:**
Enable R8 full mode + resource shrinking in release. Ensure only the primary font is bundled. Confirm the DB is compressed only when it actually reduces install size. Use Android App Bundle to split by ABI/density.

**Acceptance Criteria:**
- Final AAB is at or below budget.

**Validation:**
`bundletool` measurement archived.

**Output:**
Size-optimized release build config.

**Blocks:**
Phase 17.

---

# Phase 13 — Security & Privacy

Goal: verify the app's privacy-first stance is honored end-to-end.

## TASK-190 — Permission and manifest audit

**Priority:** Critical
**Phase:** Phase 13
**Depends on:** TASK-037
**Status:** Not Started

**Objective:**
The AndroidManifest declares only permissions actually used.

**Why now:**
Every unnecessary permission is a privacy claim we cannot honor.

**Implementation:**
Audit and remove any permissions not required by MVP features. MVP should require **no runtime permissions**. Explicitly declare no INTERNET usage if truly not needed (or justify each network call).

**Acceptance Criteria:**
- Manifest declares only justified permissions.
- Justifications logged in `docs/permissions.md`.

**Validation:**
Manual manifest review.

**Output:**
Cleaned manifest + docs.

**Blocks:**
Phase 18.

---

## TASK-191 — Third-party dependency review

**Priority:** Critical
**Phase:** Phase 13
**Depends on:** TASK-030
**Status:** Not Started

**Objective:**
No ad SDK, no analytics SDK, no tracking SDK. Every dependency justified.

**Why now:**
Dependencies accumulate. A periodic and pre-release audit is essential.

**Implementation:**
List all runtime dependencies with license and purpose. Reject any dependency that transitively pulls in tracking. Document in `docs/dependencies.md`.

**Acceptance Criteria:**
- No ad/tracking/analytics dependencies.
- All licenses recorded in `NOTICE`.

**Validation:**
Dependency tree inspection + manual review.

**Output:**
Dependency register.

**Blocks:**
Phase 18.

---

## TASK-192 — Privacy statement and data safety draft

**Priority:** High
**Phase:** Phase 13
**Depends on:** TASK-190, TASK-191
**Status:** Not Started

**Objective:**
Draft an accurate privacy policy and Google Play Data Safety declaration.

**Why now:**
These are pre-release deliverables that must reflect the audited reality of the app.

**Implementation:**
`docs/privacy-policy.md`: state that the app collects no personal data, requests no permissions, has no network calls (or lists them exhaustively), stores only local reading state. Data Safety form draft prepared for Phase 18 submission.

**Acceptance Criteria:**
- Policy is truthful and complete.
- Data Safety draft is consistent with the policy.

**Validation:**
Peer legal-style review.

**Output:**
Privacy policy + data safety draft.

**Blocks:**
Phase 18.

---

# Phase 14 — Full QA

Goal: run an exhaustive QA pass across functional, edge-case, and regression scenarios.

## TASK-205 — Functional regression suite

**Priority:** Critical
**Phase:** Phase 14
**Depends on:** All feature tasks (Phases 5–10)
**Status:** Not Started

**Objective:**
Every user-facing feature has an automated end-to-end test.

**Why now:**
The full app now exists; automate the golden paths.

**Implementation:**
Instrumented tests for: launch-to-last-read-page, page swipe forward/back, surah navigation, juz navigation, direct page jump, search + tap result, add/remove bookmark, jump to bookmark, theme switch, reading size change, keep-screen-on toggle.

**Acceptance Criteria:**
- All tests pass on the device matrix (minSdk emulator, targetSdk device).

**Validation:**
CI run on scheduled nightly matrix.

**Output:**
End-to-end suite.

**Blocks:**
Phase 17.

---

## TASK-206 — Edge-case suite

**Priority:** Critical
**Phase:** Phase 14
**Depends on:** TASK-205
**Status:** Not Started

**Objective:**
Cover the edge cases enumerated in the roadmap.

**Why now:**
Edge cases are the difference between "works on my machine" and "works in production".

**Implementation:**
Cases:
- First launch with no prior state.
- Corrupted local user DB → recover gracefully.
- Missing content DB (asset stripped) → safe-mode + reinstall.
- Invalid saved page number > 604 → clamp to valid range, log event.
- Bookmark referencing an out-of-range page → filter out with a log.
- Search with empty query → hint state.
- Search with non-Arabic characters → normalize best-effort, empty result if no match.
- Search with a single-character query → require ≥ 2 characters (documented).
- Rapid navigation (Surah list → Reader → Back → Search → Reader) → no crashes.
- App killed mid-persist → next launch recovers cleanly.
- App killed mid-page-change → next launch shows the last committed page.
- Device reboot → next launch resumes correctly.
- Extremely small display (≤ 4.5", low DPI) → layout still legible or gracefully degrades.
- Extremely large display (7"+, tablets) → layout not visually broken (full tablet-optimized layout is POST-MVP; MVP just avoids catastrophe).
- Every supported Android API level → smoke run.

**Acceptance Criteria:**
- Every case has an automated test or a documented manual test with a signed-off result.

**Validation:**
Automated where possible; manual matrix logged in `qa/edge-cases.md`.

**Output:**
Edge-case suite + log.

**Blocks:**
Phase 17.

---

## TASK-207 — Stability soak tests

**Priority:** Critical
**Phase:** Phase 14
**Depends on:** TASK-205
**Status:** Not Started

**Objective:**
Extended, adversarial use of the app must not produce crashes or leaks.

**Why now:**
Stability under real usage cannot be trusted without soak testing.

**Implementation:**
Scripts:
- 30-minute continuous swiping (forward and back).
- 15-minute rapid navigation between reader, surah list, search, bookmarks.
- 10 forced process kills interleaved with normal use.
- 100 bookmark toggles across random pages.

Monitor memory, CPU, and crash count.

**Acceptance Criteria:**
- Zero crashes.
- Memory returns to baseline after each scenario.
- No ANRs.

**Validation:**
Automated runs archived under `/qa/soak/`.

**Output:**
Soak test scripts + results.

**Blocks:**
Phase 17.

---

## TASK-208 — Content QA final sweep

**Priority:** Critical
**Phase:** Phase 14
**Depends on:** TASK-024, TASK-071
**Status:** Not Started

**Objective:**
Human verification of rendered content on random and structural boundary pages.

**Why now:**
The final human eye on rendered Quran text before release.

**Implementation:**
Verify against a printed or authoritative digital Madani mushaf:
- Al-Fatiha in full.
- Ayat al-Kursi (2:255) — high-profile verse, must be flawless.
- Last three surahs.
- Random 20 pages spread across the mushaf.
- The first and last page of each Juz.
- The first page after each Surah heading (Bismillah rendering).

Log each verification with a screenshot and reviewer name.

**Acceptance Criteria:**
- All checks signed off.
- Any defect discovered here is release-blocking and reopens Phase 1 or Phase 2 as needed.

**Validation:**
Human reviewer sign-off in `qa/content-final-sweep.md`.

**Output:**
Sweep report.

**Blocks:**
Phase 17.

---

# Phase 15 — Visual Refinement

Goal: only now, with correctness proven, refine the visual language.

## TASK-220 — Typography polish pass

**Priority:** High
**Phase:** Phase 15
**Depends on:** TASK-208
**Status:** Not Started

**Objective:**
Perfect line-height, letter-spacing, margins, and header treatment for reading comfort.

**Why now:**
Only after content correctness is proven can typography be polished without hiding defects.

**Implementation:**
Iterate on Mushaf page composition: margins tuned to preserve balance; page number and surah header treated with restraint; Bismillah styled with proportion, not decoration.

**Acceptance Criteria:**
- Side-by-side with a physical Mushaf, an informed reader signs off.

**Validation:**
Reviewer sign-off; screenshot references archived.

**Output:**
Typography polish.

**Blocks:**
TASK-221.

---

## TASK-221 — Dark theme refinement

**Priority:** High
**Phase:** Phase 15
**Depends on:** TASK-220
**Status:** Not Started

**Objective:**
Ensure dark theme is warm and calm, not stark; text remains crisp; contrast is comfortable for long reading in low light.

**Why now:**
Dark theme is often afterthought; here it deserves a first-class pass.

**Implementation:**
Warm off-black background (not pure #000, not gray-white text — chosen to reduce halo/artifacting on Arabic glyphs). Adjust font weight if needed for dark rendering. Verify OLED behavior.

**Acceptance Criteria:**
- Screenshot approval on both LCD and OLED devices.
- Contrast still passes accessibility.

**Validation:**
Manual review on device.

**Output:**
Dark theme polish.

**Blocks:**
TASK-222.

---

## TASK-222 — Chrome polish (icons, spacing, transitions)

**Priority:** Medium
**Phase:** Phase 15
**Depends on:** TASK-221
**Status:** Not Started

**Objective:**
Polish icons, spacing, and non-reader animations to feel calm and coherent.

**Why now:**
Chrome polish belongs last so nothing is polished twice.

**Implementation:**
Adopt a single, thin icon set (custom or curated); harmonize spacing tokens; keep animations subtle (fade + minor slide, never bouncy).

**Acceptance Criteria:**
- Design review sign-off.

**Validation:**
Reviewer sign-off.

**Output:**
Chrome polish.

**Blocks:**
TASK-223.

---

## TASK-223 — Empty, loading, and error state pass

**Priority:** Medium
**Phase:** Phase 15
**Depends on:** TASK-222
**Status:** Not Started

**Objective:**
Every screen has a considered empty, loading, and error state.

**Why now:**
These states exist regardless; making them coherent matters.

**Implementation:**
Bookmarks-empty: a quiet line of prose. Search-empty query: a hint. Search-no-results: a calm message. Loading: unobtrusive spinner or skeleton, never flashy. Error: recoverable, non-alarming.

**Acceptance Criteria:**
- Every screen state screenshotted and reviewed.

**Validation:**
Reviewer sign-off.

**Output:**
State polish.

**Blocks:**
Phase 16.

---

# Phase 16 — Real Device Validation

Goal: independent validation on a real device matrix.

## TASK-235 — Device matrix run

**Priority:** Critical
**Phase:** Phase 16
**Depends on:** TASK-207, TASK-223
**Status:** Not Started

**Objective:**
Install and manually validate the app on a matrix of real devices.

**Why now:**
Emulators lie. Real GPU/CPU, real fonts, real OEM tweaks reveal issues that CI misses.

**Implementation:**
Matrix (target coverage):
- Low-end: e.g., Android Go device or a 3–4 year old budget phone at minSdk or minSdk+1.
- Mid-range: current mainstream device at targetSdk-1.
- Flagship: current flagship at targetSdk.
- One tablet (only for smoke, since tablet layout is POST-MVP).

Per device: complete the QA script (launch, read 20 pages, navigate via Surah/Juz/Search, add/remove a bookmark, force kill and resume, toggle theme).

**Acceptance Criteria:**
- Every device passes the QA script.
- Any device-specific defect is triaged and fixed or scheduled.

**Validation:**
Sign-off checklist in `qa/device-matrix.md`.

**Output:**
Device matrix report.

**Blocks:**
Phase 17.

---

## TASK-236 — Accessibility validation on real devices

**Priority:** High
**Phase:** Phase 16
**Depends on:** TASK-235
**Status:** Not Started

**Objective:**
Run TalkBack, switch access, and large-text scenarios on physical devices.

**Why now:**
Screen-reader behavior differs across OEMs.

**Implementation:**
Manual pass on at least two devices; log any labels that read poorly and fix.

**Acceptance Criteria:**
- Zero critical accessibility issues.

**Validation:**
Report archived.

**Output:**
Accessibility validation report.

**Blocks:**
Phase 17.

---

# Phase 17 — Release Candidate

Goal: freeze, verify, and produce the release AAB.

## TASK-250 — Feature freeze

**Priority:** Critical
**Phase:** Phase 17
**Depends on:** TASK-235, TASK-236, TASK-208
**Status:** Not Started

**Objective:**
Declare a feature freeze. No new features until v1.1.

**Why now:**
Stabilization requires no new movement.

**Implementation:**
Tag repository `rc-freeze-vX.Y.0`. Any changes after this point must be classified as: (a) release-blocker bugfix, (b) documentation, (c) content correction (must re-run Phase 1 gates).

**Acceptance Criteria:**
- Repository tagged.
- Freeze rules communicated.

**Validation:**
Only allowed change classes appear in the log after the tag.

**Output:**
RC freeze tag.

**Blocks:**
TASK-251.

---

## TASK-251 — Full RC test suite run

**Priority:** Critical
**Phase:** Phase 17
**Depends on:** TASK-250
**Status:** Not Started

**Objective:**
Run every automated suite and every critical manual test one final time against the RC build.

**Implementation:**
Automated: unit + instrumented + soak + benchmark + screenshot. Manual: content sweep abridged rerun, device matrix abridged rerun, accessibility abridged rerun.

**Acceptance Criteria:**
- All suites pass.
- No release-blocker issues remain.

**Validation:**
Test log archived.

**Output:**
RC test report.

**Blocks:**
TASK-252.

---

## TASK-252 — Build, sign, and verify release AAB

**Priority:** Critical
**Phase:** Phase 17
**Depends on:** TASK-251, TASK-179, TASK-032
**Status:** Not Started

**Objective:**
Produce the release AAB, verify Play App Signing config, and validate installability.

**Implementation:**
Build release AAB. Use `bundletool` to generate an APK set and install on the device matrix. Run smoke script. Verify offline: enable airplane mode, cold-launch, verify reader works.

**Acceptance Criteria:**
- AAB builds and installs on the device matrix.
- Offline smoke passes.
- Upgrade install from a prior debug build preserves reading position and bookmarks (or documents the migration behavior clearly).

**Validation:**
Manual install matrix + logged results.

**Output:**
Release AAB + verification log.

**Blocks:**
Phase 18.

---

# Phase 18 — Google Play Launch

Goal: submit and release to Google Play with a controlled rollout.

## TASK-265 — Application ID reservation and Play Console setup

**Priority:** Critical
**Phase:** Phase 18
**Depends on:** TASK-011
**Status:** Not Started

**Objective:**
Reserve the final application ID and configure the Play Console entry.

**Why now:**
Play Console must be set up before uploads.

**Implementation:**
Create the Play Console app with the final application ID (locked here — must match `applicationId` in Gradle). Configure Play App Signing.

**Acceptance Criteria:**
- App listing exists.
- Play App Signing configured.

**Validation:**
Confirmed via Play Console.

**Output:**
Play Console app entry.

**Blocks:**
TASK-266.

---

## TASK-266 — Store listing assets

**Priority:** High
**Phase:** Phase 18
**Depends on:** TASK-265, TASK-223
**Status:** Not Started

**Objective:**
Produce app icon, feature graphic, screenshots (phone), short and long descriptions, and localized listings for at least English and Arabic.

**Implementation:**
Icon designed with restraint (nothing kitsch). Screenshots taken on real devices in both themes. Descriptions state the app's purpose truthfully and emphasize privacy-first, offline, ad-free.

**Acceptance Criteria:**
- All required assets uploaded.
- Descriptions in EN and AR.

**Validation:**
Preview in Play Console.

**Output:**
Store listing.

**Blocks:**
TASK-269.

---

## TASK-267 — Data safety declaration

**Priority:** Critical
**Phase:** Phase 18
**Depends on:** TASK-192, TASK-265
**Status:** Not Started

**Objective:**
Complete the Data Safety form truthfully.

**Implementation:**
Submit form based on the audit in TASK-190/191. Declare: no data collected, no data shared, all data processed on-device.

**Acceptance Criteria:**
- Data safety accepted by Play Console.

**Validation:**
Play Console confirmation.

**Output:**
Data safety submission.

**Blocks:**
TASK-269.

---

## TASK-268 — Content rating, target audience, and compliance forms

**Priority:** Critical
**Phase:** Phase 18
**Depends on:** TASK-265
**Status:** Not Started

**Objective:**
Complete content rating, target-audience, and any current Google Play compliance questionnaires as they exist at submission time.

**Why now:**
Play requirements change; verify current requirements at submission.

**Implementation:**
Verify current requirements against Google Play policy. Complete each form. Provide a religious/reference content declaration.

**Acceptance Criteria:**
- All forms complete and accepted.

**Validation:**
Play Console confirmation.

**Output:**
Completed forms.

**Blocks:**
TASK-269.

---

## TASK-269 — Internal testing release

**Priority:** Critical
**Phase:** Phase 18
**Depends on:** TASK-252, TASK-266, TASK-267, TASK-268
**Status:** Not Started

**Objective:**
Upload the RC AAB to the Internal Testing track and validate with a small tester group.

**Implementation:**
Upload AAB, add testers, distribute link, run smoke script on installed builds.

**Acceptance Criteria:**
- Testers install and confirm baseline functionality.

**Validation:**
Tester feedback logged.

**Output:**
Internal testing release.

**Blocks:**
TASK-270.

---

## TASK-270 — Closed testing (optional but recommended)

**Priority:** Medium
**Phase:** Phase 18
**Depends on:** TASK-269
**Status:** Not Started

**Objective:**
Broaden testing to a slightly larger closed group.

**Implementation:**
Promote the internal AAB to closed track. Collect feedback for 5–7 days.

**Acceptance Criteria:**
- No new critical issues surfaced.

**Validation:**
Feedback log.

**Output:**
Closed track release.

**Blocks:**
TASK-271.

---

## TASK-271 — Production staged rollout

**Priority:** Critical
**Phase:** Phase 18
**Depends on:** TASK-270
**Status:** Not Started

**Objective:**
Release to production with a staged rollout per ADR-020.

**Implementation:**
Promote to production at 10% → 25% → 50% → 100% over several days, monitoring crash-free rate and vitals in Play Console.

**Acceptance Criteria:**
- Crash-free rate ≥ 99.5% at each stage.
- No critical vitals regressions.

**Validation:**
Play Console vitals archived.

**Output:**
Production release.

**Blocks:**
Phase 19.

---

# Phase 19 — Post-Launch Maintenance

Goal: sustain the app without disrupting the reading experience.

## TASK-285 — Crash monitoring baseline

**Priority:** High
**Phase:** Phase 19
**Depends on:** TASK-271
**Status:** Not Started

**Objective:**
Monitor crashes and ANRs via Play Console vitals (no third-party SDK). Establish alerting thresholds.

**Why now:**
Post-launch signal must be watched from day one.

**Implementation:**
Weekly review of Play Console vitals. Threshold: any crash affecting ≥ 0.5% of sessions triggers a hotfix investigation.

**Acceptance Criteria:**
- Review cadence documented and followed.

**Validation:**
Weekly log.

**Output:**
Monitoring cadence.

**Blocks:**
—

---

## TASK-286 — Dependency and security update cadence

**Priority:** Medium
**Phase:** Phase 19
**Depends on:** TASK-191
**Status:** Not Started

**Objective:**
Quarterly review of dependencies and security advisories; monthly review of Android platform advisories.

**Implementation:**
Scheduled reviews with an explicit "do not upgrade for the sake of upgrading" rule; upgrades require justification and re-run of the reader regression + content sweep.

**Acceptance Criteria:**
- Cadence documented and followed.

**Validation:**
Review log.

**Output:**
Update cadence.

**Blocks:**
—

---

## TASK-287 — Android version compatibility

**Priority:** Medium
**Phase:** Phase 19
**Depends on:** TASK-285
**Status:** Not Started

**Objective:**
Track new Android releases and Google Play `targetSdk` deadlines; ensure the app targets the latest required SDK before deadlines.

**Implementation:**
When Google announces a new mandatory targetSdk deadline, schedule the upgrade with sufficient lead time for full regression.

**Acceptance Criteria:**
- Compliance with Play requirements maintained.

**Validation:**
Play Console health.

**Output:**
Targeted-SDK schedule.

**Blocks:**
—

---

## TASK-288 — Content correction process

**Priority:** Critical
**Phase:** Phase 19
**Depends on:** TASK-024
**Status:** Not Started

**Objective:**
Define the process for responding to a verified content defect.

**Why now:**
Even validated content may have edge-case defects. Response must be disciplined.

**Implementation:**
Process:
1. Report received via a well-defined channel (email or GitHub issues).
2. Reproduce with the exact ayah, page, and reference source.
3. If confirmed as a defect: raise a content-correction ticket. Re-run Phase 1 gates on the corrected artifact. Bump content version.
4. Rendering-only defects: fix in code, bump app version.
5. Push a patch release through staged rollout.

Never patch content in code — always through the dataset.

**Acceptance Criteria:**
- Process documented in `docs/content-corrections.md`.
- At least one dry-run performed before v1.0 to verify the process works.

**Validation:**
Dry-run report.

**Output:**
Correction process + dry-run.

**Blocks:**
—

---

## TASK-289 — User feedback channel

**Priority:** Medium
**Phase:** Phase 19
**Depends on:** TASK-271
**Status:** Not Started

**Objective:**
Provide a minimal, respectful channel for feedback (email link in About screen).

**Implementation:**
Add a mailto link with a pre-filled subject including app version and dataset version. Do not add in-app feedback forms that require network permissions.

**Acceptance Criteria:**
- Link present, functional, respected.

**Validation:**
Manual test.

**Output:**
Feedback link.

**Blocks:**
—

---

## TASK-290 — Release cadence and long-term stewardship

**Priority:** Low
**Phase:** Phase 19
**Depends on:** TASK-285, TASK-286
**Status:** Not Started

**Objective:**
Establish a slow, deliberate release cadence: no changes unless justified.

**Implementation:**
Document release cadence: patch releases as needed for bugs/security; minor releases only for validated POST-MVP features; no releases purely for "engagement" or novelty.

**Acceptance Criteria:**
- Cadence documented.

**Validation:**
Adherence over the first year.

**Output:**
`docs/release-cadence.md`.

**Blocks:**
—

---

# Agent Execution Rules

These rules bind any human or coding agent that executes this roadmap.

1. **Never execute a task before its dependencies are complete.** If a dependency is incomplete, stop and complete it first.
2. **Never skip a failed task to continue downstream work.** A red foundation cannot support a green feature.
3. **Never silently change an architectural decision.** Any change to an ADR requires a new ADR that supersedes it, with justification and downstream impact analysis.
4. **Never modify Quran content without triggering Content Integrity validation.** All content changes re-run Phase 1 gates (TASK-019, TASK-020, TASK-021, TASK-024).
5. **Never introduce a new dependency without documenting it** in `docs/dependencies.md` with purpose, license, and justification. If it pulls tracking, reject it.
6. **Never assume compilation means the feature is complete.** Completion requires the full Definition of Done.
7. **Always run relevant tests after changes.** At minimum: unit tests for the touched module; instrumented tests for touched UI.
8. **Always update PROJECT_TASKS.md after completing a task** — mark status as Completed and update the Phase Progress Tracker and Project Status header.
9. **Record important technical discoveries** as ADRs or in `docs/` notes. Do not leave discoveries only in commit messages.
10. **Stop and repair the foundation** when a previous assumption proves incorrect. Do not paper over it downstream.
11. **Do not introduce undocumented features.** If it is not in Scope or a POST-MVP entry, it does not ship.
12. **Prioritize correctness over development speed.** A missed deadline is recoverable; a corrupted Quran display is not.
13. **Prioritize reading quality over visual novelty.** No animation, transition, or effect that interferes with reading.
14. **Prioritize stability over feature quantity.** A crash in the reader is release-blocking, always.
15. **Keep the core experience offline.** If any MVP feature grows a network dependency, the change is rejected.
16. **Treat Quran content integrity as a release-blocking concern.** No exceptions.

---

# Final Project Philosophy

This project is not merely an Android application that displays Quranic text.

It is the creation of a **digital Mushaf experience**.

Therefore:

**Correctness > Features.**
**Quran Content Integrity > Convenience.**
**Reading Experience > Visual Complexity.**
**Reliability > Development Speed.**
**Offline Reliability > Network Dependency.**
**Simplicity > Feature Bloat.**
**Respect > UI Experimentation.**
**Stability > Novelty.**

The final product should feel so natural that the user forgets they are using an application and simply feels that they are reading the Quran.
