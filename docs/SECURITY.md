# Security implementation and production checklist

## Implemented
- BCrypt password hashing (cost 12).
- Short-lived signed JWT access tokens (15 minutes by default).
- No committed/default production JWT secret: backend startup fails if `JWT_SECRET_BASE64` is absent or too short.
- Backend role authorization; all `/api/admin/**` endpoints require ADMIN.
- Customer resources are queried by authenticated principal ID, not a request-supplied owner ID. This includes cart, wishlist, orders, payments, profile, and saved addresses.
- DTO validation; JPA entities and password hashes are not returned by APIs.
- Server-side product price, tax, shipping, totals, and stock calculations.
- Pessimistic product row locks and transactions during checkout; product IDs are locked in deterministic order.
- Unpaid cancellations restore stock; paid orders cannot be cancelled through the unpaid-cancellation path.
- Simulated payment stores no card number, CVV, or banking credentials; its value is excluded from real verified sales. The simulation endpoint returns 404 and creates no payment record unless `APP_PAYMENTS_SIMULATED_ENABLED=true`; code and default configuration are disabled by default. The CI profile explicitly enables it for automated flow tests.
- Customer accounts can be disabled by an administrator.
- Product purchase cost is returned only by the ADMIN-only inventory API; the public ProductView omits this field.
- Coupon creation and management require ADMIN. Customers can only read currently published offers and validate a coupon against their own cart.
- Checkout revalidates coupons server-side and takes a pessimistic coupon-row lock while applying usage limits, within the order transaction.
- Database credentials and secrets are environment-configured; `.env` is ignored by Git.

## Authentication notes
The initial browser client stores its short-lived bearer access token in `sessionStorage`. This is acceptable for local demonstration only; any same-origin script execution can access it. Password changes do not yet revoke already-issued tokens, which remain valid until expiry. Logout clears the browser token but does not maintain a server-side token denylist.

## Before public production deployment
- Store a generated 256-bit-or-stronger JWT key in a managed secret store and rotate it safely.
- Serve exclusively over HTTPS and configure exact trusted CORS origins.
- Add a strict Content Security Policy and security headers.
- Move access-token handling to an HttpOnly, Secure, SameSite cookie architecture with CSRF protection, or use a backend-for-frontend.
- Add rate limits for authentication, checkout, password changes, password reset, and payments.
- Implement verified email delivery and single-use, expiring password-reset tokens.
- Before enabling simulated payments on Render staging, set `APP_PAYMENTS_SIMULATED_ENABLED=true` as an owner-managed environment variable. Keep it absent/false outside staging and remove it before replacing simulation with real payment integration.
- Integrate a real payment provider with server-verified signatures, replay protection, idempotent webhook processing, refunds, and reconciliation.
- Add audit events, monitoring, alerting, backups, and dependency/security scanning.
- Review coupon abuse controls and test race conditions/usage limits against the actual TiDB deployment before public promotion campaigns.
- Review external image URLs or implement controlled object storage and content validation.
- Perform a security review and end-to-end tests before accepting real customer data or payments.

This is a development application, not a completed compliance assessment or penetration-tested production service.
