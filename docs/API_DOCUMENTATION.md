# API documentation

Interactive OpenAPI UI is available at /swagger-ui/index.html when the backend is running. Protected calls use Authorization: Bearer <JWT>.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | /api/auth/register | Public | Register a customer |
| POST | /api/auth/login | Public | Issue access JWT |
| GET | /api/products | Public | Search, filter, sort and page active products |
| GET | /api/products/{id} | Public | Product details |
| GET | /api/categories | Public | Active categories |
| GET/POST | /api/cart and /api/cart/items | Customer | View/add cart items |
| PUT/DELETE | /api/cart/items/{id} | Customer | Update/remove own item |
| DELETE | /api/cart | Customer | Clear own cart |
| GET/POST | /api/wishlist and /api/wishlist/items/{productId} | Customer | View/add own wishlist |
| DELETE | /api/wishlist/items/{id} | Customer | Remove own item |
| POST | /api/orders | Customer | Checkout; requestId acts as an idempotency key |
| GET | /api/orders/my | Customer | Own order history |
| GET | /api/orders/{id} | Customer | Own order only |
| POST | /api/orders/{id}/cancel | Customer | Cancel own unpaid order |
| POST | /api/payments/simulate/{orderId} | Customer | Simulated payment |
| GET | /api/payments/{id} | Customer | Own payment record |
| GET | /api/admin/dashboard | ADMIN | Summary metrics |
| POST/PUT/DELETE | /api/admin/products[/{id}] | ADMIN | Product management |
| POST/PUT/DELETE | /api/admin/categories[/{id}] | ADMIN | Category management |
| GET | /api/admin/orders | ADMIN | All orders |
| PUT | /api/admin/orders/{id}/status | ADMIN | Update fulfilment status |
| GET | /api/admin/customers | ADMIN | Paginated users |

Admin DELETE endpoints deactivate products/categories instead of deleting historical records.
