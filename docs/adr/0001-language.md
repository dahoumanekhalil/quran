# ADR-0001: Use Kotlin as the primary language

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 3

## Context and problem statement

The Android app needs a primary programming language. Java, Kotlin, and multi-language mixes are viable on Android. This choice constrains every downstream ADR (UI toolkit, DI, testing, concurrency) and the hiring / maintenance surface.

## Decision drivers

- Long-term maintainability and readability.
- Null-safety at the type level (relevant to reader stability — see charter principle #11).
- First-class support for structured concurrency (needed for Room / DataStore / IO).
- Compatibility with Jetpack Compose (declarative UI).
- Google's stated direction for Android.
- Ability for a small team (currently one) to maintain the codebase.

## Options considered

### Option A — Kotlin only

Modern, concise, null-safe, coroutines/Flow built in, Google's default for new Android development since 2019, all Jetpack libraries have Kotlin-first APIs, mature tooling.

### Option B — Java only

Widely known, huge ecosystem, verbose, no null-safety, no coroutines (RxJava or callbacks instead), no first-class Compose support, essentially deprecated as the default for new Android apps.

### Option C — Kotlin + Java hybrid

Interop is smooth, but a mixed codebase doubles the mental model, complicates linting, and adds no value for a greenfield project.

## Decision

**Kotlin only.**

Kotlin's null-safety, coroutines/Flow, and first-class Compose integration directly serve the charter's reading-stability and correctness-over-cleverness principles. No compelling reason to introduce Java.

## Consequences

- Positive: modern, safe, concise; access to the full Jetpack Compose surface; coroutines for the reader engine and data pipeline; ktlint/Spotless for consistent style.
- Negative: JVM bytecode still, so JDK is required for compilation (see ADR-0023). Kotlin compilation is slower than Java — mitigated by incremental compilation and by the phone-first build workflow in ADR-0023.
- Follow-up: ADR-0002 (UI toolkit) can now safely assume Kotlin. ADR-0009 (concurrency) picks Coroutines.

## References

- ADR-0002 (UI toolkit), ADR-0009 (concurrency), ADR-0019 (build system).
- Google's Android development guidance: Kotlin-first since 2019.
