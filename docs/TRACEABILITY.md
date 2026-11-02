# ShopIt requirements traceability

Status is measured against the requested scope on `shopit-improvements`, not against older work-package branches. “Existing” means found on the main baseline; “Added” means committed on this branch; “Remaining” means not fully implemented or verified.

| Requirement area | Status | Evidence / remaining work |
|---|---|---|
| Work on isolated branch from main; no main/backup edits | Added | `shopit-improvements` created from `main`; backup branch was not found and not touched |
| Audit and known issues document | Added | `docs/KNOWN_ISSUES.md` |
| Admin bootstrap normalization/takeover default false | Existing | `AdminBootstrap`; no password logging |
| Reserve admin email; registration race and weak-password checks | Existing | `AuthService` |
| Shared server pricing (49 shipping, 1999 free threshold after coupon, 18% tax) | Existing | `PricingService` shared by cart/coupon/checkout |
| Cart availability and checkout 409 | Existing | `CartService`, `OrderService` |
| Legal order transitions, successful-payment confirmation, paid cancellation 409 | Existing | `OrderStatusTransitions`, `OrderService`, `PaymentService` |
| Order history timeline and 30-minute unpaid expiry | Existing | history service/processor/scheduler use `Clock` |
| Simulated payment off by default | Existing | 404 when `APP_PAYMENTS_SIMULATED_ENABLED=false` |
| API error mapping and baseline security headers | Existing | `ApiExceptionHandler`, `ApiSurfaceFilter` |
| Rate limits and default-off API docs | Existing | config-driven filters; test profile disables limits |
| Hide products in inactive categories | Added | public catalogue query and detail check; integration regression test |
| Input bounds for product search, login and current password | Added | DTO/controller validation and regression test |
| Metadata-only request logging | Added | `RequestLogFilter`, tests exclude headers/body/query secrets |
| Startup non-secret configuration logging | Added | `RuntimeConfigurationLogger` and test |
| Admin stock edit 0–1,000,000 | Added | `PATCH /api/admin/products/{id}/stock`, role/bounds integration test |
| Admin category list/reactivation/product counts | Added | `GET /api/admin/categories`, `PATCH /api/admin/categories/{id}/status`, integration test |
| Admin customer search/detail excluding admins | Added | `GET /api/admin/customers?q=...`, `GET /api/admin/customers/{id}`, integration test |
| Admin date/email/order-ID filters and CSV exports | Added | Bounded order filters; UTF-8 order/inventory CSV exports capped at 5,000 with formula-safe cells |
| Full dashboard chart/status/top-product data | Remaining | Add grouped SQL/JPA queries and response DTOs |
| Full React admin layout/pages/shared UI component set | Remaining | `frontend/src/App.jsx` remains monolithic and the full route/page set plus shared UI components is not implemented; a signed-in non-admin now sees an explicit access-required page |
| Existing-route page titles and non-admin login redirect boundary | Added | Route titles and safe post-login path selection have Vitest coverage; full storefront polish/accessibility audit remains |
| Playwright e2e workflow and npm audit in CI | Remaining | Existing CI runs Vitest/build and MySQL Maven verification only |
| Frontend lockfile and npm ci | Remaining | `frontend/package-lock.json` absent on baseline; need network-enabled lock generation |
| 512 MB JVM tuning and before/after measurement | Remaining | Do not apply unmeasured flags; benchmark on a 512 MB-equivalent runtime |
| TiDB script/staging migration verification | Remaining | No `scripts/verify-tidb.sh` or TiDB run evidence on baseline; MySQL CI is not TiDB proof |
| Update every deployment/Render/GitHub setting | Owner action | No Render or GitHub repository settings changed; owner must configure staging vars/security settings |

## Verification status

The required local Maven/npm commands were not run because a local GitHub clone could not resolve `github.com` in the execution environment. CI for the exact pull-request commit is the available automated verification path. No live database, live site, or customer data was accessed.
