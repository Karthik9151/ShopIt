# ShopIt Agent Work Ledger

Last updated: 2026-11-02
Repository: https://github.com/Karthik9151/ShopIt
Baseline branch: `main`
Baseline commit audited: `260ea492765a98f4226c5a661e66a7186cd3a3b9`

> This ledger tracks the ordered remediation packages. A PR is never considered merged until the owner confirms that GitHub merged it. No application changes are implied by a PLANNED or BLOCKED status.

## Work packages

| WP id | Review findings | Status | Branch | PR link | Checks run | Owner actions pending |
|---|---|---|---|---|---|---|
| WP-00 | F1-F17, N1-N6 | PR OPEN | `wp00-ledger` | https://github.com/Karthik9151/ShopIt/pull/6 | PR run 41430477537: frontend tests and build passed; backend Maven job still running at last check. Main baseline run 41421412026: backend 7/7; frontend 1/1; Docker build passed. Local commands unavailable. | Confirm backup branch location; provide local checkout or authorize remote-only Git workflow |
| WP-01 | F1 | PR OPEN | `wp01-admin-bootstrap` | https://github.com/Karthik9151/ShopIt/pull/7 | PR run 41431237299: backend 13/13 passed; frontend 1/1 passed; frontend build and Docker build passed. First two CI attempts failed due test syntax; corrected on third run. | Optional one-deploy APP_ADMIN_TAKE_OVER_EXISTING=true if choosing controlled takeover; unset afterward |
| WP-02 | F2, F14 | BLOCKED | `wp02-pricing` | — | — | Confirm none; D1/D2 apply |
| WP-03 | F4 | BLOCKED | `wp03-error-handling` | — | — | Confirm trace-id correlation design is acceptable |
| WP-04 | F5 | BLOCKED | `wp04-session-expiry` | — | — | — |
| WP-05 | F6, F17 | BLOCKED | `wp05-order-history` | — | — | Approve/verify additive V5 actor migration effect and rollback before migration |
| WP-06 | F7 | BLOCKED | `wp06-auto-cancel` | — | — | Acknowledge Render free-service scheduled-job sleep limitation |
| WP-07 | F9 | BLOCKED | `wp07-cart-availability` | — | — | — |
| WP-08 | F3 | BLOCKED | `wp08-payment-switch` | — | — | Set APP_PAYMENTS_SIMULATED_ENABLED=true on staging if demo payments are required |
| WP-09 | F8 | BLOCKED | `wp09-rate-limiting` | — | — | Review configured limits and proxy behavior after tests |
| WP-10 | F12 | BLOCKED | `wp10-public-hardening` | — | — | Verify report-only CSP on live site before enforcement |
| WP-11 | F11 | BLOCKED | `wp11-registration-hardening` | — | — | Accept documented email-enumeration trade-off until email verification exists |
| WP-12 | F13 | BLOCKED | `wp12-stock-exposure` | — | — | Review public ProductView API contract change |
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
