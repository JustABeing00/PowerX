# Building PowerX

> Environment baseline verified 2026-10-31 (Windows 11, VS Code + OpenCode):
> Java 21 (Microsoft build), Android SDK (`ANDROID_HOME` set) with
> `platforms/android-34` (+ android-36 installed for GeckoView 159's media3
> dependency), `build-tools/34.0.0` (+ 36.0.0), `cmdline-tools/latest`,
> `platform-tools` (adb 1.0.41), Gradle 8.7 distribution present.
> No system Gradle on PATH — use the wrapper / SDK Gradle.

## 1. Foundation app (Windows, supported today)

Requirements: JDK 17+, Android SDK 34, acceptance of SDK licenses.

```powershell
cd E:\PowerX
# Gradle via SDK distribution (first run downloads Android Gradle Plugin + deps)
& "$env:ANDROID_HOME\gradle-8.7\bin\gradle" assembleDebug
# install on a connected device / emulator
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

Then: launch **PowerX**, load `https://www.example.com`,
`https://www.youtube.com/`, test tabs/history/downloads/private mode per
`docs/TESTING.md`. Never commit signing keys; debug builds only for now —
release signing is documented at release time in Phase 6.

## 2. Full Chromium engine track (Linux CI — NOT Windows)

Do **not** attempt a Chromium checkout on this Windows machine. Requirements
(indicative, verify against current Chromium docs at pin time):

- 64-bit Linux (Ubuntu LTS per upstream), `depot_tools`, GN + Ninja (via hooks),
  Android SDK/NDK (via hooks), Python 3.
- **~100–200 GB disk**, 16 GB+ RAM (32 GB recommended), multi-core CPU;
  first builds take several hours even on strong hardware.
- Network must reach `chromium.googlesource.com` + dependency hosts.

Reproducible sketch (mirrors the *idea* of the reference pipeline, authored
fresh; exact commands re-verified at spike time):

1. `fetch android` / pinned-tag checkout of `chromium/src`.
2. `gclient sync -D --no-history --nohooks` + `gclient runhooks`.
3. Apply **our own** patch queue (kept in `chromium-patches/`, authored
   against upstream docs — never Titanium/Vanadium patch text).
4. `gn gen out/Default` with our `args.gn` analogue; `autoninja chrome_public_apk`.
5. Sign with CI-secret keys; publish GitHub release with attestation.
   Mirror the reference's *categories* of CI steps (build → sign → release →
   attest) but **never** its auto-commit/auto-push-to-main behavior.

## 3. Reproducibility rules

- Pinned versions recorded in `progress.md` + release notes (Chromium tag,
  GeckoView version, AGP, build-tools).
- CI must be read-only toward the repo (no `contents: write`, no self-push)
  unless explicitly authorized.
- Every APK reports PASS/FAIL per `docs/TESTING.md`; untested = NOT TESTED.
