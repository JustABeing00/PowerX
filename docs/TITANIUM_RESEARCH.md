# Titanium Browser — Technical Research

> Research-only document. Describes **functionality and architecture** of the
> Titanium Browser project. Contains **no copied implementation code**.
> Titanium sources were treated as read-only reference material.
>
> - Local reference: `D:\android-titanium-browser-main` (read-only, never modified)
> - Upstream: https://github.com/jqssun/android-titanium-browser
> - Companion extension: https://github.com/jqssun/android-titanium-extension
> - Upstream base: https://github.com/GrapheneOS/Vanadium
> - Research date: **2026-10-31**
> - Upstream state observed: default branch `main`, **111 commits**,
>   ~3.1k stars, 162 forks, 161 issues, 5 pull requests.
>   Latest release observed: **v156.0.8078.79** (published 2026-10-30).
>   Recent tags: v156.0.8078.79 / .73 / .47, v155.0.8059.84 / .79 / .39.
>   Recent commit subjects are almost entirely automated `update` commits from
>   `github-actions[bot]` (Vanadium submodule bumps) plus occasional author
>   commits (`desktop`, `developer`, `priority`, …).
> - Vanadium submodule pinned at upstream commit `0202b7b` (as displayed on
>   the GitHub repo page on the research date).

## 1. What Titanium is (functional summary)

Titanium Browser for Android is a **patch-and-build distribution of Chromium**,
not a from-scratch browser. Its repository contains **no browser engine code
of its own**. It consists of:

1. A **Vanadium submodule** (GrapheneOS's hardened Chromium patch set) —
   the security/privacy base.
2. A **Titanium patch layer** (`patch.sh` + `res/` + `extensions/`) applied
   on top of a pinned Chromium source tree at build time.
3. **Build configuration** (`args.gn`, `.gclient`, `build.sh`, `common.sh`).
4. **CI automation** (`.github/workflows/build.yml`) that bumps Vanadium,
   builds `chrome_public_apk`/`chrome_public_bundle` for arm/arm64, signs,
   and publishes GitHub releases.
5. A **bundled companion extension** (`android-titanium-extension`, MV3)
   shipped inside the APK as an asset and side-loaded into Chromium's
   external-extensions directory at runtime.

Formerly named Helium Browser for Android; renamed to avoid branding
collision with the unrelated desktop Helium project. Package name observed in
build config: `io.github.jqssun.helium` (historical remainder of the rename).

## 2. Local ZIP inspection (`D:\android-titanium-browser-main`)

Top-level contents of `android-titanium-browser-main/`:

| Entry | Purpose |
|---|---|
| `README.md` | Usage docs (extension install sources, debug URLs, WebRTC note), architecture diagram, build/credits notes |
| `LICENSE` | GNU GPL version 2 (full text, no "or later" upgrade path investigated — see `docs/LICENSING_AUDIT.md`) |
| `.gitmodules` | Single submodule: `vanadium → https://github.com/GrapheneOS/Vanadium.git` |
| `vanadium/` | **Empty directory in the ZIP** — GitHub source archives do not include submodule contents |
| `build.sh` | Full Chromium checkout + patch + GN/Ninja build + sign script (Linux-only) |
| `patch.sh` | ~190-line Titanium patch layer (sed edits + file copies against Chromium source) |
| `common.sh` | Helpers: key setup, APK/AAB signing, version comparison |
| `args.gn` | GN build flags (package name, `is_desktop_android = true`, proprietary codecs, PGO, etc.) |
| `.gclient` | `depot_tools` solution + three `checkout_android` hooks (filter lists, subproject patches, extension bundling) |
| `extensions/` | Companion-extension bundling: `bundle.py`, `BUILD.gn`, `stage_bundled_extensions.inc` |
| `res/` | `icon.svg`, `icon.sh` (icon recoloring), `drawable/themed_app_icon.xml` (original Saturn-like glyph) |
| `fastlane/` | Play Store listing metadata + screenshots |
| `.github/workflows/build.yml` | Self-hosted-runner CI: Vanadium bump → build → sign → GitHub release + attestation |

### 2.1 Critical finding: the ZIP has no Vanadium contents

`vanadium/` is an **empty directory reference only**. Any analysis based
solely on the ZIP cannot see the Vanadium patch set; the live submodule
content must be reviewed at https://github.com/GrapheneOS/Vanadium
(`patches/*.patch`, `subprojects_patches/**/*.patch`, `tools/`, `args.gn`).
This also means the ZIP alone is **not a complete buildable source tree** —
a full build additionally downloads ~tens of GB of Chromium + dependencies.

## 3. Build pipeline (functional description)

1. **Checkout**: `depot_tools` + shallow fetch of the Chromium tag matching
   the version string in `vanadium/args.gn`.
2. **Rebrand Vanadium patches**: delete a set of Vanadium patches irrelevant
   outside GrapheneOS (trichrome targets, language packs, JS optimizer UI,
   component updates, PDF/config-app/new-tab-card/predictive-back patches),
   then text-replace `Vanadium→Titanium` branding in the remaining patches.
3. **Apply** Vanadium generic + subproject patches with `git am`.
4. **Sync deps + hooks** (`gclient sync`, `runhooks`, `install-build-deps.sh`).
   Hooks: (a) download EasyList/EasyPrivacy/anti-adblock filter lists into
   `titanium/android_config/filter_lists/`; (b) apply Vanadium subproject
   patches; (c) download the companion extension release CRX and index it in
   `bundled.json` with an extension-ID/version check.
5. **Apply Titanium layer** (`patch.sh`, see §4).
6. **GN configure + Ninja build** `chrome_public_apk` (arm, then arm64) and
   `chrome_public_bundle` (arm64).
7. **Sign** APK/AAB with keys from CI secrets; publish via `softprops/action-gh-release`
   with SLSA attestation (`actions/attest-build-provenance`).

CI additionally **auto-commits Vanadium submodule bumps** (`git commit "update"
&& git push` with `contents: write`) on a 16-day cron — a workflow with push
rights that must **not** be imitated in this project without explicit approval.

## 4. Titanium patch layer (`patch.sh`) — functional categories

The script makes ~70 targeted source edits. Grouped by **function** (not code):

**Branding/assets.**
Swap the app icon resource dir for `res_titanium_base`, navy-tint stock
icons, inject the original vector glyph, set `extractNativeLibs=false`,
register a PDF MIME type, drop an ARCore manifest entry.

**Core enabler: desktop-Android mode.**
Sets `is_desktop_android = true` (see `args.gn`). This single GN flag is the
load-bearing decision: it compiles Chromium's **desktop extension stack**
(extension toolbar UI, `chrome://extensions` page, popup hosting, off-store
install flows) into the Android build instead of the stripped-down mobile
configuration. Nearly every other extension edit below only works because of it.

**Extension support restoration/extension (largest group).**
- Re-enable Manifest V2 handling paths (MV2 deprecation bypass, MV2 testing
  flag forced on, restore `browser_action`/`page_action` API registration).
- Allow off-store CRX installs initiated from extension pages and from
  Opera/Edge CDN hosts.
- Force desktop-site rendering on the Chrome Web Store so store install flows work.
- Add an extensions toolbar container to the phone toolbar layout and wire
  toolbar-button drawing into the phone toolbar painter.
- Rework the extension-action popup anchoring so popups open on phones
  (null-button guards, alternate anchor view, fullscreen sizing on phones
  instead of desktop auto-resize).
- Allow extensions in Incognito/OTR contexts; treat extension renderer
  processes as important to keep them alive.
- Enable `desktop_android` in extension API feature manifests; disable
  security-flag filtering on the extension install dialog.
- Make locale/validation helpers tolerate Storage Access Framework
  (`content://`, virtual-document) paths so **Load unpacked** works through
  the Android file picker.
- Exempt NTP-override handling so extensions can replace the new-tab page;
  exempt `app` API platform allow-listing for desktop_android.
- Fix three upstream crash bugs called out by id (incognito use-after-free
  ×2, profile-destroy ordering, VirtualDocumentPath fast-path, back-press
  handler).

**Feature overrides.**
Force-enable at startup: submenus in app menu, DevTools frontend, media
picker, screen capturing, background media capture, auto-PiP, context-menu
PiP, load-all-tabs-at-startup, native URL overriding. Extend flag-expiry
milestones; force site-search/omnibox features on; adjust zero-suggest
provider for desktop-android.

**UI/mobile adaptations.**
Extension WebUI pages get a mobile viewport + fluid card widths; snackbar and
tab-strip accessibility checks accept screen-reader/touch-exploration states;
magic-stack home modules skip educational tips; small menu/window-management
changes (incognito window toggle); language-settings and privacy-settings page
tweaks.

**DevTools on device.**
Enable task manager + DevTools menu paths on non-tablet phones, fix incognito
DevTools intent plumbing, add mobile viewport to DevTools frontend, keep line
wrapping for view-source.

**Media/playback.**
Bypass a renderer media-playback restriction guard for background playback
scenarios.

**Navigation hardening.**
Restrict external-intent handling to network URLs (`LaunchIntentDispatcher`
scheme guards); verify-package bypass for the bundled-extension config parser.

### What this tells an independent implementer

- Chrome-extension support on Android is **not one patch**; it is the
  combination of (a) compiling the desktop extension stack
  (`is_desktop_android`), (b) un-blocking the MV2/off-store install paths the
  desktop stack gates off on mobile, (c) rebuilding the phone toolbar/popup
  anchoring that the desktop UI assumes, and (d) SAF/content-URI tolerance for
  unpacked loading. Any independent Chromium-based implementation must solve
  all four, against whatever Chromium milestone it pins.
- The edits are **tightly coupled to exact Chromium source lines** (sed
  line-matches with version conditionals, e.g. `version_lt 156…` /
  `152.0.7969.0` branches). This is the main maintenance cost: every Chromium
  milestone bump can break the patch layer, which is why Titanium tracks
  Vanadium/Chromium closely with automated rebuilds.
- Difficult-to-reproduce items: popup anchoring/resizing on phones, extension
  process prioritization without regressing battery/memory, SAF-tolerant
  unpacked install, and the MV2-allowlist behavior as upstream removes MV2
  code paths entirely.

## 5. Companion extension (`android-titanium-extension`)

- Repo state observed: `main`, **3 commits**, 171 stars, 5 forks, 9 issues.
  License: **GPL-2.0**.
- It is an ordinary **Manifest V3** extension (`manifest_version: 3`,
  service-worker background, `action` popup, `options_ui`, `incognito: split`).
- Declared permissions (from upstream `src/manifest.json`): `downloads`,
  `tabs`, `cookies`, `contextMenus`, `storage`,
  `declarativeNetRequestWithHostAccess`, host access `<all_urls>`.
- Source layout: `src/background.js`, `common.js`, `options.{html,js}`,
  `popup.{html,js}`, `send.{html,js}`, `install.{html,js}`, `style.css`,
  plus `src/marketplaces/` adapter scripts and declarativeNetRequest rule
  resources (`opera.json`, `edge.json`) that adjust user-agent handling so
  Opera/Edge add-on stores serve installable packages.
- **Architecture split learned**: browser-engine responsibilities (install
  pipeline, off-store allow-list, toolbar/popup hosting, process lifetime)
  live in the browser build; the companion extension only provides
  **store adapters** (Opera/Edge download assist), **external download
  manager forwarding** via `intent://` URLs configured in options,
  enhanced dark mode, and extra privacy toggles. Nothing in it grants
  extension capability by itself — it *consumes* the engine's extension stack.
- **Bundling mechanism** (functional): at image build time `bundle.py`
  downloads the released `.crx`, parses its header to derive the extension
  ID, records `{id: {external_crx, external_version}}` in `bundled.json`,
  and packages both as compressed APK assets. At runtime the C++ helper
  (`stage_bundled_extensions.inc`, hooked into `ExternalPrefLoader`) stages
  the CRX into Chromium's external-extensions directory and merges the index
  into external-extension prefs — i.e. a **pre-seeded force-install list**.
  Size check (`existing size == asset size`) avoids redundant rewrites.

## 6. Vanadium relationship

- Vanadium = GrapheneOS's hardened Chromium patch set + build config; it
  provides the WebView and the stock browser on GrapheneOS and explicitly
  depends on OS-level hardening outside the browser.
- Titanium consumes Vanadium as a submodule, prunes non-portable patches,
  rebrands the rest, and stacks its own extension/UI layer on top.
- Vanadium's own licensing: patch set under **GPL-2.0-only** with explicit
  exception notes (`LICENSE.WebView-note`, `LICENSE.Apache-2.0-note`,
  `LICENSE.FTL-note`); upstream Chromium remains BSD-style. Full breakdown in
  `docs/LICENSING_AUDIT.md`.
- Update strategy observed: Titanium's CI checks out the newest Vanadium tag
  on cron and rebuilds — Chromium version tracking is inherited from Vanadium,
  which is why Titanium releases mirror Chromium milestones (v151–v156 in the
  tag list).

## 7. Upstream issues & discussions (sample, 2026-10-31)

From the latest-20 issue list (`state=all` via GitHub API; 161 total):

| # | State | Theme |
|---|---|---|
| 371 | open | Extensions disappearing (lifecycle/persistence problem) |
| 362 | open | Extension popups render extremely small (phone popup sizing) |
| 361 | open | Release build cannot reach any website (networking regression) |
| 354 | open | Embedded YouTube videos won't play |
| 366 | open | History auto-clears when app is swiped away (session handling) |
| 357 | open | Deleting download history deletes actual files (storage handling) |
| 369/368/367/363 | open | Home-screen shortcuts, settings scroll state, tab-switcher layout, toolbar pinning (mobile UX) |
| 353 | open | AMOLED dark UI, tablet tab strip (theming/form-factor) |
| 352/356/360 | open | F-Droid, Zap Store, Play Store staleness (distribution) |
| 358 | open | Non-English/garbled report (locale handling signal) |
| 370 | open | Remove "ML Kit" manifest entry (dependency hygiene) |

Patterns relevant to us: **extension lifecycle/popup sizing**, **site-compat
regressions per milestone** (YouTube, networking), **download/history storage
semantics**, and **distribution-channel lag**. The `is_desktop_android`
approach demonstrably ships but carries a long tail of phone-form-factor bugs.

## 8. Security architecture (as understood functionally)

- Inherits Vanadium/Chromium sandboxing, site isolation, safe-browsing and
  certificate validation; Titanium's deltas are small and UI-scoped except
  for the extension-surface expansion (which **increases** attack surface by
  design) and the intent scheme guards (which **reduce** it).
- Filter lists (EasyList/EasyPrivacy/anti-adblock) are fetched at build time
  into `titanium/android_config/filter_lists/` — ad/tracker blocking is partly
  engine-config data, partly extension-driven (uBlock Origin).
- No analytics/telemetry additions were observed in the patch layer; update
  provenance is via SLSA attestation on releases.

## 9. Useful implementation concepts (idea-level, no code copied)

1. `is_desktop_android = true` as the master switch for desktop extension UI on Android.
2. Phone toolbar extension-container + popup re-anchoring pattern.
3. Bundled-extension staging through external-extension prefs.
4. Store-adapter extension (DNR user-agent rules + content-script install buttons) instead of forking stores.
5. Build-time filter-list fetch for native blocking data.
6. External download manager via `intent://` forwarding from an extension.
7. Scheme-guard on external intents (`URLUtil.isNetworkUrl`).
8. Extension process importance boost.
9. SAF-tolerant unpacked-extension loading.
10. Cron-driven Vanadium bump + full rebuild + attested release pipeline.

## 10. Hard parts for an independent implementation

- Rebasing ~70 line-sensitive edits every Chromium milestone (with version
  conditionals already present in the reference).
- Phone popup/toolbar UX that upstream desktop code never designed for.
- MV2 support against upstream MV2 removal.
- Extension process lifetime vs. Android battery/memory behavior.
- Per-site breakage triage (YouTube embeds, networking regressions) per release.
- Reproducing all of the above **without** reusing GPL-2.0 patch text —
  see `docs/LICENSING_AUDIT.md` and `docs/FOUNDATION_DECISION.md`.

## 11. Source links (reference, read-only)

- Main repo: https://github.com/jqssun/android-titanium-browser
- Commits: https://github.com/jqssun/android-titanium-browser/commits/main
- Tags: https://github.com/jqssun/android-titanium-browser/tags
- Releases: https://github.com/jqssun/android-titanium-browser/releases
- Issues: https://github.com/jqssun/android-titanium-browser/issues
- Extension repo: https://github.com/jqssun/android-titanium-extension
- Extension manifest: https://github.com/jqssun/android-titanium-extension/blob/main/src/manifest.json
- Vanadium: https://github.com/GrapheneOS/Vanadium
- Chromium source: https://chromium.googlesource.com/chromium/src.git
