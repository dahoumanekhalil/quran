# Digital Mushaf

An offline-first, Mushaf-style Android Quran reader. Not a text-reading app — a calm, page-based reading surface that mirrors a physical **Madani mushaf (15 lines per page, 604 pages)**, using the **Hafs `an` Asim** riwaya.

**Guiding principle:** the Quran is always the visual and functional center. Every pixel and control serves reading. No advertisements, no subscriptions, no user accounts, no third-party analytics, no tracking, no engagement funnels.

---

## Repository layout

| Path | Contents |
|---|---|
| `PROJECT_TASKS.md` | The authoritative roadmap — 20 phases, every task with objective, why-now, implementation, acceptance criteria, and dependencies. **Single source of truth for planning.** |
| `docs/CHARTER.md` | Vision, principles, MVP scope, POST-MVP, FUTURE, explicit non-goals. |
| `docs/data-model.md` | Storage-independent Quran data schema and invariants. |
| `docs/adr/` | Architecture Decision Records — every non-trivial technical decision, formatted per `docs/adr/0000-template.md`. |
| `data/sources/` | Immutable, hashed archives of upstream data (Tanzil, quran-meta, Quran.com). Each has a `SOURCE.json` manifest. |
| `data/pipeline/` | Deterministic Python pipeline that ingests the sources and emits `quran.db`. |
| `data/output/RELEASE/` | Frozen dataset artifacts — the SQLite file that the app ships with. |
| `android/` | Android app project (Phase 3+). |
| `prototype/` | Throwaway prototypes (Phase 2 rendering prototype). |
| `LICENSES/` | Full text of third-party licenses. |
| `NOTICE` | Third-party attribution. |

---

## Current status

**Frozen content:** `data/output/RELEASE/quran-1.0.0.db`
SHA-256: `0ba78b6ab99f57a5688adc572f35ccf0568716a48d4424378d73cfd91c0b093e`

| Phase | Status |
|---|---|
| Phase 0 — Project Preparation | Complete |
| Phase 1 — Quran Data Foundation | Complete |
| Phase 2 — Typography & Rendering Prototype | Not started |
| Phase 3+ | Not started |

See `PROJECT_TASKS.md` for the full phase tracker.

---

## Building the dataset from source

Requires Python 3.10+. Deterministic — same input hashes yield byte-identical outputs.

```bash
python data/pipeline/pipeline.py all
```

Stages: `parse_tanzil` → `parse_meta` → `merge` → `validate_structural` → `validate_unicode` → `cross_source_diff` → `package_sqlite` → `build_search_index` → `release`.

Individual stages: `python data/pipeline/pipeline.py <stage>`.

---

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) and [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

---

## Third-party content and licenses

The application bundles or depends on the following works. Full license text is in [`LICENSES/`](LICENSES/), attribution is in [`NOTICE`](NOTICE).

| Work | Provenance | License |
|---|---|---|
| Tanzil Uthmani Quran text (v1.1) | https://tanzil.net/ | CC BY 3.0 (verbatim only) |
| `quran-center/quran-meta` (pagination + metadata) | https://github.com/quran-center/quran-meta | MIT |
| Mushaf font (chosen in Phase 2) | TBD | TBD |

---

## Project license

See [`LICENSE`](LICENSE).
