# Privacy policy — Mushaf (Android)

Effective date: **on first Play Store release** (Phase 18).
Current version: draft; will be linked from the app's About screen and Play listing at publish time.

## Summary in one sentence

Mushaf does not collect, store, or transmit any personal information — everything stays on your device, and the app makes no network requests.

## What data does the app collect?

**None.** No account, no email, no phone number, no name.

The app does not:

- Ask for or store your name, email, phone number, or any other identifier.
- Ask you to sign in with Google, Facebook, Apple, or any other provider.
- Collect device identifiers (IMEI, MAC address, Advertising ID, Firebase IID, etc.).
- Read your contacts, calendar, photos, location, microphone, camera, or messages.
- Use any third-party analytics SDK.
- Use any advertising SDK.
- Use any crash reporting SDK that transmits data off-device.

## What data does the app store on your device?

The following is stored **only in your app's private storage** on your device:

- Your last-read page (so the app can reopen there).
- Your bookmarks — a list of page numbers you saved.
- Your settings — chosen theme, text size, Mushaf font, and the keep-screen-on preference.

That data is never uploaded, backed up, or shared. Android automatic backup is disabled (`android:allowBackup="false"`). Uninstalling the app removes all of this data.

## Does the app make network requests?

**No.** The Android manifest does not declare the `INTERNET` permission. All Quran text, pagination data, fonts, and the full-text search index are bundled inside the installed APK. The app functions completely offline.

## Does the app use ads?

**No, and never will.**

## Does the app have in-app purchases?

**No, and never will.** The app is free and contains no premium tiers, subscriptions, upsells, or paywalls.

## Does the app request permissions?

**No.** It requests zero Android permissions. See `docs/permissions.md`.

## Children

The app is safe for all ages. It contains no ads, no communication features, no user-generated content, and no data collection.

## Third-party content licenses

The app bundles:

- **Quran Uthmani text** from the Tanzil Project (Creative Commons Attribution 3.0). Verified against the Quran.com KFGQPC-derived text for cross-source consistency.
- **Pagination data** from `quran-center/quran-meta` (MIT license).
- **KFGQPC Uthmanic Hafs v2.2 font** — bundled under the King Fahd Quran Printing Complex EULA (free distribution permitted).
- **Amiri Quran v1.003 font** — bundled under the SIL Open Font License 1.1.

## Google Play Data Safety declaration

The Play Store Data Safety section will state:

- **Data collected:** None.
- **Data shared:** None.
- **Data type breakdown:** N/A.
- **Encryption in transit:** N/A (no transit).
- **Data deletion request URL:** N/A (no data to delete off-device).

## Contact

Feedback and questions: `khalildahoumane@gmail.com`. This address is used only for user-initiated messages sent via a mail app on the user's device — the app itself has no network permission and cannot send anything.

If any of the above changes in a future version, the effective date will be updated and the About screen inside the app will link to the revised policy. Source of truth: https://github.com/dahoumanekhalil/quran (this repo).

## Independent verification

The claims above are checkable:

- Manifest permissions → `android/app/src/main/AndroidManifest.xml` — zero `<uses-permission>` elements.
- No network dependencies → `docs/dependencies.md` — every runtime dep listed with "Network? No".
- No trackers → grep the runtime dep tree for the forbidden groups documented in `docs/dependencies.md`.
