# PowerX — Independent Android Browser

> Provisional name. Original project; no Titanium/Vanadium/Chromium code or
> assets vendored. Research references only — see `docs/`.

PowerX is a native Android browser built to one day run compatible Chrome
extensions via an independently implemented Chromium engine (Path A), starting
from an engine-abstracted Kotlin foundation with a GeckoView proof-of-concept
engine that builds on Windows today.

## Docs

| Doc | Purpose |
|---|---|
| `docs/TITANIUM_RESEARCH.md` | Reference-project functional analysis (no code copied) |
| `docs/LICENSING_AUDIT.md` | License table + Paths A/B/C |
| `docs/ENGINE_COMPARISON.md` | Engine options + recommendation |
| `docs/FOUNDATION_DECISION.md` | Recorded architecture decision + phase map |
| `docs/ARCHITECTURE.md` | App layering + engine boundary |
| `docs/BUILDING.md` | How to build the APK (Windows) + Chromium CI track (Linux) |
| `docs/SECURITY.md` | Security rules + explicit non-goals |
| `docs/TESTING.md` | Test matrix (PASS/FAIL/NOT TESTED/BLOCKED) |
| `docs/THIRD_PARTY_NOTICES.md` | Dependency + license inventory |
| `progress.md` | Running log of phases, files, results, blockers |

## Quick start

See `docs/BUILDING.md`. Debug APK:

```powershell
& "$env:ANDROID_HOME\gradle-8.7\bin\gradle" assembleDebug
```

## Rules (binding)

- Never copy Titanium/Vanadium/Chromium implementation, assets, or branding.
- App code touches web content only through `engine/BrowserEngine`.
- Never claim extension support an engine doesn't have — capability labels only.
- No secrets in git; no auto-push/release CI without explicit approval.
