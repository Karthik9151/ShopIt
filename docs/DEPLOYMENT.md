# ShopIt staging deployment (mobile-browser workflow)

This runbook deploys the React/Vite frontend to Vercel and the Spring Boot API plus MySQL 8.4 to Railway. Use a staging environment first. The current payment flow is simulated only.

## 1. Railway project and MySQL

1. Sign in to Railway with the GitHub account that can access `Karthik9151/ShopIt`.
2. Create a project named `ShopIt-Staging`.
3. Add a MySQL database service and name it exactly `MySQL`.
4. Keep the database private. The backend and MySQL should be in the same Railway project/environment.

Railway's MySQL service provides `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, and `MYSQLDATABASE` variables.

## 2. Railway backend service

1. Add a service from the GitHub repository `Karthik9151/ShopIt`.
2. Select branch `main`.
3. Set the service root directory to `/backend`.
4. Set Java version by adding `RAILPACK_JDK_VERSION=21`.
5. Set build command to `mvn -B -DskipTests package`.
6. Set start command to `java -jar target/shopit-api-1.0.0.jar`.
7. Set health check path to `/actuator/health`.
8. Generate a Railway public domain for the API after the first successful deploy.

## 3. Backend variables

Add these variables to the backend service. The service reference namespace `MySQL` must exactly match the database service name.

```dotenv
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET_BASE64=<base64 encoding of at least 32 cryptographically random bytes>
JWT_EXPIRATION_MS=900000
APP_ADMIN_EMAIL=<dedicated administrator email>
APP_ADMIN_PASSWORD=<unique strong password of at least 12 characters>
APP_CORS_ORIGINS=<exact Vercel production origin, added after the frontend deploy>
```

Generate the JWT key locally, for example on Android Termux:

```bash
openssl rand -base64 32
```

Copy the output directly into Railway's secret variable editor. Never commit it or paste it into chat. Do not use the example/test JWT keys from source control or CI.

Railway supplies `PORT`; do not hard-code a production port. The Spring configuration already reads it.

Set `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` before the first backend deployment so the bootstrap process can create the administrator. Do not register that email first as a customer. After confirming administrator login, change the password in the account and remove `APP_ADMIN_PASSWORD` from the Railway service variables. The bootstrap does not overwrite existing accounts.

## 4. Vercel frontend

1. Sign in to Vercel and import `Karthik9151/ShopIt` from GitHub.
2. Set the project root directory to `frontend`.
3. Select Vite as the framework preset.
4. Set Node.js to 22.x.
5. Build command: `npm run build`.
6. Output directory: `dist`.
7. Install command: `npm install`.
8. Add the production environment variable:

```dotenv
VITE_API_BASE_URL=https://<railway-api-domain>/api
```

Use the HTTPS Railway API domain generated in step 2. This is a public frontend configuration value, not a secret. Do not put database credentials, JWT secrets, or admin passwords in Vercel.

Deploy the frontend and note its exact production origin, for example `https://shopit-example.vercel.app`.

The `frontend/vercel.json` file in this repository configures the SPA fallback required by React Router so direct navigation or refresh on routes such as `/products`, `/orders`, and `/admin` serves `index.html`.

## 5. Finish CORS configuration

Return to Railway → backend → Variables and set:

```dotenv
APP_CORS_ORIGINS=https://<exact-vercel-production-origin>
```

Use only the origin (scheme + hostname), with no trailing path or `/api`. For multiple trusted origins, separate them with commas. Do not use `*`.

Redeploy the backend after saving the variable. If Vercel's project domain changes, update this value.

## 6. Smoke tests

1. Open `https://<railway-api-domain>/actuator/health`; expect a healthy response.
2. Open the Vercel website on mobile and confirm the home page and product catalogue load.
3. Register a separate ordinary customer account.
4. Sign in as the administrator and confirm the admin dashboard loads.
5. As administrator, create/edit/deactivate a category and product; verify the public catalogue changes.
6. As administrator, verify customer listing and enable/disable controls.
7. As customer, test profile update, password change, saved address, cart, wishlist, and logout/login.
8. Place a test order. Confirm totals are server-calculated, inventory is decremented, and the cart is cleared.
9. Use only the explicitly labelled simulated-payment workflow; verify the UI says no real money was charged.
10. Confirm a customer cannot access another customer's profile, address, cart, wishlist, or order.
11. Test direct URL refresh on `/products`, `/login`, `/orders`, and `/admin`.
12. Review Railway deploy logs and GitHub Actions checks for errors.

## 7. Before real production use

Do not treat this staging launch as approval for live sales. The current application does not integrate a real payment gateway. Before public production, complete the security items in `docs/SECURITY.md`, including safer token handling (HttpOnly/Secure cookies with CSRF protection or a backend-for-frontend), strict CSP/security headers, rate limiting, account verification and password reset, backups/restore testing, audit/monitoring, and end-to-end testing. Add verified payment-provider webhooks, refunds, and reconciliation before accepting real payments.
