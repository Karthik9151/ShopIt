# ShopIt requirements traceability

Status is measured against the requested scope on shopit-improvements. Existing means present on the audited main baseline; Added means implemented on the working branch; Remaining means further implementation or owner/runtime verification is still required.

| Requirement area | Status | Evidence / remaining work |
|---|---|---|
| Work on isolated branch; leave main and backup untouched | Added | All continuation work is committed to shopit-improvements and PR #47; no merge performed |
| Admin bootstrap, reserved admin identity and safe registration | Existing | AdminBootstrap and AuthService; takeover defaults off, registration normalizes identity and rejects conflicts/weak credentials |
| Server-owned pricing and checkout calculations | Existing | Shared PricingService; INR 49 default shipping, INR 1,999 free-shipping threshold after discounts, 18% tax |
| Cart availability, legal order transitions and unpaid-order expiry | Existing | CartService, OrderStatusTransitions, PaymentService and Clock-based expiration |
| Simulated-payment guard and schema validation | Existing | Simulated payments default off; Hibernate ddl-auto=validate; Flyway V1–V4 unchanged |
| Admin role boundary | Existing | /api/admin/** requires ADMIN; integration tests cover anonymous/customer/admin access |
| Customer/order/product/category/coupon/inventory administration | Added | Paginated/searchable APIs, detail views, status updates, customer history, inventory filters and CSV exports |
| Dashboard analytics | Added | Verified daily sales, order-status breakdown, top paid products, low-stock items and recent orders; summary fields retained |
| Public stock and inactive-category disclosure controls | Existing/Added | Public product responses disclose availability indicators, not exact stock; inactive-category products are hidden |
| Request validation, metadata-only logging and runtime config logging | Added | Bounds, request logging without credentials/body/query secrets, non-secret startup switches |
| CSRF policy for Bearer API | Added | CSRF remains active; public auth operations and explicit Bearer requests are handled separately; regression coverage added |
| Admin route layout and dedicated workspaces | Added | Shared AdminLayout, dedicated order/product/category/customer pages, dashboard analytics, inventory and coupon workspaces |
| Shared UI primitives and route-boundary coverage | Added | Shared Button/Card/Badge/DataTable/Modal/Toast/ConfirmDialog/FormField/Skeleton/PageHeader; customer-to-admin access tests |
| Browser smoke coverage | Added | Playwright tests cover non-admin route blocking and admin navigation across order/product/category/customer workspaces; full customer checkout/payment journey is not yet covered |
| Frontend dependency audit and browser-test CI | Added | CI runs ESLint, npm audit, Vitest, Vite production build and Playwright Chromium smoke tests |
| Frontend lockfile and deterministic npm ci | Remaining | frontend/package-lock.json is not committed; current CI runs npm install |
| Full application-wide frontend modularization | Remaining | Admin UI is split into modules; App.jsx still contains portions of storefront/auth route composition |
| 512 MB JVM tuning and before/after measurement | Remaining | No target-sized memory/heap/RSS benchmark; do not claim measured tuning |
| TiDB compatibility verification | Owner action | MySQL 8.4 CI is not TiDB proof; execute the isolated staging matrix in docs/TIDB_VERIFICATION.md |
| Render staging/runtime verification | Owner action | No Render service, environment variable, live site or live database was accessed |
| Current CI and CodeQL clearance | Pending | Check runs attached to the exact latest PR head before merge; older successful runs do not validate newer commits |

## Verification boundary

The working branch has not been merged. Automated evidence must be read from GitHub Actions for the exact latest PR head SHA. No production deployment, payment transaction, Render setting, live database, customer record or TiDB instance was accessed.
