# Testing

> Rule: only four states — **PASS / FAIL / NOT TESTED / BLOCKED**.
> Never report PASS without execution.

## Manual matrix (on-device)

> 2026-11-01: APK generation verified (BUILD SUCCESSFUL ×2, 531 MB universal
> debug APK). No device/emulator attached → install and all runtime cases are
> BLOCKED until a device is available.

### Core browsing
| # | Case | Result |
|---|---|---|
| T01 | APK generates via `assembleDebug` | PASS |
| T01b | APK installs on arm64 device | BLOCKED — no device |
| T02 | Launch, no crash on cold start | BLOCKED — no device |
| T03 | HTTPS navigation via address bar | NOT TESTED |
| T04 | Back/forward/reload/stop | NOT TESTED |
| T05 | Multiple tabs, switch, close, restore | NOT TESTED |
| T06 | Bookmarks add/edit/remove | NOT TESTED |
| T07 | History records + clear single/all | NOT TESTED |
| T08 | Download file, progress, open, failure recovery | NOT TESTED |
| T09 | Private mode isolation (no persistence) | NOT TESTED |
| T10 | Light/dark/system theme | NOT TESTED |

### Engine extensions (Gecko proof-of-concept)
| # | Case | Result |
|---|---|---|
| E01 | Install uBlock Origin (Firefox build) from AMO | NOT TESTED |
| E02 | Enable/disable/remove extension | NOT TESTED |
| E03 | Extension permission prompt shown pre-install | NOT TESTED |
| E04 | Extension persists across restart | NOT TESTED |
| E05 | Chrome Web Store install **correctly reported unsupported** on Gecko engine | NOT TESTED |

### Content filtering + YouTube
| # | Case | Result |
|---|---|---|
| Y01 | youtube.com loads, video plays with audio | NOT TESTED |
| Y02 | Fullscreen + landscape | NOT TESTED |
| Y03 | Picture-in-picture (where supported) | NOT TESTED |
| Y04 | Blocking toggle + per-site allowlist | NOT TESTED |
| Y05 | Behavior after network interruption | NOT TESTED |

### Security
| # | Case | Result |
|---|---|---|
| S01 | Invalid-cert site warns/blocks | NOT TESTED |
| S02 | Clear-all-data removes history/cookies/cache | NOT TESTED |
| S03 | No secrets in logcat during browsing | NOT TESTED |

## Automated
- JVM unit tests (settings, storage repositories, filter-list parsing) run via
  Gradle `testDebugUnitTest` once the foundation lands. Target: Phase 4.
