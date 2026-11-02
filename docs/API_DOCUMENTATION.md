# API documentation

Interactive OpenAPI UI is available at `/swagger-ui/index.html` when the backend is running. Protected calls use `Authorization: Bearer <JWT>`.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | /api/auth/register | Public | Register a customer |
| POST | /api/auth/login | Public | Issue access JWT |
| POST | /api/auth/logout | Customer | Client-side stateless logout acknowledgement |
| GET/PUT | /api/profile | Customer | View/update own profile |
| PUT | /api/profile/password | Customer | Change own password after current-password verification |
| GET/POST | /api/addresses | Customer | List/create own delivery addresses |
| PUT/DELETE | /api/addresses/{id} | Customer | Update/delete own address |
| PUT | /api/addresses/{id}/default | Customer | Set own default address |
| GET | /api/products | Public | Search, filter by category/price, sort and page active products |
| GET | /api/products/{id} | Public | Product details |
| GET | /api/categories | Public | Active categories |
| GET | /api/coupons | Public | Currently valid, active coupon offers |
| POST | /api/coupons/validate | Customer | Preview a coupon against the authenticated customer's cart; server calculates discount and estimate |
| GET/POST | /api/cart and /api/cart/items | Customer | View/add cart items |
| PUT/DELETE | /api/cart/items/{id} | Customer | Update/remove own item |
| DELETE | /api/cart | Customer | Clear own cart |
| GET/POST | /api/wishlist and /api/wishlist/items/{productId} | Customer | View/add own wishlist |
| DELETE | /api/wishlist/items/{id} | Customer | Remove own item |
| POST | /api/orders | Customer | Checkout; requestId acts as an idempotency key |
| GET | /api/orders/my | Customer | Own order history |
| GET | /api/orders/{id} | Customer | Own order only |
| POST | /api/orders/{id}/cancel | Customer | Cancel own unpaid order and restore stock |
| POST | /api/payments/simulate/{orderId} | Customer | Simulated payment only |
| GET | /api/payments/{id} | Customer | Own payment record |
| GET | /api/admin/dashboard | ADMIN | Summary metrics, inventory cost budget, potential retail value and estimated gross margin |
| GET | /api/admin/products | ADMIN | All inventory including inactive items and private unit cost |
| GET/POST | /api/admin/coupons | ADMIN | List and create owner-managed coupon campaigns |
| PUT | /api/admin/coupons/{id} | ADMIN | Update coupon rules and validity window |
| PUT | /api/admin/coupons/{id}/status | ADMIN | Pause or reactivate a coupon |
| POST/PUT/DELETE | /api/admin/products[/{id}] | ADMIN | Product management, including private unit purchase cost on admin requests/responses |
| POST/PUT/DELETE | /api/admin/categories[/{id}] | ADMIN | Category management |
| GET | /api/admin/orders | ADMIN | All orders |
| PUT | /api/admin/orders/{id}/status | ADMIN | Update fulfilment status |
| GET | /api/admin/customers | ADMIN | Paginated customer list |
| PUT | /api/admin/customers/{id}/status | ADMIN | Enable/disable a customer account |

Coupon validation and redemption are performed on the server; checkout rechecks the coupon's validity, minimum order value, usage cap, and discount under a row lock. Public product DTOs do not contain cost price. Customer-owned resources are scoped using the authenticated principal. Admin DELETE endpoints deactivate products/categories instead of deleting historical records.
