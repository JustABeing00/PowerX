# Licensing Audit

> Research date: 2026-10-31. This is **technical information, not legal
> advice**. For Play Store / commercial distribution, have a qualified
> intellectual-property lawyer review the final dependency list and the
> chosen distribution model before release.

## 1. Component table

| Component | License | What it does | Distribution implications |
|---|---|---|---|
| Titanium Browser repo scripts/patches (`build.sh`, `patch.sh`, `extensions/*`, `res/*`, `args.gn`, `.gclient`, workflows) | **GPL-2.0** (repo `LICENSE` is GPL v2 full text) | Patch-and-build layer over Chromium/Vanadium; contains the extension-support modifications | Any work **based on** these files distributed as a whole must be licensed GPL-2.0 with complete corresponding source offered (§3). **Cannot be folded into a closed-source proprietary browser.** Commercial sale is allowed, but source disclosure is mandatory. |
| Titanium companion extension (`android-titanium-extension`) | **GPL-2.0** | MV3 store-adapter/download-manager UX extension | Same copyleft consequence if derived from; bundling its binary also triggers source-offer duties for that component. Reimplement store-adapter UX from scratch to avoid this. |
| Vanadium patch set | **GPL-2.0-only** (+ exception notes: `LICENSE.WebView-note`, `LICENSE.Apache-2.0-note`, `LICENSE.FTL-note`) | Hardened Chromium patch set consumed as submodule | Same as above: deriving our patch set from Vanadium's patch text = GPL-2.0-only obligations on the derived patch set + source disclosure. Reading it for ideas does not itself create obligations; **copying/adapting patch text does**. |
| Chromium (upstream `src/`) | **BSD-3-Clause** (core) | Browser engine, //chrome Android UI, extension system | Permissive: commercial, closed-source, and modified redistribution allowed with copyright-notice retention and no-endorsement rule. Suitable for proprietary distribution **provided third-party deps below are honored**. |
| Chromium `third_party/` deps | **Mixed**: BSD/MIT/Apache-2.0 (majority), plus reciprocal (MPL, APSL) and restricted (LGPL-2.1, GPL-2.0 — require per-dep approval in Chromium's own process) | Codecs, crypto, fonts, parsers, etc. | Must be reviewed **per dependency** at pin time (`third_party/*/README.chromium` + `LICENSE` files). Chromium's own docs state GPL-family deps need case-by-case approval; our `chrome_public_apk` equivalent must ship `chrome://credits`-style attribution. Some codecs (e.g. proprietary-codec flags like `proprietary_codecs = true`, AAC/H.264) implicate **patent licensing**, not just copyright — separate clearance needed for Play Store distribution. |
| GeckoView / Mozilla Android Components | **MPL-2.0** | Alternative engine + browser UI components | Weak copyleft, **file-level**: modifications to MPL files must be disclosed under MPL; can be combined into a larger proprietary work. Commercial + Play Store distribution explicitly fine. No Chrome-extension support (see `docs/ENGINE_COMPARISON.md`). |
| Android System WebView / AndroidX WebKit | Apache-2.0 (client libs); system component varies by device | Embeddable renderer with no extension APIs | Permissive for app code, but **no extension capability** — unsuitable for the project's core requirement. |
| EasyList / EasyPrivacy / anti-adblock filter lists | Varies (generally GPL-family/custom with attribution practice; check each list's header at fetch time) | Build-time fetched blocking data (`filter_lists/`) | Redistribution inside the APK may trigger share-alike/attribution duties **for the lists**. Preserve list headers + attribution screen; verify each list's terms before bundling, or fetch lists at runtime post-install instead of shipping them. |
| Fonts/icons/images (this project) | To be **original or permissively licensed** (Apache-2.0 / OFL / CC0) | App identity, no Chromium/Titanium assets reused | Keep a `THIRD_PARTY_NOTICES` inventory from day one; never import Titanium's `res/` glyphs or Chromium brand marks. |
| Trademarks ("Chrome", store badges, Titanium/Vanadium names) | Trademark law, separate from copyright | Branding | Do not use Chrome branding; use an original name (provisional: **PowerX**); comply with store-badge and EasyList attribution rules. |

## 2. Key distinctions (must-read)

1. **GPL does not ban commercial distribution** — it mandates freedom to
   share/change: complete corresponding source + license preservation + no
   further restrictions. Play Store paid/free distribution under GPL is
   allowed **if** source is offered and the APK contains no incompatible
   proprietary lockdown (Play signing itself is fine; Google Play's terms vs.
   GPL has history — keep the GitHub source release as the canonical offer).
2. **A different app name removes zero copyright obligations.** Renaming
   Titanium's package/file names while reusing its patch logic still creates
   a derivative work of GPL-2.0 material.
3. **Rewriting after inspection is not automatically "clean".** Subconscious
   copying of non-trivial expression (exact sed targets, helper structure,
   asset bytes) can still be derivative. True clean-room needs an isolated
   implementer; our process is documented as **independent re-implementation
   from upstream docs**, explicitly **not** certified clean-room
   (see `docs/FOUNDATION_DECISION.md` §5).
4. **Chromium BSD ≠ whole-product BSD.** The `chrome_public_apk` target pulls
   in LGPL/GPL-family and patent-encumbered third-party code; each pinned
   milestone needs a fresh `third_party` review + `chrome://credits`
   equivalent.
5. **Vanadium GPL-2.0-only is stricter than "GPL-2.0-or-later"**: no upgrade
   path to v3 terms; combined GPL-2.0-only + Apache-2.0 code has known
   friction — another reason to keep our patch layer independently authored
   against upstream Chromium instead of forking Vanadium's patch text.

## 3. The three paths

### PATH A — Independently implemented Chromium-based browser (RECOMMENDED long-term)

- Use upstream Chromium (BSD) + independently authored patches/config for
  Android extension support; no Titanium/Vanadium patch text reused.
- Obligations: BSD attribution, per-dep `third_party` audit, codec/patent
  clearance, trademark-safe branding.
- Compatible with: commercial Play Store distribution, closed-source UI
  layers, open-source release — **any model**, provided audits are done.
- Cost: very high engineering + build-infra cost (see `docs/BUILDING.md`;
  Linux builders, ~100 GB+ checkouts, per-milestone rebases).

### PATH B — GeckoView-based browser

- Engine + components under MPL-2.0; file-level copyleft only.
- Compatible with commercial/proprietary distribution with MPL file
  disclosure for modified Mozilla files.
- **Decisive limitation**: runs Firefox WebExtensions (AMO ecosystem,
  1000+ mobile extensions since 2024 incl. uBlock Origin), **not**
  unmodified Chrome Web Store extensions. Fails the project's #1 requirement
  as the final engine, but is the best **buildable-today** proof-of-concept
  engine (Maven artifact, Windows-buildable).

### PATH C — GPL-compliant Titanium-derived browser

- Fork Titanium's layer, keep everything GPL-2.0, publish full source +
  build scripts with every release.
- Compatible with: open-source distribution incl. Play Store free/paid **as
  GPL software**; **incompatible with**: closed-source/proprietary model,
  and any Play policy or SDK term that would add "further restrictions"
  beyond GPL-2.0.
- Lowest engineering cost, highest licensing constraint. Kept as fallback if
  Path A proves unaffordable — requires an explicit, recorded decision first.

No commercial/permission licensing path exists for Titanium's GPL layer (no
CLA/dual-license offer observed); Chromium itself needs no permission beyond
its BSD terms.

## 4. Required hygiene for this repo (enforced from Phase 0)

- `docs/THIRD_PARTY_NOTICES.md`: every dependency + license + source URL
  (started in Phase 2, reviewed every release).
- No `C:` writes, no Titanium file copies, no secrets in git history.
- No auto-commit/auto-push/release CI workflows without explicit approval.
- IP-lawyer review flagged for: final distribution model, codec patents,
  filter-list bundling terms, Play Store GPL interplay (Path C only).
