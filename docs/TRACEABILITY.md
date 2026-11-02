# ShopIt requirements traceability

Status is measured against the requested scope on `shopit-improvements`. “Existing” means present on the audited `main` baseline; “Added” means implemented on this branch; “Remaining” means implementation or owner/runtime verification is still required.

| Requirement area | Status | Evidence / remaining work |
|---|---|---|
| Work on isolated branch; leave main and backup untouched | Added | All changes are committed to `shopit-improvements` / PR #47; no merge performed |
| Admin bootstrap, reserved admin identity and safe registration | Existing | AdminBootstrap and AuthService; takeover defaults off, identity normalization and weak/conflicting registration protection |
| Server-owned pricing and checkout calculations | Existing | Shared PricingService; INR 49 default shipping, INR 1,999 free-shipping threshold after discounts, 18% tax |
| Cart availability, legal order transitions and unpaid-order expiry | Existing | CartService, OrderStatusTransitions, PaymentService and Clock-based expiration |
| Simulated-payment guard and schema validation | Existing | Simulated payments default off; Hibernate `ddl-auto=validate`; Flyway V1–V4 unchanged |
| Admin authorization boundary | Existing | `/api/admin/**` requires ADMIN; integration tests cover anonymous/customer/admin access |
| Admin dashboard analytics | Added | Verified sales series, status breakdown, top paid products, low-stock products and recent orders |
| Admin product/order/customer detail and history | Added | Admin order/product/customer detail and paginated customer order history with timeline |
| Admin inventory/category/coupon workspaces | Added | Paginated inventory filters, paged categories/coupon buckets, product/category state operations |
| Order/inventory exports and safe CSV handling | Added | Filtered CSV exports with row limits and formula neutralization |
| Public stock and inactive-category disclosure controls | Existing/Added | Public responses expose availability indicators, not exact stock; inactive-category products are hidden |
| Request validation/logging and runtime configuration logging | Added | Bounds and metadata-only logs; no credential/body/query secrets in request metadata |
| CSRF boundary for stateless Bearer API | Added | No blanket CSRF disable or `/api/**` exemption; explicit Bearer/public-auth matcher and regression tests |
| Local-only demo datasource guard | Added | Demo profile allows only loopback MySQL and rejects other datasource URLs before datasource/Flyway startup |
| Admin route layout and dedicated workspaces | Added | Shared AdminLayout and dedicated orders/products/categories/customers pages, dashboard analytics, inventory and coupons |
| Shared UI primitives and route/access regression tests | Added | Button/Card/Badge/DataTable/Modal/Toast/ConfirmDialog/FormField/Skeleton/PageHeader; nested admin access tests |
| Playwright browser verification | Added | 4 Chromium tests: 3 admin/customer boundary/navigation tests plus mocked registration → cart → checkout → order detail/history journey; verifies no payment call when simulated payment is disabled |
| Frontend dependency audit and browser-test CI | Added | ESLint, npm audit, Vitest, Vite build and Playwright in CI |
| Frontend lockfile and deterministic `npm ci` | Remaining | `frontend/package-lock.json` is not committed; CI uses `npm install` |
| Frontend feature modularization | Added | `App.jsx` is a small routing/layout shell; auth context, route helpers, storefront/customer pages, admin dashboard/detail and admin operations are in feature modules |
| Customer lifecycle browser E2E | Added | Playwright uses mocked API contracts for register/cart/checkout/order detail/history and asserts disabled simulation does not call payment; live browser-to-backend staging journey remains owner verification |
| 512 MB JVM tuning and before/after measurement | Owner action | No target-sized memory/heap/RSS benchmark |
| TiDB compatibility verification | Owner action | MySQL 8.4 CI is not TiDB proof; execute `docs/TIDB_VERIFICATION.md` matrix |
| Render staging/runtime verification | Owner action | No Render service, environment variable, live site or live database was accessed |

## Verification status

Latest verified PR head: `32c4b233902028634d923107acffa8b4646cb4a1`.

- ShopIt CI run `41483344271`: frontend lint, dependency audit, Vitest (39/39), production build, Playwright (4/4), backend Maven/MySQL 8.4 verification (73 tests, no failures) and backend container build all passed.
- CodeQL run `41483344120`: Java/Kotlin and JavaScript/TypeScript analyses passed.
- Dependency review run `41483344287`: passed.
- Branch comparison: 142 commits ahead of `main`, 0 behind; no Flyway migration files changed.
- PR #47 is open, ready for review, and unmerged.

These results do not replace owner-operated TiDB, Render or target-instance resource verification. No production deployment, payment transaction or live database operation was performed.
