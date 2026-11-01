# Security

> Living document. Rules apply from Phase 2 onward; audited at Phase 6.

## Non-negotiables

- HTTPS by default; surface certificate errors honestly, never silently bypass.
- Extension installs show origin + requested permissions **before** install;
  per-extension private-mode gating; no blanket capability bypasses.
- Private browsing: isolated sessions, no history/cookie persistence, explicit
  data-clearing (per-site + global).
- Secure storage: tokens/keys in EncryptedSharedPreferences/Keystore, never
  plaintext; no secrets in source, logs, or git history.
- Network: no custom CA injection, no TLS interception, no proxy "features"
  that break certificate validation.
- Downloads: user-confirmed, progress + history, failure recovery, no silent
  execution of downloaded files.
- Permissions: least-privilege manifest; runtime Android permissions only with
  in-context rationale.
- No analytics/telemetry without explicit opt-in; no browsing-history
  collection, ever.
- Engine security updates: track Chromium milestones / GeckoView releases;
  document lag between upstream fix and our release.

## Explicitly out of scope (never implement)

DRM circumvention, stream ripping, account-sharing tricks, paywall/access-control
bypasses, ad-fraud-adjacent behavior, or anything that impersonates another
browser's identity (user-agent rules limited to documented compat cases).
