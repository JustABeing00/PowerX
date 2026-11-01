# Foundation Decision

> Status: **DECIDED 2026-10-31** (reversible only by recorded re-decision).
> Phase 0 (research + licensing audit) → complete. Phase 1 (this decision) → complete.

## Decision

1. **Long-term engine: independent Chromium-based browser (Path A).**
   Chrome-extension compatibility is the project's #1 requirement and only a
   full Chromium build provides it. Our implementation will be authored
   against upstream Chromium/Vanadium **documentation and behavior**, never by
   copying Titanium/Vanadium patch text (GPL-2.0).
2. **Immediate foundation: engine-abstracted native Android app
   (Kotlin) with a GeckoView proof-of-concept engine.** Rationale: it builds
   on Windows today, is a real engine with a real (Firefox) extension story,
   and proves out our UI, settings, storage, and engine-abstraction layers
   while the Chromium CI track is built.
3. **Rejected**: WebView-as-engine (zero extension APIs — would be fake
   compliance with the core requirement); GPL-derived Titanium fork (no
   recorded authorization; conflicts with the desired proprietary
   distribution option); renaming/copying Titanium sources (explicitly
   forbidden by project rules).
4. **Provisional product name: PowerX** (matches repo `JustABeing00/PowerX`;
   rename later without touching architecture).

## Consequences and commitments

- `BrowserEngine` interface owns all engine contact; app code must never
  import engine SDK classes directly outside `engine/*`.
- Chromium track: Linux CI (GitHub Actions or self-hosted), pinned
  milestone, independently authored patch queue, per-milestone
  `third_party` license re-audit. No Windows full-Chromium builds attempted.
- No auto-commit/auto-push/auto-release workflows without explicit approval.
- IP-lawyer review required before: first public APK, Play Store listing,
  any Path C consideration, codec enablement beyond baseline.
- This process is documented as **independent re-implementation**, explicitly
  **not** certified clean-room (researchers inspected GPL'd sources).

## Phase map

- [x] Phase 0 — research + licensing audit (`TITANIUM_RESEARCH`, `LICENSING_AUDIT`, `ENGINE_COMPARISON`)
- [x] Phase 1 — architecture decision (this file)
- [ ] Phase 2 — independent foundation (app shell, UI, engine abstraction, settings, tests)
- [ ] Phase 3 — engine proof of concept (GeckoView APK installs, browses, installs a Firefox extension)
- [ ] Phase 4 — browser implementation (tabs, history, downloads, content filtering, diagnostics)
- [ ] Phase 5 — UI refinement on-device
- [ ] Phase 6 — release prep (licensing review, security, full test matrix, docs)
- Parallel track: Chromium CI feasibility spike (Linux), recorded separately.
