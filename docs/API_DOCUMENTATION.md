# API documentation

The API exposes JSON endpoints under `/api`. Protected customer requests require `Authorization: Bearer <JWT>`; all `/api/admin/**` endpoints require the ADMIN role. Request/response bodies use validated DTOs, not persistence entities.

Interactive OpenAPI/Swagger endpoints (`/swagger-ui/index.html`, `/v3/api-docs`) are hidden by default. Set `APP_API_DOCS_ENABLED=true` only in a trusted local/test environment when interactive API documentation is needed.

## Authentication and account

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a customer; the configured administrator email is reserved |
| POST | `/api/auth/login` | Public | Issue short-lived access JWT; throttled attempts return 429 and Retry-After |
| POST | `/api/auth/logout` | Stateless | Return 204 acknowledgement; browser removes token locally |
| GET/PUT | `/api/profile` | Customer | View/update own profile |
| PUT | `/api/profile/password` | Customer | Change own password after current-password verification |
| GET/POST | `/api/addresses` | Customer | List/create own delivery addresses |
| PUT/DELETE | `/api/addresses/{id}` | Customer | Update/delete own address |
| PUT | `/api/addresses/{id}/default` | Customer | Set own default address |

**JWT lifecycle:** Logout removes the current token and user record from browser `sessionStorage`, but the stateless API does not revoke a copied JWT. That token remains valid until expiry unless the account is disabled or the signing key is rotated. Password changes do not yet revoke previously issued access tokens. This is the current behavior, not a server-side token blacklist.

## Catalogue and customer commerce

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/products` | Public | Search, category/price filter, sort and paginate active products |
| GET | `/api/products/{id}` | Public | Product details; no purchase cost or exact stock |
| GET | `/api/categories` | Public | Active categories |
| GET | `/api/coupons` | Public | Currently valid, active offers |
| POST | `/api/coupons/validate` | Customer | Preview coupon against authenticated customer's cart |
| GET/POST | `/api/cart`, `/api/cart/items` | Customer | View cart/add items |
| PUT/DELETE | `/api/cart/items/{id}` | Customer | Update/remove own item |
| DELETE | `/api/cart` | Customer | Clear own cart |
| GET/POST | `/api/wishlist`, `/api/wishlist/items/{productId}` | Customer | View/add own wishlist |
| DELETE | `/api/wishlist/items/{id}` | Customer | Remove own item |
| POST | `/api/orders` | Customer | Checkout; `requestId` is an idempotency key |
| GET | `/api/orders/my` | Customer | Own order history |
| GET | `/api/orders/{id}` | Customer | Own order only |
| POST | `/api/orders/{id}/cancel` | Customer | Cancel own unpaid order and restore stock; paid-order cancellation is rejected |
| POST | `/api/payments/simulate/{orderId}` | Customer | Simulated payment only, when environment switch is enabled |
| GET | `/api/payments/{id}` | Customer | Own payment record |

## Admin

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/dashboard` | ADMIN | Full-data summary and inventory valuation; not page-limited |
| GET | `/api/admin/products` | ADMIN | Paginated inventory including inactive items/private unit purchase cost |
| POST/PUT/DELETE | `/api/admin/products[/{id}]` | ADMIN | Product management; DELETE deactivates |
| POST/PUT/DELETE | `/api/admin/categories[/{id}]` | ADMIN | Category management; DELETE deactivates |
| GET | `/api/admin/orders` | ADMIN | Paginated order history with optional status filter |
| PUT | `/api/admin/orders/{id}/status` | ADMIN | Update permitted fulfilment status |
| GET | `/api/admin/customers` | ADMIN | Paginated customer list |
| PUT | `/api/admin/customers/{id}/status` | ADMIN | Enable/disable customer account |
| GET/POST | `/api/admin/coupons` | ADMIN | List/create owner-managed campaigns |
| PUT | `/api/admin/coupons/{id}` | ADMIN | Update coupon rules and validity |
| PUT | `/api/admin/coupons/{id}/status` | ADMIN | Pause/reactivate coupon |

### Pagination contract

- Products: `GET /api/admin/products?page=0&size=20&q=phone&active=true`
- Orders: `GET /api/admin/orders?page=0&size=20&status=PENDING_PAYMENT`
- Customers: `GET /api/admin/customers?page=0&size=20`
- `page` is zero-based and defaults to 0; `size` defaults to 20 and must be 1–100.
- Product `q` and `active` filters and order `status` are optional.
- Responses are Spring Data Page JSON: read rows from `content`; metadata includes `number`, `size`, `totalElements`, `totalPages`, `first`, and `last`.
- Negative page values, invalid size values and sizes above 100 are rejected with HTTP 400.
- Dashboard valuation and summary totals intentionally use the full dataset, not the current page.

## Business rules and trust boundaries

- The server owns prices, tax, shipping, coupon discounts, totals, order status and stock calculations.
- Listed prices are tax-exclusive; checkout applies the configured tax rate (default 18%).
- Shipping is configured flat rate (default INR 49) unless the post-discount merchandise subtotal meets the free-shipping threshold (default INR 1,999).
- Coupon validity, minimum order, usage cap and discount are recalculated at checkout under a coupon row lock.
- Customer-owned resources are scoped to the authenticated principal. Admin routes require ADMIN.
- Payment is simulated only; no real money or card data is processed.
- Unpaid orders expire after the configured TTL (default 30 minutes). There is no refund/returns workflow; paid-order cancellation is blocked.

### Additional owner-console endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/categories` | ADMIN | All categories, including inactive, with product counts |
| PATCH | `/api/admin/categories/{id}/status` | ADMIN | Set category active state; reactivating makes its active products public again |
| PATCH | `/api/admin/products/{id}/stock` | ADMIN | Update exact stock; request body requires `stock` from 0 to 1,000,000 |
| GET | `/api/admin/customers?q={search}&page=0&size=20` | ADMIN | Search customers by name, email, or phone; admin accounts are excluded |
| GET | `/api/admin/customers/{id}` | ADMIN | Read one customer DTO; non-customer accounts return 404 |

The customer search query is limited to 120 characters. Existing category create/update/deactivate routes remain unchanged.

### Order search and exports

`GET /api/admin/orders?page=0&size=20` also accepts optional `status`, `orderId`, partial `email`, `from`, and `to` parameters. The date values are ISO-8601 timestamps; when both are supplied, `to` must be later than `from`.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/orders/export.csv` | ADMIN | CSV export using the same status, order ID, email and date filters; maximum 5,000 rows |
| GET | `/api/admin/inventory/export.csv` | ADMIN | Inventory CSV; optional `threshold` stock ceiling and `sort=name|stock|price|createdAt`, `direction=asc|desc` |

Both export routes return UTF-8 CSV, neutralize spreadsheet formula prefixes in text cells, and return 400 when the result would exceed 5,000 rows. Inventory threshold must be from 0 through 1,000,000.


## Owner-console detail, reporting and paginated workspaces

All routes in this section require the ADMIN role.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | /api/admin/dashboard | Retains the existing summary and valuation fields and adds salesByDay, statusBreakdown, topProducts, lowStockItems and recentOrders collections |
| GET | /api/admin/orders/{id} | Read one order with item and status-history details |
| GET | /api/admin/customers/{id}/orders?page=0&size=20 | Paginated order history for a customer, including order timelines |
| GET | /api/admin/products/{id} | Read one product, including owner-only fields |
| PATCH | /api/admin/products/{id}/status | Set product active state using an active boolean |
| GET | /api/admin/inventory?page=0&size=20&threshold=5&sort=stock&direction=asc | Paginated inventory with optional low-stock threshold and safe sort/direction selection |
| GET | /api/admin/categories/paged?page=0&size=20 | Paginated categories with deterministic name ordering |
| GET | /api/admin/coupons/paged?bucket=ACTIVE&page=0&size=20 | Paginated campaign buckets: ACTIVE, SCHEDULED, EXPIRED or PAUSED |

The dashboard sales series uses verified/successful payment data. Top products are derived from successfully paid orders rather than simulated payment attempts. Low-stock and recent-order collections are bounded for dashboard display; the headline counts and valuation fields continue to describe the full dataset.

Page-number parameters are zero-based. Page size defaults to 20 and is bounded to 1–100. The customer order-history endpoint verifies that the requested account is a CUSTOMER; non-customer or missing IDs return 404.

## Browser authentication and CSRF boundary

The frontend sends its JWT explicitly in the Authorization: Bearer header; the backend JWT filter does not read authentication cookies, and the CORS configuration does not allow credentialed browser requests. CSRF checks remain enabled for state-changing requests that do not carry a Bearer header, apart from the public stateless /api/auth operations. This is not cookie-based authentication. Do not move JWTs into automatically attached cookies without redesigning the CSRF policy and tests.
