# Progress Log

## 2026-10-31 — Phase 0: research + licensing audit (DONE)

- Verified workspace: empty repo `JustABeing00/PowerX`, branch `main`, no commits.
- Inspected Titanium ZIP read-only: structure, README, GPL-2.0 LICENSE,
  `.gitmodules` (Vanadium submodule), `build.sh`, `patch.sh` (~190 lines),
  `common.sh`, `args.gn` (`is_desktop_android=true`, pkg `io.github.jqssun.helium`),
  `.gclient` (3 hooks), `extensions/` bundling, `res/` original glyph,
  `.github/workflows/build.yml` (self-hosted, auto-push Vanadium bumps, attest).
- **Finding**: ZIP `vanadium/` is empty (submodule not archived) — not buildable alone.
- Upstream observed: `main`, 111 commits, latest release v156.0.8078.79
  (2026-10-30), tags v151–v156, 161 issues / 5 PRs, Vanadium pin `0202b7b`.
- Extension repo: 3 commits, MV3, permissions
  (downloads/tabs/cookies/contextMenus/storage/DNR+`<all_urls>`), store adapters.
- Vanadium: own LICENSE = GPL-2.0-only + exception notes.
- Created: `docs/TITANIUM_RESEARCH.md`, `docs/LICENSING_AUDIT.md`,
  `docs/ENGINE_COMPARISON.md`.
- Tests: none (research phase). No code copied; Titanium untouched.

## 2026-10-31 — Phase 1: architecture decision (DONE)

- Decided: Path A long-term (independent Chromium), GeckoView PoC now,
  WebView and GPL-fork rejected. Recorded in `docs/FOUNDATION_DECISION.md`.
- Created: `docs/ARCHITECTURE.md`, `docs/BUILDING.md`, `docs/SECURITY.md`,
  `docs/TESTING.md`, `docs/THIRD_PARTY_NOTICES.md`, `README.md`.
- Environment verified: Java 21, SDK android-34 + build-tools 34.0.0,
  adb present, Gradle 8.7 in SDK, no python3 (use PowerShell).

## 2026-11-01 — Phase 2: first successful build (DONE)

- `assembleDebug` **BUILD SUCCESSFUL** (build-seventh.log, 36 tasks).
  Artifact: `app/build/outputs/apk/debug/app-debug.apk` (~531 MB, universal,
  GeckoView native libs for all ABIs — expected size, will split per-ABI later).
- Fixes from compiler evidence: nested-comment trap in KDoc, GV152 4-arg
  `onLocationChange`, delegate-tracked back/forward state, explicit SAM types
  in `consume()` helper.
- Warnings cleanup: `onProgress` param-name shadowing, `onBackPressed` →
  `OnBackPressedDispatcher` (rebuilt in build #8).
- Blocker: **no Android device or emulator attached** (`adb devices` empty).
  Emulator would need hypervisor platform changes — not done without approval.
  All on-device tests remain NOT TESTED/BLOCKED; see `docs/TESTING.md`.

- [x] Scaffold Gradle/Kotlin app: engine abstraction + Gecko engine + original UI
- [x] Toolchain, final: SDK 37 is NOT published publicly (sdkmanager:
  "Failed to find package 'platforms;android-37'"), so GeckoView 159 is
  unbuildable here. Using SDK 36 + build-tools 36.0.0 + AGP 9.5.0 +
  Gradle 9.8.0 (workspace-local `.tools/`, verified running) + Kotlin 2.1.0
  + GeckoView 152 (core 1.18.0 fits SDK 36). Full chain evidence in
  build-*.log and GeckoView POMs.
- [x] AGP 9.5 finding (evidence: build error text): "The
  'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin
  support since AGP 9.0" — AGP bundles Kotlin; KGP removed from both build
  files, `kotlinOptions` dropped. Rebuilding.
- [ ] `assembleDebug` with modern toolchain; record result (running)
- [ ] Phase 3 PoC: browse + install Firefox extension (uBO) on-device
- Known gap (Phase 4): pre-install permission prompt via
  `WebExtensionController.PromptDelegate.onInstallPromptRequest`
  (signatures verified in GeckoView 161 javadoc; needs AllowOrDeny dialog wiring).
  Test E03 stays NOT TESTED until then.
