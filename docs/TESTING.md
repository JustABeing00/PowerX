# Testing

> Rule: only four states — **PASS / FAIL / NOT TESTED / BLOCKED**.
> Never report PASS without execution.
>
> Device used 2026-11-01: OnePlus CPH2239 (Android 11, arm64-v8a),
> connected via adb; UI driven via taps + uiautomator dumps, plus manual
> on-phone confirmation by the user for dialogs/video.

## Manual matrix (on-device)

### Core browsing
| # | Case | Result |
|---|---|---|
| T01 | APK generates via `assembleDebug` | PASS (BUILD SUCCESSFUL ×6; 531 MB universal debug APK) |
| T01b | APK installs on arm64 device | PASS (`adb install -r` → Success, multiple reinstalls) |
| T02 | Launch, no crash on cold start | PASS (resumed/focused, no FATAL, repeated launches) |
| T03 | HTTPS navigation via address bar | PASS (example.com: URL + "Example Domain" content in dump) |
| T04 | Back/forward/reload | PASS back (DDG re-rendered, button states flipped); forward state verified; reload NOT TESTED |
| T05 | Multiple tabs | NOT TESTED (single-tab PoC; tab UI is Phase 4) |
| T06 | Bookmarks | NOT TESTED (no UI yet; Phase 4) |
| T07 | History persistence UI | NOT TESTED (recorded internally; no viewer yet) |
| T08 | Downloads | NOT TESTED (no UI yet) |
| T09 | Private mode | NOT TESTED (not implemented) |
| T10 | Theme switching | NOT TESTED (Settings renders with System/Light/Dark options; switch not exercised) |
| T11 | Settings screen renders/saves | PASS render (homepage DDG, 5 engines, theme, Save all present); save flow NOT TESTED |
| T12 | Non-exported activities enforced | PASS (`am start` ExtensionsActivity → SecurityException, as designed) |

### Engine extensions (Gecko proof-of-concept)
| # | Case | Result |
|---|---|---|
| E01 | Install uBlock Origin (Firefox build) from AMO | PASS (`.../latest/ublock-origin/latest.xpi` → "Installed: uBlock0@raymondhill.net", listed; manifest warnings for desktop-only perms logged, expected) |
| E02 | Enable/disable/remove extension | Partial: uninstall PASS (confirm dialog → "Uninstalled", list empty); enable/disable NOT TESTED (not implemented — Phase 4) |
| E03 | Extension permission prompt shown pre-install | PASS ("Install extension?" dialog with ID + permissions; user tapped Allow; Deny path NOT TESTED) |
| E04 | Extension persists across restart | NOT TESTED (needs kill→relaunch→list check; Phase 3 wrap-up) |
| E05 | Chrome Web Store install correctly reported unsupported | Partial: capability label rendered in UI; store-visit behavior NOT TESTED |

### Content filtering + YouTube
| # | Case | Result |
|---|---|---|
| Y01 | youtube.com loads | PASS (m.youtube.com rendered: Shorts + video list + bottom nav in dump) |
| Y02 | Actual video playback with audio | NOT TESTED (needs on-screen confirmation; user to verify) |
| Y03 | Fullscreen / PiP | NOT TESTED |
| Y04 | Blocking toggle + per-site allowlist | NOT TESTED (no UI yet) |
| Y05 | Behavior after network interruption | NOT TESTED |

### Security
| # | Case | Result |
|---|---|---|
| S01 | Invalid-cert site warns/blocks | NOT TESTED |
| S02 | Clear-all-data removes history/cookies/cache | NOT TESTED (no UI yet) |
| S03 | No secrets in logcat during browsing | PASS (no keys/tokens in captured logs; none exist in app) |

## Bugs found on-device (all fixed, verified by rebuild + retest)
1. Address-bar typing interleaved with async location updates
   ("aboexample.comut:blank") → `setSelectAllOnFocus` + no-clobber-while-editing + focus/keyboard commit in `navigate()`.
2. Second `GeckoRuntime` per process (`Only one GeckoRuntime instance is
   allowed` → resume-timeout to launcher) → process-wide `EngineProvider`
   singleton + `singleTask` + `onNewIntent` link routing.
3. Application ran engine init in Gecko child processes (silent death loading
   YouTube) → `PowerXApp` main-process guard.

## Automated
- JVM unit tests: NOT TESTED (no test source set yet; target Phase 4).
