# Security implementation and production checklist

## Implemented
- BCrypt password hashing (cost 12).
- Short-lived signed JWT access tokens; the browser stores tokens in sessionStorage for this initial version.
- Backend role authorization; all /api/admin endpoints require ADMIN.
- Customer resources are queried by the authenticated principal's user ID, not by a request-supplied owner ID.
- DTO validation and entity models are not returned directly.
- Server-side product price, tax, shipping, totals and stock calculations.
- Pessimistic product row locks and transaction boundaries during checkout.
- Simulated payment stores no card number, CVV or banking credentials.
- Secrets and database credentials are environment-configured; .env is ignored.

## Before public production deployment
- Replace the development JWT fallback with a random secret of at least 256 bits stored in a managed secret store.
- Serve exclusively over HTTPS and configure trusted CORS origins.
- Add a strict Content Security Policy and security headers.
- Move access-token handling to an HttpOnly, Secure, SameSite cookie architecture with CSRF protection, or equivalent BFF.
- Implement rate limits for authentication, checkout, password reset and payment endpoints.
- Add account verification and secure reset-token workflows.
- Integrate a real payment provider with server-verified signatures, replay protection and idempotent webhook processing.
- Implement cancellation/refund policy and reconcile payment/order states.
- Add audit events, monitoring, alerting, backups, and dependency/security scanning.
- Review product image URL handling or implement controlled object storage and content validation.

This code is a development foundation, not a completed compliance or penetration-tested production system.
