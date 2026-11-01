# Engine Comparison

> Research date: 2026-10-31. Evidence-backed assessment for the project's
> core requirement: **compatible Chrome extensions running on Android**.

## Comparison table

| Technology | Extension compatibility | Android integration | Licensing | Complexity |
|---|---|---|---|---|
| **Full Chromium build** (`chrome_public_apk`, desktop-extension stack enabled) | **Full Chrome MV2+MV3 compat** (Web Store, Opera/Edge stores, CRX/ZIP sideload, unpacked via SAF): the only option that runs Chrome extensions unmodified. Requires engine-level work: desktop extension UI compiled in, install allow-listing, phone toolbar/popup hosting, process lifetime, SAF-tolerant loading | Native; full control of UI, permissions, updates; must implement updater + crash/ANR triage per milestone | Upstream BSD-3 + per-dep `third_party` audit (some LGPL/GPL-family + codec patents); independently authored patches keep proprietary distribution open | **Very high**: Linux builders, depot_tools/GN/Ninja, ~100 GB checkouts, multi-hour builds, per-milestone rebase of line-sensitive edits |
| **GeckoView** (Mozilla, Maven artifact) | **Firefox WebExtensions only** (AMO; 1000+ mobile extensions incl. uBlock Origin, Dark Reader, Privacy Badger). Chrome Web Store extensions do **not** install; Chrome-only APIs need porting. Honest, working extension story — but a different ecosystem | Excellent: `GeckoView` + `WebExtensionController` install/enable/disable/uninstall, private-mode gating, native-messaging bridge; purpose-built for browsers | MPL-2.0 (file-level copyleft; commercial/proprietary OK with disclosure of modified MPL files) | **Low–medium**: Gradle dependency, Windows-buildable, documented API |
| **Android System WebView** (+ AndroidX WebKit) | **None**: no extension APIs, no background/service-worker extension contexts, no `chrome.*`/`browser.*` surface. An "extension manager" UI on top changes nothing at runtime | Easy embedding, but version varies per device; no browser chrome; shares no data with Chrome | Apache-2.0 client libs; device component varies | Low — and **incapable** of the core requirement |
| **Other wrappers** (custom tabs, WebView forks, cloud-rendering shells) | None (inherits WebView limits) or ToS-breaking (account/DRM circumvention risks) | Thin | Varies; often incompatible with store policies | Not viable — rejected |

## What must live at engine level vs. app UI (Chrome-extension case)

**Engine-level (cannot be faked in app code):**
extension install pipeline + signature/off-store checks; MV2/MV3 manifest
handling; background service workers and event pages; content-script
injection; `chrome.*` API implementations (tabs, storage, cookies,
contextMenus, downloads, declarativeNetRequest, …); extension renderer
process model + lifetime; permission prompts and incognito gating; update
machinery; `chrome://extensions` engine pages.

**App-UI-level (legitimately ours):**
extension manager screens (list/enable/disable/remove), permission
explanations, popup/action-button hosting surfaces, diagnostics and
compat labels, store-adapter helpers, per-site toggles.

Rule: if a feature needs a `chrome.*` runtime behavior, it is engine work.
Claiming otherwise is how WebView-wrappers mislead users — we will not do that.

## Manifest/requirement assessment

- **MV3**: supported by modern Chromium milestones; Firefox/GeckoView also
  supports MV3-era APIs (with `gecko_android` compat signaling). Either path
  must track MV3 service-worker semantics (no persistent backgrounds).
- **MV2**: Chromium upstream is removing MV2 paths; keeping MV2 (needed for
  full uBlock Origin) on Path A means carrying allow-list shims per
  milestone — the highest-maintenance item in the reference layer.
- **uBlock Origin / ad blockers**: fullest on Path A (MV2 uBO + DNR);
  on GeckoView, uBO for Firefox Mobile is officially supported and excellent
  for YouTube/general browsing, but it is the Firefox build, not the Chrome build.
- **Incognito**: both engines gate per-extension private access; Titanium's
  reference forces OTR-capable process handling — on our build this must be a
  deliberate, reviewed behavior, not a blanket bypass.
- **YouTube**: no engine gives permanent ad-free guarantees; both paths rely
  on maintained filter lists + a supported content blocker, plus PiP/media
  integration at the app layer.

## Recommendation

**Long-term: Path A (independent Chromium-based build)** — it is the only
architecture that satisfies the Chrome-extension requirement. Accept the cost
openly: Linux CI builders, pinned milestones, per-release QA
(`docs/TESTING.md`), and a maintained rebase discipline.

**Immediate: build the app foundation engine-agnostic with a GeckoView
proof-of-concept engine** (Windows-buildable today, real extensions from
AMO), keeping a `BrowserEngine` abstraction boundary so the Chromium engine
module can replace it without rewriting the app. This gives a genuine,
installable browser now while the Chromium CI track is established — and it
never misrepresents WebView as extension-capable.

Details and decision record: `docs/FOUNDATION_DECISION.md`. Build realities:
`docs/BUILDING.md`.
