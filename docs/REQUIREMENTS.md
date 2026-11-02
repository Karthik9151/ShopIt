# Requirements and delivery status

## Implemented in this branch
- Customer registration and login with BCrypt hashes and JWT bearer authentication.
- ADMIN/CUSTOMER roles; admin APIs are protected by Spring Security.
- Own profile update and current-password-verified password change.
- Customer-owned delivery address book with default address selection.
- Product catalogue, search, category and price filtering, sorting and pagination.
- Admin product/category create, update and deactivate operations.
- Admin customer listing and enable/disable controls.
- Customer-owned cart and wishlist, persisted in MySQL.
- Checkout and order creation, price snapshots, request ID deduplication, server-side totals, stock validation under row locks.
- Order history, order ownership checks, unpaid cancellation and inventory restoration.
- Simulated payment record, explicitly labelled and without card data.
- Admin metrics that distinguish simulated value from real verified sales.
- Responsive storefront, account screens, and admin workspace.
- Flyway schema, sample catalogue migration, OpenAPI documentation, and GitHub Actions CI.

## Not yet implemented
- Email verification and email-delivered forgot-password/reset workflow (requires a configured mail provider).
- Refresh-token rotation, token revocation across active sessions, and forced logout on password change.
- Real payment provider integration, signed callback/webhook verification, refunds, and payment reconciliation.
- Shipping carrier integration, discounts/coupons, returns, reviews/ratings, and image object storage.
- Order-status history writes and a customer-facing event timeline.
- More comprehensive API/service integration tests, browser end-to-end tests, and production load tests.
- Production rate limiting, CSP/security headers, audit logging, monitoring, backup validation, and security review.

Do not enable live payments or use the simulated payment endpoint in production.
