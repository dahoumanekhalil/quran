# Architecture Decision Records (ADRs)

Every non-trivial architectural or product-shaping decision is recorded here as an ADR. Decisions include the trade-offs considered, the choice made, and the consequences we accept.

## Format

Use [`0000-template.md`](0000-template.md) verbatim. Numbering is monotonic (zero-padded to four digits). Never renumber a merged ADR — supersede it with a new one instead.

## Statuses

- **Proposed** — under discussion.
- **Accepted** — the current decision. Code and process must comply.
- **Superseded by ADR-NNNN** — replaced by a newer ADR (cross-linked).
- **Deprecated** — no longer applicable; not superseded (unusual).

## Review

Each ADR carries a **Review date**. Before every phase transition (as defined in `PROJECT_TASKS.md`), skim any ADR whose review date has passed and either bump it forward or mark it superseded.

## Existing ADRs

| # | Title | Status |
|---|---|---|
| 0001 | Programming language | Accepted |
| 0002 | UI toolkit | Accepted |
| 0003 | SDK levels (min / target / compile) | Accepted |
| 0004 | Architecture pattern | Accepted |
| 0005 | Navigation library | Accepted |
| 0006 | State management | Accepted |
| 0007 | Local persistence | Accepted |
| 0008 | Dependency injection | Accepted |
| 0009 | Concurrency model | Accepted |
| 0010 | Data layer architecture | Accepted |
| 0011 | Domain layer architecture | Accepted |
| 0012 | UI layer architecture | Accepted |
| 0013 | Offline-first strategy | Accepted |
| 0014 | Quran content asset packaging | Accepted |
| 0015 | Font packaging strategy | Accepted |
| 0016 | Reader paging approach | Accepted |
| 0017 | Text rendering approach | Accepted |
| 0018 | Testing strategy | Accepted |
| 0019 | Build system | Accepted |
| 0020 | Release strategy | Accepted |
| 0021 | Quran text source | Accepted |
| 0022 | Pagination reference | Accepted |
| 0023 | Build & test workflow (phone-first, cloud CI) | Accepted |
