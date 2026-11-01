# Third-Party Notices (inventory — update on every dependency change)

| Component | Version | License | Source | Notes |
|---|---|---|---|---|
| AndroidX core/appcompat/material | (pinned at scaffold) | Apache-2.0 | https://github.com/androidx/androidx | UI foundation |
| GeckoView | 152.0.20260612001812 (POM-verified: core 1.18.0, media3 1.10.1; 159.x needs unpublished SDK 37 — revisit) | MPL-2.0 | https://maven.mozilla.org/maven2/org/mozilla/geckoview/ · https://mozilla.org/MPL/ | Engine PoC; file-level copyleft observed; used unmodified |
| Kotlin stdlib / coroutines | (via AGP) | Apache-2.0 | https://github.com/JetBrains/kotlin | — |
| (Chromium track, future) | pinned tag TBD | BSD-3-Clause + per-dep audit | https://chromium.googlesource.com | Full `third_party` audit required at pin time |

> No Titanium/Vanadium/Chromium code or assets are vendored in this repo.
> Filter lists are fetched at runtime/build-time with attribution preserved —
> never committed as copied sources without license headers intact.
