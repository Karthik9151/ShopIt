# ShopIt Known Issues and Audit Record

Audit date: 2026-11-02  
Audited baseline: main  
Working remediation branch: shopit-improvements  
Continuation PR: https://github.com/Karthik9151/ShopIt/pull/47

## Audit boundary

The audit and implementation use repository source, tests, documentation and GitHub Actions. No live Render service, production site, live database, payment processor or customer data was accessed. Work remains isolated to shopit-improvements; main and backup branches were not modified, and Flyway V1–V4 were not changed.

## Implemented on shopit-improvements

- Consolidated admin dashboard analytics: verified daily sales, order status breakdown, top products from successful payments, low-stock items and recent orders. Existing dashboard summary fields remain additive/backward-compatible.
- Admin order detail and paginated per-customer order history.
- Admin product detail/status, inventory pagination/filtering, paged categories and paged coupon buckets.
- Dedicated admin layout and page modules for orders, products, categories and customers; dashboard analytics, inventory and coupon workspaces use the shared admin navigation.
- Shared UI primitives and route/title/access tests.
- Playwright browser smoke tests for customer admin-route blocking and administrator navigation.
- CI frontend lint, npm audit, Vitest, production build and Playwright workflow stages.
- Frontend Vitest excludes the Playwright e2e directory so the two runners remain separate.
- CSRF policy no longer uses a blanket /api/** exemption. CSRF remains enabled for state-changing requests without Bearer credentials; public stateless auth endpoints and explicit Bearer requests are handled by a documented matcher with integration regression coverage.

## Remaining engineering work

### Frontend architecture and browser coverage
- Admin workspaces are modularized, but App.jsx still contains parts of storefront/auth route composition. A complete application-wide split into feature routes, contexts and hooks remains.
- Playwright coverage is currently an administrative navigation/access smoke suite. Full end-to-end customer registration, cart, checkout, simulated-payment and order-history journeys remain to be automated.
- No comprehensive cross-browser, visual-regression or accessibility audit has been performed; perform keyboard/mobile checks in isolated staging.

### Dependency reproducibility
- frontend/package-lock.json is not committed. CI currently uses npm install; npm ci cannot be used reproducibly until a reviewed lockfile is generated and committed in a network-enabled environment.

### Runtime and deployment verification
- No measured JVM tuning for a 512 MB instance. Collect startup, heap, container RSS and health-readiness measurements on a target-equivalent runtime before selecting flags.
- MySQL 8.4 integration CI is not TiDB compatibility proof. Complete the isolated verification matrix in docs/TIDB_VERIFICATION.md.
- Render environment variables, deployed service status and live routing were not inspected or changed. Owner must verify staging configuration and readiness on the actual target environment.

## Existing controls confirmed

- Admin bootstrap normalizes the configured email, logs non-secret outcomes and defaults takeover of an existing account to false.
- Customer registration reserves the configured admin email, normalizes email, handles duplicate-key races and rejects weak/common/email-equal passwords.
- Pricing is calculated server-side; defaults are INR 49 flat shipping, INR 1,999 free-shipping threshold after discounts and 18% tax.
- Checkout rejects unavailable products; order transitions and payment confirmation are validated; paid-order cancellation is blocked.
- Unpaid orders expire on a Clock-based 30-minute default; stock/coupon usage restoration is handled by the expiry flow.
- Simulated payments default off and are unavailable when the setting is disabled.
- /api/admin/** requires ADMIN; customer-owned resources are scoped to the authenticated principal.
- Public product responses expose availability indicators rather than exact stock.
- Hibernate uses ddl-auto=validate; existing Flyway migrations V1–V4 were not edited.

## Current verification status

The previous PR head's backend Maven/MySQL verification and Docker build passed, but its frontend Vitest job failed because Vitest discovered the Playwright spec. A Vitest exclusion fix and CSRF-boundary hardening/tests have now been committed; the resulting CI and CodeQL runs are pending. Do not merge until the latest PR head has successful frontend tests, production build, Playwright smoke tests, backend Maven/MySQL verification, dependency review and CodeQL results.

A successful CI run does not replace owner-operated TiDB, Render or target-instance resource verification.
