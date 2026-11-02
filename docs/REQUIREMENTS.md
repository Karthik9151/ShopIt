# Requirements and delivery status

## Implemented in this branch
- Customer registration and login with BCrypt hashes and JWT bearer authentication.
- ADMIN/CUSTOMER roles; admin API routes enforced by Spring Security.
- Product catalogue, search, category filtering, sorting and pagination.
- Admin product/category create, update and deactivate operations.
- Customer-owned cart and wishlist operations persisted in MySQL.
- Checkout and order creation, price snapshots, request ID deduplication, backend total calculation and stock validation under row locks.
- Order history, order detail ownership checks, unpaid cancellation and restock.
- Simulated payment record with explicit provider label and no card data.
- Admin summary, customer list and order status updates.
- Responsive storefront and primary customer/admin pages.
- Flyway schema, demo catalogue migration, OpenAPI and CI workflow.

## Not yet implemented
- Email verification, password reset delivery, refresh-token rotation and account profile edit endpoints.
- Address book, shipping integrations, discount/coupon support and returns/refunds.
- Real payment provider integration and signed webhook/callback verification.
- Product image storage/upload pipeline and product reviews/ratings.
- Order-status history writes and customer-facing event timeline.
- Admin customer disable/role maintenance and revenue-by-period analytics.
- API rate limiting, CSP/security headers, structured audit logging and production observability.
- Comprehensive API/service integration tests and end-to-end browser tests.

Do not enable live payments or use the simulated payment endpoint in production.
