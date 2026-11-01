# Architecture

> Independent, original design. Provisional name: **PowerX**.
> Layers are separated so the engine is replaceable (GeckoView now → Chromium module later).

```
app (Kotlin, original UI + branding)
├── ui/            Home, address bar, tab switcher, bookmarks, history,
│                  downloads, settings, extension manager, diagnostics
├── engine/        BrowserEngine interface + GeckoEngine impl (+ future ChromiumEngine)
│                  App code touches ONLY this boundary for web content.
├── contentblock/ Filter-list fetching, rule status, per-site allowlist
├── storage/       Bookmarks/history/downloads repositories (Room), secure prefs
├── settings/      Typed settings store, search-engine config, theme control
└── privacy/       Private-mode sessions, data-clearing, permission mediation
```

## Key decisions

- **Single-activity + fragments**, Material3, light/dark/system themes, original
  iconography (no Chromium/Titanium assets).
- **Engine boundary** (`engine/BrowserEngine.kt`): tabs/sessions, navigation,
  engine-backed extension operations (`install/list/setEnabled/uninstall`),
  content-blocking hooks, crash/telemetry-safe diagnostics. GeckoView types
  stay inside `engine/gecko/`; a future `engine/chromium/` module plugs the
  same interface.
- **Extensions**: honest capability labels per engine
  (supported / partial / unsupported / failed) driven by engine metadata, never
  faked. Gecko engine surfaces Firefox/AMO extensions; Chromium engine will
  surface Chrome-extension capabilities only when its integration truly exists.
- **Content blocking**: engine-native hooks where available + maintained
  extension (uBO build matching the engine) + fetched filter lists with
  attribution; no stacked duplicate blockers.
- **YouTube**: standard playback + PiP + fullscreen/orientation handling;
  blocking via supported content blocker + filter lists; no DRM/stream-ripper
  or access-control bypasses, ever.
- **Updates**: in-app update check scaffolding + documented APK provenance;
  engine security updates tracked per milestone (Chromium) / GeckoView
  release notes (Gecko).
- **Testing**: JVM unit tests for storage/settings/blocking logic; on-device
  manual matrix in `docs/TESTING.md`; no PASS claims without execution.
