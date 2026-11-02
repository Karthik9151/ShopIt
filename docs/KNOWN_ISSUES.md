# ShopIt Known Issues and Audit Record

Audit date: 2026-11-02  
Audited baseline: `main`  
Working remediation branch: `shopit-improvements`  
Continuation PR: https://github.com/Karthik9151/ShopIt/pull/47

## Audit boundary

The audit and remediation were performed against repository source, tests, documentation and GitHub Actions. No live Render service, production site, live database, payment processor or customer data was accessed. All changes remain on `shopit-improvements`; `main` and backup branches were not modified. No Flyway migration was changed.

## Implemented on shopit-improvements

- Admin dashboard reporting for verified sales by day, order-status counts, top products derived from successful payments, low-stock items and recent orders. Existing dashboard summary fields remain available.
- Admin order detail, customer detail and paginated per-customer order history with order timelines.
- Admin product detail/status, inventory pagination/filtering, paged category management and paged coupon buckets.
- Admin order/customer/product/category/inventory workflows, including bounded filters and formula-safe CSV exports.
- Shared admin layout, dedicated order/product/category/customer modules, analytics, inventory and coupon screens, plus shared UI primitives.
- Vitest route and component coverage; Playwright browser smoke tests for non-admin route blocking and administrator navigation across the workspaces.
- CI frontend stages for ESLint, npm audit, Vitest, production build and Playwright Chromium; backend Maven verification against MySQL 8.4 and backend container build.
- Vitest excludes the Playwright-only `frontend/e2e` specs. Playwright runs separately on Ubuntu 24.04, which supports the selected browser package.
- CSRF protection is not broadly disabled or blanket-exempted for `/api/**`. State-changing requests without explicit Bearer credentials retain CSRF checks; public stateless `/api/auth/**` operations and explicit `Authorization: Bearer` requests use the documented matcher. Regression tests cover the boundary.
- The `demo` Spring profile now rejects non-loopback/non-MySQL datasource URLs during bean-factory initialization, before datasource/Flyway startup. A unit test covers loopback, remote, private-IP, H2 and null cases.

## Remaining engineering work

### Frontend architecture and end-to-end coverage

- The frontend is now separated into a small router/layout shell (`App.jsx`), an authentication context, shared route helpers, storefront/customer pages, admin dashboard/detail pages and admin operations pages. Feature components are no longer concentrated in the router file.
- Playwright covers 4 Chromium tests: 3 administrator/customer access-navigation tests and 1 mocked customer lifecycle journey (registration, product add, cart, checkout, order detail and order history). The customer test asserts no payment endpoint is called when simulated payments are disabled. It mocks API responses; full browser-to-live-backend execution remains an isolated-staging owner check.
- No comprehensive cross-browser, visual-regression or full accessibility audit has been performed. Complete keyboard/mobile checks in isolated staging.

### Dependency reproducibility

- `frontend/package-lock.json` is not committed. CI currently uses `npm install`; reproducible `npm ci` is not available until a reviewed lockfile is generated and committed in a network-enabled environment.

### Runtime and deployment verification

- There is no measured JVM tuning for a 512 MB instance. Collect startup, heap, container RSS and health-readiness measurements on a target-equivalent runtime before selecting flags.
- MySQL 8.4 CI is not TiDB compatibility proof. Complete the isolated verification matrix in `docs/TIDB_VERIFICATION.md`.
- Render environment variables, deployed service status and live routing were not inspected or changed. The owner must verify staging configuration and readiness on the target environment.

## Verified CI results

Verified on PR head `32c4b233902028634d923107acffa8b4646cb4a1`:

| Check | Result | Evidence |
|---|---|---|
| Frontend ESLint | Passed | ShopIt CI run `41483344271` |
| Dependency audit (`npm audit --audit-level=high`) | Passed | ShopIt CI run `41483344271` |
| Vitest | 39 passed across 11 files | ShopIt CI run `41483344271` |
| Vite production build | Passed | ShopIt CI run `41483344271` |
| Playwright Chromium tests | 4 passed | ShopIt CI run `41483344271` |
| Backend Maven/H2/MySQL 8.4 verification | 73 passed, 0 failures/errors | ShopIt CI run `41483344271` |
| Backend container build | Passed | ShopIt CI run `41483344271` |
| CodeQL Java/Kotlin and JavaScript/TypeScript | Both passed | CodeQL run `41483344120` |
| Dependency review | Passed | Dependency review run `41483344287` |

The tested PR head was 142 commits ahead of `main` and 0 commits behind at the time of verification. No migration files were changed. PR #47 is open, ready for review (not draft), and has not been merged.

## Owner/runtime actions still required

Complete the reviewed frontend lockfile/reproducible install, full browser-to-live-backend staging journey, target-sized JVM measurement, isolated TiDB verification and Render staging smoke test before treating those runtime/operational areas as complete. A successful CI run does not prove production deployment, TiDB compatibility or 512 MB instance performance.
