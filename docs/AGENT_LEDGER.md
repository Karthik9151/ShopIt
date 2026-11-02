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
| WP-07 | F9 | BLOCKED | `wp07-cart-availability` | — | — | — |
| WP-07 | F9 | PR OPEN — DRAFT | `wp07-cart-availability` | https://github.com/Karthik9151/ShopIt/pull/13 | CI run 41437052277: Maven/MySQL verify, backend container build, frontend tests and production build all passed. Local commands unavailable. | None; remove unavailable product rows before checkout |
| WP-08 | F3 | BLOCKED | `wp08-payment-switch` | — | — | Set APP_PAYMENTS_SIMULATED_ENABLED=true on staging if demo payments are required |
| WP-08 | F3 | PR OPEN — DRAFT | `wp08-payment-switch` | https://github.com/Karthik9151/ShopIt/pull/14 | CI run 41437483366: Maven/MySQL verify, backend container build, frontend tests and production build all passed. Local commands unavailable. | Set APP_PAYMENTS_SIMULATED_ENABLED=true on staging only if demo payments are required; never in production |
| WP-09 | F8 | BLOCKED | `wp09-rate-limiting` | — | — | Review configured limits and proxy behavior after tests |
| WP-09 | F8 | PR OPEN — DRAFT | `wp09-rate-limiting` | https://github.com/Karthik9151/ShopIt/pull/15 | First CI run 41438195716 failed at compile (static decision factory call); corrected in b94284c and validation run 41438476868 queued. | Confirm trusted proxy overwrites X-Forwarded-For before enabling APP_RATE_LIMIT_TRUST_FORWARDED_FOR |
| WP-10 | F12 | PR OPEN — CHECKS PASS | `wp10-public-hardening` | https://github.com/Karthik9151/ShopIt/pull/16 | CI run 41442109697: Maven/MySQL verify + backend container build passed; frontend tests + production build passed. Local commands unavailable in this runtime. | Owner must inspect deployed report-only CSP violations before considering enforcement; no Render settings changed |
| WP-11 | F11 | PR OPEN — DRAFT | `wp11-registration-hardening` | https://github.com/Karthik9151/ShopIt/pull/17 | First CI 41438913897 failed only Mockito verify count; corrected in ff7a8de. Retry 41439144995 passed Maven/MySQL verify, backend Docker build, frontend tests and production build. | Accept documented email-enumeration trade-off until email verification exists |
| WP-12 | F13 | PR OPEN — DRAFT | `wp12-public-stock` | https://github.com/Karthik9151/ShopIt/pull/18 | CI 41439282251: Maven/MySQL verification and frontend tests/build passed; backend container build was still in progress at last check. | Low-stock threshold defined as 1–5 units |
| WP-13 | F10, F15 | PARTIAL — PR OPEN | `wp13-cold-start` | https://github.com/Karthik9151/ShopIt/pull/19 | CI run 41439602610: Maven/MySQL verification and frontend tests/build passed. JVM benchmark unavailable without runtime. | Owner must supply start-to-health/first API latency and peak RSS measurements; no uptime monitor on Render Free |
| WP-14 | F16 | PR OPEN — CHECKS PASS | `wp14-admin-pagination` | https://github.com/Karthik9151/ShopIt/pull/20 | CI run 41442965512: Maven/MySQL verify + backend container build passed; frontend tests + production build passed. | None for code; no live Render/TiDB action performed |
| WP-15 | Operations | PR OPEN — CHECKS PASS | `wp15-operations` | https://github.com/Karthik9151/ShopIt/pull/21 | CI 41442859399 passed backend Maven/MySQL verify + container build and frontend tests/build. | Owner backup/PITR configuration and isolated restore rehearsal |
| WP-16 | Test coverage, N5 | PR OPEN — CHECKS PASS | `wp16-e2e-testing` | https://github.com/Karthik9151/ShopIt/pull/22 | CI 41443710648 passed backend Maven/MySQL verify + container build and frontend tests/build. | Stacked on unmerged WP-14; merge WP-14 first |
| WP-17 | CI security, N6 | PR OPEN — BLOCKED | `wp17-ci-security` | https://github.com/Karthik9151/ShopIt/pull/23 | Dependabot/CodeQL config added; dependency-review run 41443918784 failed (Dependency Graph disabled); lockfile attempts failed twice. | Owner enable Dependency Graph/alerts/secret scanning/push protection/branch protection; generate valid lockfile then npm ci |
| WP-18 | TiDB verification | PR OPEN — BLOCKED (OWNER STAGING CHECK) | `wp18-tidb-verification` | https://github.com/Karthik9151/ShopIt/pull/24 | Docs only; baseline scripts/verify-tidb.sh not found; no TiDB access or script run. | Owner verify TLS, Flyway history, schema, transactions and locking on isolated TiDB staging cluster |
| WP-19 | Readability | PR OPEN — CHECKS PENDING | `wp19-readability` | https://github.com/Karthik9151/ShopIt/pull/25 | CI pending Maven/MySQL verification and frontend tests/build. | Stacked on unmerged WP-14; merge WP-14 first |
| WP-20 | Documentation | PR OPEN — CHECKS PENDING | `wp20-docs-accuracy` | https://github.com/Karthik9151/ShopIt/pull/26 | CI pending Maven/MySQL verification and frontend tests/build; docs only. | Stacked on WP-19/WP-14; merge WP-14 then WP-19 before WP-20 |
| WP-21 | Accessibility and SEO | PR OPEN — CHECKS PASS | `wp21-accessibility` | https://github.com/Karthik9151/ShopIt/pull/27 | CI 41445293967 passed Maven/MySQL verify + backend container build and frontend tests/production build. | Stacked on unmerged WP-14; merge WP-14 first |
| WP-15 | Operations | PR OPEN — CHECKS PASS | `wp15-operations` | https://github.com/Karthik9151/ShopIt/pull/21 | CI 41442859399 passed Maven/MySQL verify, backend container build, frontend tests and production build. | Owner backup/PITR configuration and isolated restore rehearsal |
| WP-16 | Test coverage, N5 | PR OPEN — CHECKS PASS | `wp16-e2e-testing` | https://github.com/Karthik9151/ShopIt/pull/22 | CI 41443710648 passed Maven/MySQL verify, backend container build, frontend tests and production build. | Stacked on unmerged WP-14; merge WP-14 first |
| WP-17 | CI security, N6 | PR OPEN — BLOCKED | `wp17-ci-security` | https://github.com/Karthik9151/ShopIt/pull/23 | Dependabot/CodeQL added; dependency-review CI 41443918784 failed because Dependency Graph is disabled; npm lockfile generation failed twice (registry timeout; offline ENOTCACHED). | Owner enable Dependency Graph/Dependabot alerts/secret scanning/push protection/branch protection; generate valid lockfile in network-enabled checkout then use npm ci |
| WP-18 | TiDB verification | PR OPEN — BLOCKED (OWNER STAGING CHECK) | `wp18-tidb-verification` | https://github.com/Karthik9151/ShopIt/pull/24 | Docs only; baseline scripts/verify-tidb.sh not found; no TiDB access or script run. | Owner verify TLS, Flyway history, schema, transactions and locking on isolated TiDB staging cluster |
| WP-19 | Readability | PR OPEN — CHECKS PENDING | `wp19-readability` | https://github.com/Karthik9151/ShopIt/pull/25 | CI pending: Maven/MySQL verify and frontend tests/build. | Stacked on unmerged WP-14; merge WP-14 first |
| WP-20 | Documentation | PR OPEN — CHECKS PASS | `wp20-docs-accuracy` | https://github.com/Karthik9151/ShopIt/pull/26 | CI 41445039629 passed Maven/MySQL verify + backend container build and frontend tests/production build. | Stacked on WP-19/WP-14; merge WP-14 then WP-19 before WP-20 |
| WP-14 | F16 | PR OPEN — DRAFT | `wp14-admin-pagination` | https://github.com/Karthik9151/ShopIt/pull/20 | CI pending; no local checkout. Admin orders/products paginated, filtered; inventory and dashboard aggregates stay full-table. | Ensure dashboard valuation remains full inventory; not page-limited |
| WP-15 | Operations | BLOCKED | `wp15-operations` | — | — | Provide/verify operational recovery and database backup procedures |
| WP-16 | Test coverage, N5 | PR OPEN — CHECKS PASS | `wp16-e2e-testing` | https://github.com/Karthik9151/ShopIt/pull/22 | CI run 41443710648: Maven/MySQL verify + backend container build passed; frontend tests + production build passed. | Stacked on unmerged WP-14; merge WP-14 before WP-16 |
| WP-14 | F16 | PR OPEN — CHECKS PASS | `wp14-admin-pagination` | https://github.com/Karthik9151/ShopIt/pull/20 | CI run 41442965512: Maven/MySQL verify + backend container build passed; frontend tests + production build passed. | None for code; no live Render/TiDB action performed |
| WP-13 | F10, F15 | PARTIAL — PR OPEN | `wp13-cold-start` | TBD | GET 60-second timeout, safe single GET retry and five-second slow-request banner implemented; JVM benchmark unavailable without runtime. | Owner must supply start-to-health/first API latency and peak RSS measurements; no uptime monitor on Render Free |
| WP-09 | F8 | BLOCKED | `wp09-rate-limiting` | — | — | Review configured limits and proxy behavior after tests |
| WP-09 | F8 | PR OPEN — DRAFT | `wp09-rate-limiting` | https://github.com/Karthik9151/ShopIt/pull/15 | First CI run 41438195716 failed at compile (static decision factory call); corrected in b94284c and validation run 41438476868 queued. | Confirm trusted proxy overwrites X-Forwarded-For before enabling APP_RATE_LIMIT_TRUST_FORWARDED_FOR |
| WP-10 | F12 | PR OPEN — CHECKS PASS | `wp10-public-hardening` | https://github.com/Karthik9151/ShopIt/pull/16 | CI run 41442109697: Maven/MySQL verify + backend container build passed; frontend tests + production build passed. Local commands unavailable in this runtime. | Owner must inspect deployed report-only CSP violations before considering enforcement; no Render settings changed |
| WP-11 | F11 | PR OPEN — CHECKS PASS | `wp11-registration-hardening` | https://github.com/Karthik9151/ShopIt/pull/17 | CI 41439144995: Maven/MySQL verify, backend container build, frontend unit tests and production build passed. | Accept the documented duplicate-email 409 enumeration trade-off until verified email delivery exists |
| WP-12 | F13 | BLOCKED | `wp12-stock-exposure` | — | — | Review public ProductView API contract change |
| WP-13 | F10, F15 | BLOCKED | `wp13-cold-start-jvm` | — | — | Optional uptime monitor intentionally not enabled; no Render settings changes |
| WP-14 | F16 | BLOCKED | `wp14-admin-pagination` | — | — | Review paginated admin API contract |
| WP-15 | Operations | PR OPEN — CHECKS PASS | `wp15-operations` | https://github.com/Karthik9151/ShopIt/pull/21 | CI run 41442859399: Maven/MySQL verify + backend container build passed; frontend tests + production build passed. | Owner must configure backups/PITR and complete isolated restore rehearsal; no live DB/Render/TiDB action performed |
| WP-16 | Test coverage, N5 | BLOCKED | `wp16-e2e-testing` | — | — | — |
| WP-17 | CI security, N6 | BLOCKED | `wp17-ci-security` | — | — | Enable secret scanning/push protection, Dependabot alerts and main branch protection in GitHub settings |
| WP-18 | TiDB verification | PR OPEN — CHECKS PASS; STAGING BLOCKED | `wp18-tidb-verification` | https://github.com/Karthik9151/ShopIt/pull/24 | CI 41444371515 passed Maven/MySQL verify + container build and frontend tests/build. No TiDB query/script run; script absent at baseline. | Owner must verify TLS, Flyway history, schema, transactions and locking on isolated TiDB staging cluster |
| WP-17 | CI security, N6 | PR OPEN — BLOCKED (OWNER SETTINGS + LOCKFILE) | `wp17-ci-security` | https://github.com/Karthik9151/ShopIt/pull/23 | Dependabot/CodeQL/dependency review files added; dependency-review run 41443918784 failed because Dependency Graph is disabled. npm lockfile generation failed twice (registry timeout; offline ENOTCACHED). | Owner enable Dependency Graph/Dependabot alerts, secret scanning/push protection and branch protection; generate real package-lock.json in network-enabled environment then use npm ci |
| WP-18 | TiDB verification | BLOCKED | `wp18-tidb-ci` | — | — | Run scripts/verify-tidb.sh on staging and verify flyway_schema_history; no staging DB access performed |
| WP-19 | Readability | PR OPEN — CHECKS PASS | `wp19-readability` | https://github.com/Karthik9151/ShopIt/pull/25 | CI 41444506589 passed Maven/MySQL verify + backend container build and frontend tests/production build. | Stacked on unmerged WP-14; merge WP-14 first |
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

- Existing PR #16 was found diverged from the post-WP-09 main branch (six commits behind and mergeability state dirty). The wp10-public-hardening branch was rebuilt on top of current main commit 4cc2c3598cb751cfd4b360a42cab526fff8cbbd9, preserving the WP-10-only file set and existing PR. No merge was performed.
- The resulting PR branch is one commit ahead of main, zero commits behind, and GitHub reports it mergeable.
- Implemented baseline API response headers and hidden Swagger/OpenAPI routes by default; added local/test/CI profile switches; added static-site headers and a report-only CSP to the Render Blueprint. The CSP is report-only, not enforcing.
- CI run 41442109697: Maven verification with MySQL 8 integration passed; backend container build passed; frontend unit tests passed; production build passed.
- Local backend Maven verify and frontend test/build commands were not executable in this runtime because there is no mounted checkout and GitHub DNS resolution failed. No local success is claimed.
- Security: docs routes return a generic 404 unless the owner explicitly enables the flag in a trusted profile. No auth matcher was weakened, no DB migration was added, and no live Render settings were changed.
- Owner action: inspect actual browser CSP reports on the deployed frontend before considering an enforcing CSP. Roll back by reverting the WP-10 files/commit; no DB rollback is needed.


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


### WP-13 package evidence — 2026-11-02

- Branch: `wp13-cold-start`, stacked on WP-12; PR pending creation. All PRs remain unmerged by instruction.
- Browser GET requests use a 60-second timeout and are retried once on network failure or 502/503/504. Mutating requests are not retried.
- API request activity is counted; a polite status banner appears after a request remains pending for five seconds and clears when all requests finish.
- Added Vitest tests for GET gateway statuses, network errors, cancellation, retry-once flag, and never retrying POST/PUT/PATCH/DELETE.
- JVM tuning is not applied without measured startup-to-health time, first business endpoint latency, heap and peak RSS on the actual 512 MB runtime. No Render setting or uptime monitor changed.
- CI pending at this ledger revision; local Maven/npm commands unavailable.
- Rollback: revert WP-13 frontend client, banner, CSS, test and docs. No DB migration. Residual gap: real-browser retry scheduling is not covered by an end-to-end test.


### WP-14 package evidence — 2026-11-02

- Branch: `wp14-admin-pagination`, stacked on WP-13 branch to preserve the package history; PR #20 will remain draft/unmerged.
- GET `/api/admin/orders?page=0&size=20&status=` now returns a Spring Page; order status filter is normalized to uppercase.
- GET `/api/admin/products?page=0&size=20&q=&active=` now returns a Spring Page and supports name search and active-state filter.
- Page is zero-based; default size 20; maximum size 100; negative page or invalid/oversized size yields 400.
- Product catalogue in Admin has query/active filters and page controls. Recent orders has status filtering and page controls. AdminInventory shows paged inventory rows, while dashboard KPIs and valuation remain aggregate/full-table.
- Integration test covers response shape, page boundaries, search/active filter, status filter and invalid bounds.
- WP-13 CI run 41439602610 passed Maven/MySQL verification and frontend tests/build. WP-14 CI pending. Local commands unavailable.
- Security: endpoints remain within the existing admin authorization boundary; no schema change.
- Rollback: revert WP-14 controller/repository/service/frontend/test changes. Gap: integration test disables security filters to focus on pagination and separate pre-existing security tests cover admin authorization.

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


### WP-18 package evidence — 2026-11-02

- Added docs/TIDB_VERIFICATION.md and linked it from docs/DEPLOYMENT.md. It specifies owner-side TLS/connectivity, Flyway history/checksum, schema validation, transaction/locking/concurrency and cleanup checks for an isolated staging database.
- The repository baseline path scripts/verify-tidb.sh returns NOT_FOUND. Per instruction, the script was not run and no TiDB access was attempted.
- This package remains BLOCKED pending owner-run staging verification. MySQL 8.4 CI passing does not prove TiDB behavior.
- No Render/TiDB settings, database rows, schema, or Flyway migrations were changed. V1–V4 untouched; no migration added.


### WP-18 CI result — 2026-11-02

- Documentation PR CI run 41444371515 passed Maven/MySQL verification and backend container build plus frontend tests/production build. This validates repository checks only; it does not verify TiDB compatibility.
- No staging TiDB endpoint was accessed and `scripts/verify-tidb.sh` was not run. The baseline repository path was not found.

### WP-10 package evidence — 2026-11-02
### WP-17 package evidence — 2026-11-02

- PR #23 adds weekly Dependabot update configuration for Maven, npm, and GitHub Actions; CodeQL analysis for Java and JavaScript/TypeScript; and a dependency-review workflow.
- CI run 41443918784 failed in Dependency Review because GitHub reports the Dependency Graph is not enabled/supported on this repository. No GitHub settings were changed (owner-only action).
- The frontend lockfile remains absent. Attempt 1 to retrieve npm registry metadata timed out. Attempt 2, offline lockfile generation, failed with ENOTCACHED because package metadata is not present in the local npm cache. No fake lockfile was committed and CI was not switched to npm ci.
- WP-17 is therefore BLOCKED as a full package, although the Dependabot and CodeQL configuration files are present in the PR. Required owner actions: enable Dependency Graph, alerts, secret scanning/push protection and branch protection; create a valid lockfile in a network-enabled environment and change the install to npm ci.
- Local Maven/frontend commands are unavailable because this execution environment has no mounted checkout and npm registry DNS/network access failed. No secrets are present in committed files.
### WP-16 package evidence — 2026-11-02

- PR #22 is stacked on the unmerged wp14-admin-pagination branch; this dependency is stated in the PR description. The PR is not merged.
- Replaced duplicated CI admin email/password literals in CommerceFlowIntegrationTest, CouponAndInventoryIntegrationTest, and PublicStockDisclosureIntegrationTest with a shared environment-backed test helper. The CI workflow generates a new test-only admin identity and JWT signing key per run without echoing values.
- Added frontend Vitest interaction coverage for unauthenticated admin-route redirection and navigation from sign-in to account creation.
- No real secrets or production account details were added. No Flyway migration changed. CI outcomes are pending; local Maven/npm commands were unavailable in this runtime.
- Failure modes addressed: duplicated test credentials drifting between test classes/workflow; unauthenticated access to protected admin routes; broken auth-page navigation. Further full browser/E2E checkout testing remains a coverage gap.


### WP-16 verification update — 2026-11-02

- The first CI attempt 41443311352 failed on the duplicate sign-up link rendered across tests and the static uppercase admin email not matching the generated CI administrator. Fixed cleanup between renders and made the admin reservation test use the uppercase form of the generated APP_ADMIN_EMAIL.
- The second CI attempt 41443710648 passed both jobs: Maven/MySQL 8 integration verification plus backend container build, and frontend Vitest plus production build.
- WP-16 branch depends on unmerged WP-14 and its PR description explicitly requires WP-14 to merge first. Neither PR has been merged.
### WP-15 package evidence — 2026-11-02

- PR #21 adds docs/OPERATIONS.md and links it from docs/DEPLOYMENT.md. Scope covers release checks, provider-managed backup/PITR policy, isolated restore rehearsal, incident response, application rollback, and forward-only migration recovery.
- Procedures are documented only: no live backup, restore, production DB inspection, Render action, TiDB action, or GitHub settings change was performed.
- No application behavior, authorization, API contract, or Flyway migration changed. V1–V4 remain untouched.
- Local backend/frontend commands were unavailable because this execution runtime has no mounted checkout and GitHub DNS resolution failed. PR CI results are pending and will be recorded from the actual workflow run.
- Owner actions: configure provider backups/PITR, approve RPO/RTO, perform an isolated restore rehearsal, and verify provider-specific logical-dump compatibility.


### WP-15 verification update — 2026-11-02

- PR CI run 41442859399 completed successfully: Maven verification with MySQL 8 integration and backend container build passed; frontend tests and production build passed. No local Maven/npm commands were executed.

### WP-10 package evidence — 2026-11-02
### WP-14 package evidence — 2026-11-02

- Existing PR #20 (wp14-admin-pagination) was six commits behind main and initially dirty. The branch was rebased using a two-parent reconciliation commit so current merged WP-09 main is the first parent while the existing WP-13 work ancestry is retained. The PR now has no base divergence and GitHub reports it mergeable. No PR was merged.
- Admin product listing supports page/size/search/active filters; admin order listing supports page/size/status filters. Page size defaults to 20 and is capped at 100; negative page and over-limit sizes return HTTP 400. Dashboard/inventory valuation remains based on the full data set, not the current page.
- The first verification failure was missing Spring Data Page/Pageable imports in OrderService. After adding them, Maven surfaced two older integration tests that still iterated the now-paged admin product response as a raw array. Updated both tests to inspect the Page content array and request a sufficient page size.
- CI run 41442965512 passed: Maven verification with MySQL 8 integration, backend container build, frontend unit tests, and production build.
- Local Maven/frontend commands were not run because this runtime has no mounted repository checkout and GitHub DNS resolution failed. No local command success is claimed.
- No Flyway migration was added or edited. API pagination is additive; no authorization changes. Rollback by reverting WP-14 code/test changes. No live Render/TiDB validation was performed.


### WP-21 package evidence — 2026-11-02

- Added a keyboard-accessible skip link, a labeled primary navigation landmark, a focusable main-content target, and synchronized aria-expanded/aria-controls for the mobile navigation toggle.
- Added polite status announcement semantics to product result counts and global focus-visible/skip-link styling.
- Added Open Graph/Twitter title and description metadata plus application name; no canonical production URL was invented.
- Added Vitest coverage for skip navigation, landmark naming, main target and mobile menu expanded state.
- Branch is based on unmerged WP-14 and depends on it; no API/schema/Flyway changes or Render/GitHub settings changes. CI pending.


### WP-21 CI result — 2026-11-02

- CI run 41445293967 passed frontend accessibility Vitest tests and production build, plus backend Maven/MySQL verification and backend container build.
### WP-19 package evidence — 2026-11-02

- PR #25 improves readability with .editorconfig conventions, focused PricingService helpers, shared frontend locale formatters, and null money/input regression coverage.
- Pricing behavior preserves discounted subtotal shipping eligibility, 18% tax calculation, HALF_UP rounding and current decision values. Existing pricing tests remain and a null-input case was added.
- PR branch is stacked on unmerged WP-14 (stated in the PR); merge WP-14 first. No API contract, persistence schema, or Flyway migration change.
- CI results are pending. Local Maven/npm commands were not run because there is no local repository checkout and package registry access timed out.


### WP-20 package evidence — 2026-11-02

- Updated API docs against current controllers for admin products/orders/customers pagination, filters, Page JSON contract, size bounds, dashboard full-data aggregates, and Swagger default-off configuration.
- Documented stateless logout accurately: browser storage is cleared, but copied JWTs are not revoked by the API and password changes do not revoke old tokens.
- Refreshed testing docs for the actual current Vitest, MySQL integration, route-guard, retry and pagination coverage; recorded exact local backend/frontend commands and the uncommitted npm lockfile limitation.
- Brought in the owner operations and TiDB verification guide documents and linked them from deployment docs. WP-15 and WP-18 retain their separate package PRs.
- Branch is stacked on WP-19 and inherits WP-14; dependency/merge order is stated in PR. No PR merged. Docs only; no API/schema/Flyway/Render/TiDB/GitHub settings change, no secrets.


### WP-20 CI result — 2026-11-02

- Documentation PR CI run 41445039629 passed Maven/MySQL verification and backend container build plus frontend tests/production build. It does not validate the live Render/TiDB deployment.
### WP-19 CI result — 2026-11-02

- CI run 41444506589 on the latest WP-19 implementation commit passed Maven/MySQL verification and backend container build, together with frontend tests and production build. Local commands were not run in this runtime.
