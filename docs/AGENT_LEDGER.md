# ShopIt Agent Work Ledger

Last updated: 2026-11-02 11:25 UTC
Repository: https://github.com/Karthik9151/ShopIt
Baseline branch: `main`
Baseline commit audited: `260ea492765a98f4226c5a661e66a7186cd3a3b9`

> This ledger tracks the ordered remediation packages. A PR is never considered merged until the owner confirms that GitHub merged it. No application changes are implied by a PLANNED or BLOCKED status.

## Work packages

| WP id | Review findings | Status | Branch | PR link | Checks run | Owner actions pending |
|---|---|---|---|---|---|---|
| WP-00 | F1-F17, N1-N6 | MERGED | `wp00-ledger` | https://github.com/Karthik9151/ShopIt/pull/6 | PR CI run 41430681265: Maven/MySQL, frontend tests and production build all passed. | None; backup branch not found remotely and was not touched |
| WP-01 | F1 | MERGED | `wp01-admin-bootstrap` | https://github.com/Karthik9151/ShopIt/pull/7 | PR CI run 41431237299: Maven/MySQL, frontend tests, production build and Docker build all passed. | Optional owner-only APP_ADMIN_TAKE_OVER_EXISTING one-deploy use; default remains false |
| WP-02 | F2, F14 | MERGED | `wp02-pricing` | https://github.com/Karthik9151/ShopIt/pull/8 | PR CI run 41432755002: Maven/MySQL, frontend tests and production build all passed. | Keep backend and frontend free-shipping thresholds aligned when owner changes config |
| WP-03 | F4 | MERGED | `wp03-error-handling` | https://github.com/Karthik9151/ShopIt/pull/9 | PR CI run 41433524474: Maven/MySQL, frontend tests and production build all passed. | None |
| WP-04 | F5 | MERGED | `wp04-session-expiry` | https://github.com/Karthik9151/ShopIt/pull/10 | PR CI run 41434567635: Maven/MySQL, frontend tests and production build all passed. | None |
| WP-05 | F6, F17 | MERGED | `wp05-order-history` | https://github.com/Karthik9151/ShopIt/pull/11 | PR CI run 41434916735: Maven/MySQL, frontend tests and production build all passed. | Verify Flyway V5 on staging and preserve actor attribution during any rollback; TiDB remains unverified |
| WP-06 | F7 | PR OPEN — DRAFT | `wp06-auto-cancel` | https://github.com/Karthik9151/ShopIt/pull/12 | CI run 41436577576: Maven/MySQL verify, backend container build, frontend tests and production build all passed. Local commands unavailable. | Render free service only sweeps while awake; startup sweep catches up after wake |
| WP-07 | F9 | PR OPEN — DRAFT | `wp07-cart-availability` | https://github.com/Karthik9151/ShopIt/pull/13 | CI run 41437052277: Maven/MySQL verify, backend container build, frontend tests and production build all passed. Local commands unavailable. | None; remove unavailable product rows before checkout |
| WP-08 | F3 | PR OPEN — DRAFT | `wp08-payment-switch` | https://github.com/Karthik9151/ShopIt/pull/14 | CI run 41437483366: Maven/MySQL verify, backend container build, frontend tests and production build all passed. Local commands unavailable. | Set APP_PAYMENTS_SIMULATED_ENABLED=true on staging only if demo payments are required; never in production |
| WP-09 | F8 | PR OPEN — DRAFT | `wp09-rate-limiting` | https://github.com/Karthik9151/ShopIt/pull/15 | First run 41438195716 failed compile; corrected. Retry 41438476868 and latest ledger run 41438543941 passed Maven/MySQL, frontend tests/build and backend container build. | Confirm trusted proxy overwrites X-Forwarded-For before enabling APP_RATE_LIMIT_TRUST_FORWARDED_FOR |
| WP-10 | F12 | PR OPEN — DRAFT | `wp10-public-hardening` | https://github.com/Karthik9151/ShopIt/pull/16 | Corrected CI run 41438484902 and latest ledger run 41438550884 passed Maven/MySQL, frontend tests/build and backend Docker build. | Owner to inspect live report-only CSP violations; no HSTS or enforced CSP |
| WP-13 | F10, F15 | BLOCKED | `wp13-cold-start-jvm` | — | — | Optional uptime monitor intentionally not enabled; no Render settings changes |
| WP-14 | F16 | BLOCKED | `wp14-admin-pagination` | — | — | Review paginated admin API contract |
| WP-15 | Operations | BLOCKED | `wp15-operations` | — | — | Provide/verify operational recovery and database backup procedures |
| WP-16 | Test coverage, N5 | BLOCKED | `wp16-e2e-testing` | — | — | — |
| WP-17 | CI security, N6 | BLOCKED | `wp17-ci-security` | — | — | Enable secret scanning/push protection, Dependabot alerts and main branch protection in GitHub settings |
| WP-18 | TiDB verification | BLOCKED | `wp18-tidb-ci` | — | — | Run scripts/verify-tidb.sh on staging and verify flyway_schema_history; no staging DB access performed |
| WP-19 | Readability | BLOCKED | `wp19-readability` | — | — | — |
| WP-20 | Documentation | BLOCKED | `wp20-docs-accuracy` | — | — | Confirm logout route documentation choice; default is document current public behavior |
| WP-21 | Accessibility and SEO | BLOCKED | `wp21-accessibility` | — | — | — |
| WP-22 | Demo profile | BLOCKED | `wp22-demo-profile` | — | — | Keep demo profile local-only; no Render configuration changes |
| WP-23 | Optional features (23a-23h) | BLOCKED | — | — | — | Owner must select one feature and approve its separate plan |

## Project goals

| Project goal | WP ids | Status |
|---|---|---|
| Secure administrator access | WP-01, WP-08, WP-09, WP-10, WP-11 | NOT STARTED |
| Correct pricing | WP-02 | NOT STARTED |
| Reliable checkout | WP-02, WP-05, WP-06, WP-07, WP-08, WP-11 | NOT STARTED |
| Safe operations | WP-03, WP-09, WP-10, WP-13, WP-15, WP-17, WP-18 | NOT STARTED |
| Public data minimization | WP-12 | NOT STARTED |
| Scalable admin operations | WP-14 | NOT STARTED |
| Test coverage | WP-03 through WP-18 | NOT STARTED |
| Readable code | WP-19 | NOT STARTED |
| Accurate documentation | WP-00, WP-15, WP-20 | IN PROGRESS |
| Accessibility and SEO | WP-21 | NOT STARTED |
| Isolated local demo | WP-22 | NOT STARTED |

## Audit findings

### Original independent tester findings

| ID | Severity | Audit result | Evidence / disposition |
|---|---|---|---|
| F1 | High | CONFIRMED | AdminBootstrap silently returns for configured existing admin email and compares the untrimmed value. See AdminBootstrap.java L9-L13. |
| F2 | High | CONFIRMED | Frontend promises free shipping above INR 1,999; OrderService always charges INR 49. See App.jsx L24 and OrderService.java L16-L17, L75-L81. |
| F3 | High | CONFIRMED | Authenticated owner can simulate own payment and transition PENDING_PAYMENT to CONFIRMED. See PaymentController.java L6-L10 and PaymentService.java L11-L16. |
| F4 | Medium | CONFIRMED | Catch-all Exception maps unhandled failures to 500 and does not log them. See ApiExceptionHandler.java L8-L15. |
| F5 | Medium | CONFIRMED | Axios uses 12-second timeout and lacks a 401 response interceptor. See api.js L1-L4 and application.yml L29-L32. |
| F6 | Medium | CONFIRMED | updateStatus checks supported status names, not legal transitions/payment prerequisite. See OrderService.java L119-L137. |
| F7 | Medium | CONFIRMED | Checkout reduces inventory and only manual cancellation restores it; no scheduled expiry sweep found. See OrderService.java L51-L58, L101-L111, L139-L149. |
| F8 | Medium | CONFIRMED | No rate limiter found on login, registration, checkout or password change. |
| F9 | Medium | CONFIRMED | Cart mapping does not mark inactive products unavailable; checkout rejects them. See CartService.java L15-L20 and OrderService.java L51-L56. |
| F10 | Medium | CONFIRMED | Frontend API timeout is 12 seconds. See api.js L2. Approximately one-minute cold start is documented in docs/DEPLOYMENT.md L132-L138; it was not independently measured during audit. |
| F11 | Medium | CONFIRMED | Registration duplicate pre-check returns 409, but concurrent insert constraint errors are not translated. See AuthService.java L32-L43 and V1__initial_schema.sql L1-L4. |
| F12 | Low | CONFIRMED | Swagger UI and OpenAPI docs are permitAll. See SecurityConfig.java L23-L25. |
| F13 | Low | CONFIRMED | Public ProductView includes exact stock. See ApiDtos.java L20-L21 and ProductService.java L96-L100. |
| F14 | Low | CONFIRMED | Shipping/tax constants duplicated in OrderService and CouponService. See OrderService.java L16-L17 and CouponService.java L16-L18. |
| F15 | Low | CONFIRMED | Dockerfile has no explicit JVM memory options. See backend/Dockerfile L18-L22. |
| F16 | Low | CONFIRMED | Admin product and order endpoints return complete lists. See AdminController.java L57-L58, L89-L90. |
| F17 | Low | CONFIRMED | V1 creates order_status_history but there is no entity/application writer in main. V1 L54-L58. |

### Additional audit findings

| ID | Severity | Finding | Evidence | Proposed remediation |
|---|---|---|---|---|
| N1 | Medium | Active products assigned to inactive categories still appear on public catalogue/detail endpoints. | CategoryService.java L17; ProductRepository.java L12-L18; ProductService.java L36-L40. | Filter inactive categories from public product searches/details; preserve no-category products if intended. |
| N2 | Medium | Some request fields and query parameters lack explicit maximum lengths. | ApiDtos.java L11, L39, L41; ProductController.java L16. | Add appropriate bounds and regression tests. |
| N3 | Low | '%' and '_' in product search act as SQL LIKE wildcards. | ProductRepository.java L15. | Decide whether to escape wildcard characters and add tests. |
| N4 | Medium / concurrency-dependent | Concurrent default-address changes may leave multiple defaults. | AddressService.java L31-L58, L86-L89. | Add concurrency test and safe enforcement. |
| N5 | Low | CI admin test credential is duplicated in workflow and integration-test sources. | .github/workflows/ci.yml L52-L54; CommerceFlowIntegrationTest.java L44-L51; CouponAndInventoryIntegrationTest.java L44-L52. | Use environment-driven test admin properties; do not duplicate credentials in source. |
| N6 | Medium | Frontend dependencies are not locked by a committed npm lockfile; CI installs via npm install. | frontend/package.json exists; no frontend/package-lock.json in audited main tree; ci.yml L22. | Commit lockfile, use npm ci and Dependabot. |

## Audit notes and constraints

- Ownership scoping was verified for addresses, wishlist, profile, cart, orders and payments. Direct cross-user tests remain sparse.
- Address default selection is correct for ordinary sequential use but is not proven safe under concurrent updates.
- Inactive products are excluded from public product list/detail endpoints; inactive products may still exist in persisted carts and wishlists.
- Inactive categories do not currently suppress their active products from public results (N1).
- V2 seed data uses ordinary multi-row INSERT and scalar category subqueries. V3/V4 use conventional CREATE/ALTER TABLE statements. No TiDB execution was performed.
- V1-V4 are immutable under the operating rules.
- WP05 needs additive V5 for history actor attribution. Existing history rows should become actor LEGACY, and rollback must preserve newly collected actor data unless separately approved.
- The exact TiDB CHECK enforcement setting was not verified.
- Local git commands and local Maven/npm commands could not run in this environment: GitHub DNS resolution failed and no local ShopIt checkout was available. The CI baseline was read from GitHub Actions logs, not executed locally.
- Remote backup branch backup-main-2026-11-02 was not found. It was not created or modified.
- No Render dashboard, TiDB console, secrets, live database or live state-changing endpoint was accessed.
- Payment remains simulated only.

## Owner decisions (dated 2026-11-02)

| Decision | Recorded default |
|---|---|
| D1 | Free shipping if merchandise subtotal after coupon discount is at least INR 1,999.00; otherwise INR 49.00; both configurable. |
| D2 | Listed prices are tax-exclusive; add 18% tax at checkout as a labelled line. |
| D3 | Prepaid orders only; CONFIRMED only after successful payment record. |
| D4 | Automatically cancel unpaid orders after 30 minutes. |
| D5 | No returns/refunds flow; admin cannot cancel paid order; preserve current 409. |
| D6 | One administrator account is sufficient; reserve its email from customer registration. |
| D7 | Password reset and email verification stay out of scope until email provider exists. |
| D8 | Simulated payments enabled on Render staging only by environment variable; disabled by default in code. |
| D9 | Existing access tokens are not revoked on password change yet; document this limitation. |
| D10 | Render free service may sleep; improve first-load experience instead of purchasing hosting. |

Additional operational choices:
- Do not enable a 10-minute uptime monitor by default; it would keep a free API service awake most of the month and consume almost all of a 750-hour workspace allowance.
- For logout documentation, default to accurately documenting the current public /api/auth/logout behavior rather than changing authentication semantics without a specific owner need.
- WP23 is excluded until the owner selects an optional feature.

## Execution blocker — 2026-11-02

The owner explicitly authorized WP-00 through WP-22 without waiting for GATE approvals. This runtime cannot resolve github.com or codeload.github.com for a local checkout (two clone attempts failed with DNS resolution errors), so local `git branch`, `git status`, `git log`, Maven and npm commands are unavailable. WP-00 through WP-02 have been implemented using the connected GitHub API and are being verified with PR CI. For each subsequent package, remote branch ancestry is checked against the preceding WP branch and PR CI is used as the available executable verification. Local command outputs are not represented as having run.


### WP-06 package evidence — 2026-11-02

- Branch: `wp06-auto-cancel`, based on merged main commit `fe02eac1cf59fb630007e8cd8dd45f9cb451af53`; PR #12 is open as draft and unmerged.
- Changes: unpaid-order expiry TTL defaults to 30 minutes; startup and five-minute scheduled sweeps; order-level `REQUIRES_NEW` transactions; inventory and coupon usage restoration; SYSTEM history event; order-row locks coordinate expiry with payment, customer cancellation and admin transitions; injectable UTC Clock.
- New unit tests cover overdue expiry and restoration, not-yet-expired orders and successful-payment exclusion.
- CI run `41436416649` was in progress when this ledger revision was recorded. The run executes Maven `verify` with MySQL 8.4 and frontend Vitest/build; no local command output is claimed.
- Security: no new endpoint or permission change; simulated payment behavior remains unchanged.
- Coverage gap: no explicit concurrent payment/expiry integration stress test or missing-product restoration test. Order row locks and per-order transaction rollback are implemented, but those cases still need direct regression coverage.
- Rollback: revert WP-06 application changes; no DB migration exists. Render sleep can delay scheduled execution; startup sweep catches expired orders after wake.


### WP-07 package evidence — 2026-11-02

- Branch: `wp07-cart-availability`, stacked on the open WP-06 branch to preserve ledger and dependency history; PR #13 targets main and remains draft/unmerged.
- CartLine and WishlistLine now expose additive `available` and `unavailableReason` values. Inactive products remain removable from carts/wishlists, but cannot be incremented, checked out or moved from wishlist to cart.
- Checkout now accumulates unavailable product names/reasons and returns HTTP 409 before saving an order; transaction rollback restores any in-transaction stock changes.
- Integration test adds, saves, deactivates, confirms unavailability, verifies a named checkout conflict, and verifies cart removal remains available.
- CI was not yet surfaced when this ledger update was made; do not regard WP-07 as verified until the PR workflow completes.
- Security: additive response fields only; no endpoint/authorization changes. No migration required.
- Adversarial gaps: no direct stock-shortage-only integration test, no multi-unavailable item assertion, no race-condition stress test for a deactivation concurrent with checkout. Checkout locks product rows and rechecks active/stock state.
- Rollback: revert code/DTO/frontend changes. No DB migration.


### WP-08 package evidence — 2026-11-02

- Branch: `wp08-payment-switch`, stacked on open WP-07 and based on WP-06's payment locking; PR #14 targets main and is draft/unmerged.
- Main application property `app.payments.simulated-enabled` defaults false; CI profile explicitly defaults true for automated payment-flow coverage.
- When disabled, POST simulation returns 404 before customer/order/payment repository access. No payment is created and no order status changes.
- OrderView now includes additive `simulatedPaymentEnabled`; the frontend hides the payment action and explains the disabled state.
- Added unit test verifies 404 and no repository interactions. Existing MySQL integration payment-flow test remains the enabled-mode regression.
- Security: no new endpoint or permission; no real payment provider or sensitive payment data.
- Owner action: set `APP_PAYMENTS_SIMULATED_ENABLED=true` only on the staging demo environment if simulated confirmation is required. No live Render setting was changed.
- CI on this package was not yet surfaced when ledger updated. Local Maven/npm commands cannot run in this execution runtime.
- Rollback: revert WP-08; no DB migration. Gaps: CI still needs to prove Spring property binding and the disabled service test against compilation; no dedicated frontend rendering test for the disabled button yet.


### WP-09 package evidence — 2026-11-02

- Branch: `wp09-rate-limiting`, stacked on open WP-08 to retain package ledger/history; PR #15 targets main and remains draft/unmerged.
- Token buckets apply: login 10/min/IP and 5/min/account-email hash; registration 5/hour/IP; orders 10/min/authenticated user; password change 5/10min/authenticated user; simulated payment 5/min/authenticated user.
- Rejected calls return HTTP 429 and `Retry-After`; login email bucket identifiers use SHA-256 of normalized email and are not logged.
- Idle buckets are evicted every five minutes after 30 idle minutes; maximum in-memory bucket count defaults to 100,000. Counters are process-local and reset on restart.
- Rate limits are enabled by default and disabled in the CI profile to avoid test-suite cross-talk. Unit tests cover burst exhaustion, independent buckets, disabled mode and the registration 429 response.
- Forwarded IP header use is disabled by default. Owner must confirm the edge proxy overwrites inbound `X-Forwarded-For` before setting `APP_RATE_LIMIT_TRUST_FORWARDED_FOR=true`; without this, hosted IP limits may aggregate against the proxy peer.
- Security: no endpoint surface change, and no raw email address is logged or used as a bucket key. No migration.
- CI for WP-09 not yet surfaced at the time this update was made; local Maven/npm commands unavailable.
- Adversarial gaps: no multi-instance/shared-distributed quota, no dedicated authenticated order/password filter integration test, and no proxy-header integration test. In-memory limiter is a single-process mitigation, not a distributed abuse-control system.
- Rollback: revert WP-09. No DB migration.


### WP-10 package evidence — 2026-11-02

- Branch: `wp10-public-hardening`, stacked on open WP-09 to preserve ledger and package ancestry; PR #16 targets main and is draft/unmerged.
- API surface filter adds `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, and a restrictive Permissions-Policy. Docs endpoints return generic 404 by default, while existing `permitAll` rules remain unchanged.
- API docs are disabled by default in main and enabled in CI/local test profiles.
- Render Blueprint adds static-site security headers and a report-only CSP. No enforced CSP/HSTS and no live Render changes.
- Unit tests cover blocked docs route, pass-through route, and response headers.
- WP-10 CI run pending when recorded; WP-09 CI compile failure was fixed before the rerun. Local Maven/npm commands unavailable.
- Owner action: inspect real browser CSP reports on the frontend deployment before considering enforcement. Rollback by reverting WP-10; no migration.


### WP-11 package evidence — 2026-11-02

- Branch: `wp11-registration-hardening`, stacked on open WP-10 branch to preserve the ordered package ledger; PR #17 targets main and remains draft/unmerged.
- Registration trims names, normalizes email, rejects passwords equal to normalized email, rejects a small common-password list, and translates a concurrent unique-email insert race to the same HTTP 409 response as an existing duplicate.
- DataIntegrityViolationException is translated only when a follow-up email lookup confirms the duplicate; unrelated integrity failures propagate normally.
- Security documentation records duplicate-email 409 enumeration trade-off until verified email delivery exists.
- Added unit tests for password/email match, common passwords, unique-email race mapping and normalized persisted name/email.
- CI has not yet surfaced for WP-11; local Maven/npm commands are unavailable here.
- Security surface: no new endpoints, auth weakening, or schema migration. Known coverage gap: the uniqueness race is simulated at repository layer rather than two concurrent MySQL HTTP requests.
- Rollback: revert WP-11 code and test changes.


### WP-12 package evidence — 2026-11-02

- Branch: `wp12-public-stock`, stacked on the open WP-11 branch to preserve the package sequence. PR pending creation; changes are cumulative until predecessors merge.
- Public ProductView no longer exposes exact stock. It now exposes `inStock` (stock greater than zero) and `lowStock` (1–5 units). AdminProductView retains exact stock/cost.
- CartLine `availableStock` is capped at 100. Product cards/details render In stock / Only a few left / Sold out and disable add-to-bag when sold out.
- Added MySQL integration test for stock 0, 1, 5 and 6; verifies public exact stock absent and admin exact stock retained.
- CI not yet queued when ledger updated; local Maven/npm not available.
- Security: reduces public inventory enumeration. No schema migration.
- Gaps: no dedicated cart availableStock cap assertion; no automated UI test for stock labels. Rollback by reverting WP-12 DTO/service/frontend/test changes.

## Per-package completion evidence

Each package must record:
- Exact branch guard command outputs when available.
- Files changed and relevant code paths.
- Backend and frontend checks actually executed.
- GitHub Actions run and job links, where available.
- Adversarial self-review: three credible failure modes and the tests that cover them, or explicit coverage gaps.
- Security surface changes.
- Risks, rollback and pending owner actions.

Never mark a package MERGED until the owner confirms GitHub merged it. Never mark checks passed without command or CI evidence.
