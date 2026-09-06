# Google Play store listing draft — TASK-266

Copy these fields into Play Console at submission time.

## App title (30 chars max)

`Mushaf — Offline Quran Reader`

Fallback (26): `Mushaf: Offline Quran`

## Short description (80 chars max)

`Read the Quran. No ads, no accounts, no tracking, fully offline. Just the Mushaf.`

## Full description (up to 4000 chars — target ~1500)

```
Mushaf is a digital Quran reader designed to feel like a printed Mushaf.

Open the app and return to your last-read page. That's the promise.

WHAT YOU GET
• The complete Quran — 114 Surahs, 6236 Ayahs, 604 pages of the standard Madani mushaf.
• Uthmani text from the Tanzil Project, cross-verified against the King Fahd Quran Printing Complex text for consonantal accuracy (6236 out of 6236 verses match).
• Two Mushaf-quality Arabic fonts bundled: KFGQPC Uthmanic Hafs and Amiri Quran. Choose either.
• Offline search across the entire Quran, tolerant of diacritics and alef variants.
• Bookmarks. Simple, per-page, like a ribbon.
• Light and dark themes, chosen for warm, low-fatigue reading.
• Adjustable text size. Keep-screen-on option for long sessions.
• Continue-reading — the app opens straight to your last page.

WHAT YOU DON'T GET
• No advertisements. Not now, not ever.
• No subscriptions. No paid tiers. The full app is free.
• No accounts. No sign-in. No email required.
• No tracking. No analytics. No third-party SDKs.
• No network requests. The app doesn't have the internet permission.
• No permissions requested at all. Zero.

WHY OFFLINE
The Quran is bundled inside the app. Nothing is downloaded, nothing is fetched, nothing is streamed. Once installed, the app never talks to any server for any reason. Everything you read, search, and bookmark stays on your device.

WHY NO ADS
Because the Quran deserves an interface that respects it.

WHY NO ACCOUNTS
Because reading is between you and the text.

CONTENT SOURCES
• Quran text — Tanzil Project (CC BY 3.0)
• Pagination — quran-center/quran-meta (MIT)
• KFGQPC Uthmanic Hafs — bundled per the KFGQPC EULA
• Amiri Quran — SIL Open Font License 1.1

PRIVACY
The app does not collect, store, or transmit any personal data. See the About screen in the app for the full privacy statement.

SOURCE
This is an open project. The full source code is available at github.com/dahoumanekhalil/quran.
```

## Category

**Books & Reference** (primary). Sub-category: Religion.

## Privacy policy URL (Play Console required)

Play Console requires a publicly-accessible URL. `docs/privacy-policy.md` in this repo is not directly usable — it's markdown, not HTML, and needs a public host.

Cheapest options that require zero infrastructure setup:

1. **GitHub's rendered blob URL:**
   `https://github.com/dahoumanekhalil/quran/blob/main/docs/privacy-policy.md`
   Play Console accepts this. Renders as GitHub's markdown view.

2. **GitHub Pages:** enable Pages on the repo (Settings → Pages → source = `main` branch, `/docs` folder). Then the policy is at:
   `https://dahoumanekhalil.github.io/quran/privacy-policy.html`
   (requires converting the `.md` to `.html` or enabling Jekyll — Jekyll auto-renders `.md` if the file has front-matter.)

3. **Custom domain:** if you already have one — cheap and forever-yours, but not required.

For the initial internal-testing release, option 1 is fine. Upgrade to option 2 before promoting to production.

## Content rating questionnaire hints

- Not a game.
- No user-generated content.
- No communication features.
- No advertising.
- No sensitive content beyond religious text.
- No location.
- No purchases (in-app or otherwise).

Expected rating: **Everyone** / PEGI 3 equivalent.

## Screenshots to capture

Take these on a real phone (see `docs/qa/device-matrix-checklist.md`). Play Console requires at least 2, up to 8. Take portrait shots:

1. Reader on page 1 (Al-Fatihah), light theme, chrome visible with Surah name + Juz + Page context strip.
2. Reader on a mid-mushaf page (e.g. page 300), dark theme, chrome hidden (immersive view).
3. Navigation → Surah list.
4. Search screen with active query and 3+ results.
5. Settings screen.
6. About screen (shows dataset SHA + privacy statement).

## Feature graphic (1024 × 500)

Recommendation: minimal typographic graphic. "**مصحف**" (Mushaf) in the Uthmanic font on a warm cream (`#FBF7EC`) background, tiny English subtitle "Offline Quran Reader" in Muted `#8A8266`. No decorative flourish.

**Do not** commission or generate an icon that includes photos of mosques, illuminated manuscripts, or any AI-generated Arabic — those risk both authenticity issues and misvocalized calligraphy.

## App icon (512 × 512)

Similar restraint. A single, calm mark. Recommended: the letter "م" or a simple book-fold silhouette in Accent color `#7A5A2E` on Paper `#FBF7EC`. No 3D, no gradients, no photorealism.

## Localizations for v1

Ship English at minimum. Arabic listing draft below — copy into Play Console alongside English before promoting to production.

### Arabic (العربية) — for the ar-SA and ar (default Arabic) locales

**App title (30 chars max):**
`مصحف — قراءة قرآن دون إنترنت`

Fallback (26): `مصحف: القرآن دون إنترنت`

**Short description (80 chars max):**
`اقرأ القرآن الكريم. بدون إعلانات، بدون تسجيل، بدون تتبع. يعمل دون إنترنت.`

**Full description:**

```
مصحف رقمي مصمم ليشبه المصحف المطبوع.

افتح التطبيق، وعُد إلى صفحتك الأخيرة. هذا هو الوعد.

ما ستحصل عليه
• القرآن الكريم كاملًا — 114 سورة، 6236 آية، 604 صفحات من المصحف المدني المعتمد.
• النص العثماني من مشروع تنزيل، تم التحقق منه بمطابقة نص مجمع الملك فهد لطباعة المصحف الشريف. النتيجة: تطابق تام في 6236 من 6236 آية على مستوى الحروف.
• خطان مصحفيان مدمجان: KFGQPC العثماني وأميري القرآن. اختر ما يريحك.
• بحث سريع في القرآن كاملاً — يعمل دون إنترنت، ويتسامح مع الحركات وأشكال الألف المختلفة.
• العلامات (Bookmarks) — بسيطة، على مستوى الصفحة، كشريط علامة كتاب.
• وضع فاتح وداكن، ألوان دافئة تريح العين في القراءة الطويلة.
• حجم خط قابل للتعديل. خيار إبقاء الشاشة مضيئة أثناء القراءة.
• استكمال القراءة — يفتح التطبيق مباشرة على آخر صفحة قرأتها.

ما لن تجده
• لا إعلانات. الآن ولا مستقبلًا.
• لا اشتراكات. لا مستويات مدفوعة. التطبيق مجاني بالكامل.
• لا حساب. لا تسجيل دخول. لا بريد إلكتروني مطلوب.
• لا تتبع. لا إحصاءات. لا مكتبات طرف ثالث.
• لا اتصال بالإنترنت. التطبيق لا يمتلك صلاحية الإنترنت أصلاً.
• لا صلاحيات على النظام. صفر صلاحيات.

لماذا دون إنترنت
النص القرآني مضمّن داخل التطبيق. لا شيء يُحمَّل، لا شيء يُطلَب، لا شيء يُبَث. بعد التثبيت، لا يتواصل التطبيق مع أي خادم لأي سبب. كل ما تقرأ وتبحث عنه وتحفظه يبقى على جهازك.

لماذا لا إعلانات
لأن القرآن يستحق واجهةً تحترمه.

لماذا لا حساب
لأن القراءة بينك وبين النص.

مصادر المحتوى
• نص القرآن — مشروع تنزيل (CC BY 3.0)
• التقسيم إلى صفحات — quran-center/quran-meta (MIT)
• خط KFGQPC العثماني — مضمَّن وفق ترخيص مجمع الملك فهد
• خط أميري القرآن — رخصة SIL Open Font License 1.1

الخصوصية
لا يجمع التطبيق أي بيانات شخصية، ولا يخزنها، ولا يرسلها. راجع صفحة "حول التطبيق" داخل التطبيق للاطلاع على بيان الخصوصية الكامل.
```

**Notes for the Arabic listing:**
- Text direction is naturally RTL — Play Console handles this if the locale is set to Arabic.
- Avoid transliterations in the description (users searching in Arabic want Arabic keywords).
- The app is one-way inside: displays only, does not collect. Emphasize this because Arabic Play users are increasingly sensitive to permission-heavy religious apps.

## Fields to leave blank / decline

- **In-app products** — none.
- **Ads** — declare "no ads" in the ads questionnaire.
- **Contains ads** — false.
- **Family policy** — the app is family-safe by construction (see content rating) but we do NOT opt into Designed for Families programs, as those add compliance surface with no user benefit for this app.
