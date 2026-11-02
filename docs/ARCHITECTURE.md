# Architecture

- Browser: React routes and component state; Axios sends JSON REST requests.
- API: stateless Spring Security filter chain validates bearer JWTs; controllers validate DTOs; services enforce business rules and transactions; repositories access JPA entities.
- Database: MySQL 8 with Flyway-managed schema; Hibernate runs in validate mode.

Backend boundaries: controller (HTTP), dto (validated inputs and explicit outputs), service (business rules), repository (persistence), entity (data model), security (JWT and roles), exception (sanitized errors).

Trust boundaries:
- Never accept customer-supplied prices, tax, shipping, totals, role, or user IDs.
- Resolve customer identity from the authenticated principal and scope repository reads/writes to that account.
- Lock product rows and validate current stock during checkout.
- Payment status is simulated only; real gateway callbacks require cryptographic verification.

Checkout reads the authenticated customer's cart, locks each product, validates active status and stock, snapshots current name/price, calculates subtotal/tax/shipping, creates the order, decrements inventory and clears the cart in one transaction.

## Admin and observability boundaries

All owner console routes are under `/api/admin/**` and require the ADMIN role through the central Spring Security chain. Controllers bind validated request DTOs and return explicit view DTOs; entity objects and password hashes are not exposed. Exact product stock and `costPrice` remain in admin-only DTOs.

Public product catalogue queries join categories and suppress products whose assigned category is inactive. Products with no category remain visible if the product itself is active.

The trace filter assigns a trace identifier before the security chain. The request-log filter logs method, path (without query string), response status, duration, numeric user ID when available, and trace ID. It does not read or log request bodies, headers, or credentials. Startup configuration logs only selected non-secret runtime policy values.
