# Monitoring cadence — TASK-285, TASK-286, TASK-287

Post-launch monitoring is done **without** any third-party SDK. Play Console vitals is the single source of truth. This keeps the promise in `docs/privacy-policy.md`.

## Weekly — Play Console vitals review

Every week the maintainer opens Play Console → Vitals and reviews:

- **Crash-free users** — target ≥ 99.5%. Alert at < 99.0%.
- **ANR rate** — target < 0.1% per user per week. Any ANR in the reader is release-blocking (Charter §11).
- **Crashes by cluster** — top 3 clusters, if any. Any cluster affecting ≥ 0.5% of sessions triggers a hotfix investigation.
- **Excessive wakeups / battery drain** — should be zero; the app requests no wake locks and does no background work.

Log observations in `docs/monitoring-log.md` (append-only). If nothing to note, write "clean week."

## Monthly — Android security advisories

First business day of each month, check:

- Android Security Bulletin — any advisory that touches the app's dependency surface.
- Play Console app health notifications.

Log in `docs/monitoring-log.md` under a monthly heading.

## Quarterly — Dependency review

Every 3 months, re-audit `docs/dependencies.md`:

1. Diff current `libs.versions.toml` against previous quarter's snapshot.
2. For each dep, check its security advisories in the intervening period.
3. Check that no new dep quietly transitively pulled in a tracker (grep the resolved runtime classpath for the forbidden groups).
4. Decide upgrade or hold per Release Cadence rule ("do not upgrade for its own sake").

Document the decision explicitly in the audit log.

## Ad-hoc — Google Play policy changes

Google Play sends deadline announcements for `targetSdk` bumps and policy changes. When one arrives:

1. Read it end-to-end.
2. Determine whether the app is affected.
3. Schedule the required work with sufficient lead time for a full regression pass before the deadline.

## Escalation thresholds

- Any crash cluster ≥ 0.5% of sessions → **48-hour hotfix SLA**.
- Any ANR cluster ≥ 0.2% of sessions → **48-hour hotfix SLA**.
- Play Console policy strike or app suspension → immediate response, all other work paused.
- Content defect report classified Category C (confirmed consonantal defect per `docs/content-corrections.md`) → **72-hour patch SLA**.

## No third-party monitoring

To be explicit: Sentry, Crashlytics, Bugsnag, Firebase Performance, Firebase Analytics, Google Analytics, Segment, Mixpanel, Amplitude — **none** of these will be added. Play Console vitals is sufficient for this app's needs and honors the privacy commitments.
