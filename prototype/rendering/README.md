# Mushaf rendering prototype

**Throwaway** Android app that satisfies TASK-026 (Phase 2): render frozen `quran-1.0.0.db` with the primary Mushaf font (KFGQPC Uthmanic Hafs v2.2), 604 pages, `HorizontalPager`, RTL, with in-app toggles for font and size.

This app is **not** the production reader. It exists to prove that the chosen rendering stack (Kotlin + Jetpack Compose + `BasicText` / `Text` + `HorizontalPager`, per ADRs 0002 / 0016 / 0017) reproduces Uthmani text acceptably on real Android hardware before Phase 3 begins.

## What's in the APK

- The full frozen dataset `quran-1.0.0.db` (bundled at `app/src/main/assets/quran/quran.db`).
- The **primary** font: KFGQPC Uthmanic Hafs v2.2 (`assets/fonts/uthmanic_hafs_v22.ttf`).
- The **alternate** font: Amiri Quran v1.003 (`assets/fonts/amiri_quran.ttf`) — toggleable in the UI.
- ~5 MB total APK size.

## Building

Per ADR-0023 (phone-first, no heavy PC tooling), the primary build path is **GitHub Actions cloud CI**:

1. Push a commit that touches this directory (or manually trigger the workflow).
2. `.github/workflows/prototype-build.yml` runs on GitHub's Ubuntu runner, produces `app-debug.apk`.
3. Download the workflow artifact from the Actions tab.
4. Copy the APK to your phone and install (enable "Install from unknown sources" for your file manager or browser). Or push via `adb install -r app-debug.apk` if you have platform-tools installed locally.

**Local build (optional fallback):**

Requires JDK 17 and Android SDK cmdline-tools installed with:
- `platforms;android-35`
- `build-tools;35.0.0`
- `platform-tools`

Then either use a pre-installed Gradle 8.7+ (`gradle assembleDebug`) or generate the wrapper first (`gradle wrapper --gradle-version 8.10.2` then `./gradlew assembleDebug`). The wrapper JAR is not committed to keep the repo lean; CI provides Gradle directly via `gradle/actions/setup-gradle`.

## What to check when the APK runs

TASK-026 acceptance criteria (from `PROJECT_TASKS.md`):

- [ ] App launches.
- [ ] Pages 1, 2, 3, 50, 300, 600, and 604 all render without missing glyphs (no tofu boxes) and with correct RTL shaping.
- [ ] Horizontal swipe advances/rewinds pages (RTL: swipe left → next page).
- [ ] Font + size + line-height toggles work.
- [ ] Page turns feel responsive (target < 100 ms on the low-end reference device).
- [ ] Text matches a printed Madani mushaf reference for the same pages (side-by-side comparison).

Log any defects in `prototype/rendering/DEFECTS.md` (create if absent). If acceptance holds, TASK-029 signs off via a new ADR-0025 (rendering-lock).

## Delete when done

This whole directory (`prototype/rendering/`) is discarded before Phase 3. Nothing in production code should import from `app.mushaf.prototype.*`.
