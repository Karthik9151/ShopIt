# ShopIt Known Issues and Audit Record

Audit date: 2026-11-02  
Audited baseline: `main`  
Working remediation branch: `shopit-improvements`

## Audit boundary

The audit was performed by reading repository source, tests, configuration, and workflow files. No live Render service, production site, live database, or customer data was accessed. The working branch was created from `main`; no Flyway migration file was changed.

## Resolved on `shopit-improvements` (CI verification pending)

| Finding | Evidence | Change |
|---|---|---|
| Active products in inactive categories were visible in the public catalogue and by direct product URL. | The public product JPQL filtered only `p.active`; the detail service filtered only the product active flag. | Public search now uses a left join and requires any assigned category to be active. Product detail applies the same rule. Products without a category remain visible. |
| Public product search accepted unbounded search text. | `ProductController.list(q)` had no maximum length. | Search input is limited to 120 characters and an integration test asserts oversized input returns 400. |
| Login and current-password DTO fields lacked explicit upper bounds. | `LoginRequest` and `PasswordChangeRequest` did not bound all credential fields. | Login email/password and current password now have explicit limits consistent with the database and password policy. |
| Request trace IDs existed, but request metadata was not logged. | `TraceIdFilter` generated a trace ID but did not log request method, path, status, duration, or user ID. | Added a security-chain request logger that emits metadata only. A test checks that body content, query values, and Authorization credentials are absent from the log output. |

## Remaining confirmed gaps

### Admin API and workflow coverage

The current `AdminController` now includes paginated product/order/customer lists, product stock editing, all-category listing/reactivation with product counts, customer search/detail, product/category management, coupon management, and order status changes. The following requested capabilities remain incomplete:

- Date, order-ID, and customer-email filters for admin orders.
- Server-backed order and inventory CSV exports with a 5,000-row cap and spreadsheet-formula protection.
- Dashboard series and grouped views for sales-by-day, status breakdown, top products, low-stock rows, and recent orders.
- Customer order-history detail and richer customer account activity.

The current dashboard returns basic counts, payment totals, and inventory valuation. It does not supply all of the requested grouped dashboard datasets.

### Frontend structure and admin usability

- Storefront and admin route/page implementations remain concentrated in `frontend/src/App.jsx`; the requested page/component/context/hook split and shared `AdminLayout` are not complete.
- The current admin route guard redirects a non-admin user to the storefront rather than rendering the requested explicit “Admin access required” page.
- The requested dedicated shared UI component set and per-route page-title behavior have not been completed or verified.
- Playwright end-to-end scenarios for the full customer/admin lifecycle are not present in the frontend package scripts or the current CI workflow.

### Runtime and deployment verification

- `backend/Dockerfile` starts Java without explicit 512 MB-oriented JVM limits. No before/after startup, health, heap, or peak-RSS measurements were available; JVM flags should not be selected or described as measured without running on the target-sized instance.
- No `scripts/verify-tidb.sh` exists on the audited baseline. The CI workflow runs Flyway-backed Maven verification against MySQL 8.4, which is not proof of TiDB compatibility.
- Render staging environment variables and live deployment behavior were not inspected. In particular, the staging value of `APP_PAYMENTS_SIMULATED_ENABLED` is unknown.

### Dependency and CI validation

- `frontend/package-lock.json` is absent, and the current workflow installs frontend dependencies with `npm install`; deterministic `npm ci` is therefore not available yet.
- The current CI workflow runs frontend unit tests/build, backend Maven verification against MySQL, and a backend container build. It does not run the requested Playwright end-to-end suite or an explicit `npm audit` step.
- Dependabot and CodeQL configuration already exist on `main`; they were not recreated in this branch.

## Controls confirmed in the audited baseline

The following requested controls were already present in source and were not reimplemented here:

- Admin bootstrap normalizes the configured email, logs non-secret outcomes, and defaults takeover of an existing account to false.
- Customer registration reserves the configured admin email, normalizes email, translates duplicate-key races to 409, and rejects email-equal/common passwords.
- Cart, coupon preview, and checkout use the shared server-side `PricingService`; the baseline defaults are INR 49 shipping, INR 1,999 free-shipping threshold after discounts, and 18% tax.
- Cart lines expose availability state; checkout rejects unavailable products with 409.
- Order transitions are validated, payment confirmation requires a successful payment, paid orders cannot be cancelled, and order status history is returned as a timeline.
- Unpaid orders have a 30-minute expiry flow using an injectable `Clock`; expiration restores stock/coupon usage under order-level locking.
- Simulated payments default off and return 404 when disabled. CI enables simulation through its test profile.
- `/api/admin/**` is restricted to the ADMIN role; admin product/order/customer listing is paginated.
- Public product responses expose `inStock` and `lowStock` rather than exact stock.
- API docs default off; response security headers are applied; GET requests use a 60-second timeout and a single retry for eligible transient failures.
- `ddl-auto` is `validate`; existing Flyway migrations V1–V4 were not edited.

## Verification status

The required local commands were not run: this execution environment could not resolve `github.com` to clone the repository, so it had no local checkout. GitHub Actions on the pull request is the available build/test verification path. A successful MySQL CI run must not be reported as a TiDB staging verification.
