# ShopIt

A full-stack e-commerce application built with React/Vite, Java 21, Spring Boot 3.5, Spring Security/JWT, Spring Data JPA, Flyway, and MySQL 8.

## Local setup

Requirements: JDK 21, Maven 3.9+, Node.js 22.12+, npm, and MySQL 8.

1. Create a MySQL database named `shopit` and a database user with privileges on it.
2. Copy `.env.example` to `.env`. Generate a JWT key and fill in local values:
   ```bash
   openssl rand -base64 32
   ```
   Set the output as `JWT_SECRET_BASE64`. The backend intentionally refuses to start with a missing/weak JWT key.
3. Export the environment values in your shell (or use your IDE's environment configuration).
4. Start the API: `cd backend && mvn spring-boot:run`.
5. Start the UI in another terminal: `cd frontend && npm install && npm run dev`.
6. Open `http://localhost:5173`. API: `http://localhost:8080`. OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`.

Flyway creates the schema and seeds a small sample catalogue. Set `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` (minimum 12 characters) before first startup to provision an administrator. Bootstrap does not overwrite existing accounts by default.

## Admin setup and troubleshooting

Configure the administrator through `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD`. The email is trimmed and normalized to lowercase. The password must contain at least 12 characters. Customer registration cannot reserve or claim the configured administrator email.

| Symptom | Likely cause | Action |
|---|---|---|
| No administrator is created | `APP_ADMIN_EMAIL` is blank | Set the administrator email and restart the API. |
| Bootstrap reports a short password | `APP_ADMIN_PASSWORD` has fewer than 12 characters | Set a password of at least 12 characters and restart. |
| Existing account remains a CUSTOMER | The email already belongs to an account and takeover is disabled | Verify ownership of the account, then use the controlled takeover procedure below or correct the account through an authorized database operation. |
| Administrator login fails | Environment values do not match the provisioned account or the password was changed | Check the configured email and reset the account password through a controlled owner procedure. Never share the password in logs or support messages. |

### Controlled takeover of an existing account

The optional `APP_ADMIN_TAKE_OVER_EXISTING` environment variable defaults to `false`. When set to `true`, startup promotes an existing account with the configured email to ADMIN, enables it, and replaces its password hash with the configured `APP_ADMIN_PASSWORD`.

Use this switch only when the owner has verified control of the account and understands that, because email verification does not exist yet, an existing account may have been registered by someone else. Prefer a controlled database correction when appropriate. If using the takeover switch, enable it for one deployment only and turn it off immediately afterward. Never leave it enabled as a normal operating setting.

Bootstrap logs report whether configuration was absent, the password was too short, an existing account was left untouched, an account was created, or an account was promoted. Passwords and password hashes are never logged.

## Owner-managed catalogue and promotions

The store owner provisions the administrator account through `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD`. Only ADMIN routes can create or update products, selling prices, private unit purchase costs, categories, and coupon campaigns. Customer registration creates customer accounts only.

The administrator can publish percentage or fixed-value coupons with start/end timestamps, minimum cart values, optional maximum discounts, and redemption limits. The customer offers page shows only currently available promotions. Coupon application is previewed against the signed-in customer's cart and is revalidated during checkout; the order stores the coupon/discount snapshot.

The admin inventory screen estimates stock cost budget, potential retail value, and gross margin potential from current stock. These figures are estimates, not realized revenue or accounting profit. Product purchase costs are returned only by admin APIs and are omitted from public product responses.

## Pricing and shipping

Listed product prices are tax-exclusive. Checkout adds 18% tax as a separate line. Shipping is INR 49.00 unless the merchandise subtotal after coupon discount is at least INR 1,999.00, in which case shipping is free. The backend values are configurable through `APP_PRICING_SHIPPING_FLAT`, `APP_PRICING_FREE_SHIPPING_THRESHOLD`, and `APP_PRICING_TAX_RATE`. The frontend announcement uses `VITE_FREE_SHIPPING_THRESHOLD` (default 1999); keep it equal to `APP_PRICING_FREE_SHIPPING_THRESHOLD` on the deployed API.

## Payments

Only an explicitly simulated payment workflow is included. It does not charge money or collect card details. The admin dashboard separates simulated payment value from real verified sales. Integrate and verify a real provider server-side before enabling live payments.

## Checks

Frontend: `cd frontend && npm install && npm test -- --run && npm run build`

Backend: `cd backend && mvn -B verify`

See `docs/` for architecture, requirements, endpoint contracts, security notes, operations/recovery, TiDB staging verification, and test procedures.


## API documentation

OpenAPI and Swagger UI are disabled by default in deployed environments. Enable `APP_API_DOCS_ENABLED=true` only in a trusted local or test profile when interactive API documentation is needed. The frontend Render Blueprint uses a report-only CSP during observation; review browser violations before any enforcement.


## Authentication lifecycle

Logout clears the browser's `sessionStorage` token and user state. The API logout endpoint is stateless and does not revoke a copied JWT; previously issued tokens remain usable until expiry unless the account is disabled or the signing key is rotated. Password changes do not yet revoke existing access tokens. See `docs/API_DOCUMENTATION.md` and `docs/SECURITY.md` for this current limitation.
