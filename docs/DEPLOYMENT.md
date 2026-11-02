# ShopIt staging deployment on Render

This runbook uses Render for the ShopIt frontend and Spring Boot API, while preserving the existing MySQL-based application through TiDB Cloud Starter (MySQL-compatible). Use this as a staging environment first. The payment workflow is simulated only.

## 1. Hosting architecture

| Component | Provider | Service |
|---|---|---|
| React/Vite frontend | Render | Free Static Site |
| Java 21 / Spring Boot API | Render | Free Docker Web Service |
| SQL database | TiDB Cloud | Starter, MySQL-compatible |

Render's managed relational database is PostgreSQL, not MySQL. Render Free services cannot attach persistent disks, so running a self-hosted MySQL container on a Free service would not provide durable storage. TiDB Cloud Starter preserves the MySQL protocol/driver path without requiring a paid Render disk.

## 2. Create the MySQL-compatible database

1. Open [TiDB Cloud](https://tidbcloud.com/) and sign up.
2. Create a **TiDB Cloud Starter** instance with MySQL compatibility.
3. Choose the AWS Singapore region (`ap-southeast-1`) to keep it geographically close to the Render Singapore backend.
4. Open the instance and select **Connect**. Copy the public hostname, port (normally `4000`), exact generated username, and generated password. The username can contain an instance-specific prefix; copy it exactly rather than replacing it with `root`.
5. Use the TiDB SQL Editor to create the application database:

```sql
CREATE DATABASE shopit;
```

6. Keep the endpoint protected by TLS. TiDB Cloud Starter requires TLS; Java uses the system root CA store, so the backend JDBC URL below uses certificate and hostname verification.
7. In the TiDB Cloud Starter firewall/network settings, restrict access to the outbound IP ranges of the Render backend service after that service has been created. Do not allowlist your phone's current IP; the database connection originates from Render.

TiDB Starter is MySQL-protocol compatible, but it is not the MySQL server distribution. ShopIt's CI currently verifies migrations and integration behavior against MySQL 8.4. Therefore, staging must verify that Flyway migrations and checkout/locking behavior also work against TiDB before using it for anything beyond testing. Stay within the free quota shown in the TiDB dashboard; read/write access can be throttled when a free quota is exhausted. Flyway V4 adds coupon campaign data, order discount snapshots, and product purchase cost. Verify this migration plus coupon row-lock/usage-limit behavior against TiDB before relying on it for a live promotion.

## 3. Deploy the backend on Render

1. Open [Render Dashboard](https://dashboard.render.com/).
2. Select **New → Web Service**.
3. Connect the GitHub repository `Karthik9151/ShopIt`.
4. Select the `main` branch.
5. Set **Language** to **Docker**.
6. Set **Dockerfile Path** to `./backend/Dockerfile`.
7. Set **Docker Build Context Directory** to `./backend`.
8. Choose the **Free** instance type.
9. Choose the **Singapore** region.
10. Set the health check path to `/actuator/health`.
11. Create the service. Render injects the `PORT` environment variable; the Spring configuration already reads it.

The Dockerfile uses a multi-stage Maven/JDK 21 build and runs the packaged Spring Boot JAR as a non-root user.

## 4. Configure backend environment variables

In Render → backend service → **Environment**, add:

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://<TiDB-host>:4000/shopit?sslMode=VERIFY_IDENTITY&serverTimezone=UTC` |
| `DB_USERNAME` | Exact username copied from TiDB's Connect dialog |
| `DB_PASSWORD` | Generated TiDB database password |
| `JWT_SECRET_BASE64` | Newly generated Base64 secret representing at least 32 random bytes |
| `JWT_EXPIRATION_MS` | `900000` |
| `APP_ADMIN_EMAIL` | Dedicated administrator email address |
| `APP_ADMIN_PASSWORD` | Unique strong administrator password (at least 12 characters) |
| `APP_CORS_ORIGINS` | Fill in the exact Render frontend origin after the frontend is deployed |

Generate the JWT key locally, for example in Android Termux:

```bash
openssl rand -base64 32
```

Copy its output directly into the Render Environment editor. Do not commit it, share it in chat, or use the test secret from GitHub Actions.

Set `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` before the first backend deployment. Do not first register that email as an ordinary customer. The bootstrap does not overwrite an existing account. After administrator access is confirmed, change the password through the account workflow and remove `APP_ADMIN_PASSWORD` from Render's environment variables.

Save the variables and trigger a deploy. Confirm that the deploy completes and that `https://<render-backend-domain>/actuator/health` reports healthy.

## 5. Restrict TiDB network access to Render

After the backend service exists:

1. Open the backend service in Render.
2. Open **Connect → Outbound** and copy every displayed CIDR range for the Singapore region.
3. Open the TiDB Cloud Starter instance's firewall settings.
4. Add the Render backend's outbound CIDR ranges.
5. Remove any broad test rule that permits all IP addresses, once the Render rules have been verified.
6. Keep TLS enabled. Do not expose database credentials in the frontend.

Render's outbound IP ranges are shared by services in the same region. If the service's region changes, re-check the ranges and update TiDB's firewall.

## 6. Deploy the frontend on Render

1. In Render, select **New → Static Site**.
2. Connect the same repository `Karthik9151/ShopIt`.
3. Select the `main` branch.
4. Build command: `cd frontend && npm install --no-audit --no-fund && npm run build`.
5. Publish directory: `./frontend/dist`.
6. Add the build-time environment variable:

```dotenv
VITE_API_BASE_URL=https://<render-backend-domain>/api
```

7. Create the Static Site and copy its exact `https://<name>.onrender.com` origin.

The `render.yaml` Blueprint contains the React Router fallback rewrite to `/index.html`. If you create the Static Site manually rather than applying the Blueprint, add a Render Static Site rewrite rule with source `/*` and destination `/index.html`.

## 7. Finish CORS and redeploy

Return to Render → backend service → Environment and set:

```dotenv
APP_CORS_ORIGINS=https://<exact-render-frontend-domain>
```

Use only the frontend origin (scheme and hostname), without `/api`, a trailing slash, or wildcard. Save and redeploy the backend.

If the frontend's domain changes, update CORS. If the backend URL changes, update `VITE_API_BASE_URL` on the frontend Static Site and redeploy the frontend.

## 8. Smoke tests from a mobile browser

1. Open `https://<render-backend-domain>/actuator/health`; expect a healthy response.
2. Open the Render frontend URL and verify the home page, product catalogue, and product detail pages.
3. Refresh direct routes such as `/products`, `/login`, `/register`, `/orders`, and `/admin`; each should load the React application.
4. Register a normal customer account and sign in.
5. Sign in separately with the administrator credentials and confirm the admin dashboard and inventory load.
6. As an admin, create/edit/deactivate a category and product; verify the public catalogue updates.
7. Verify the admin customer list and enable/disable controls.
8. As a customer, test profile update, password change, saved address, cart, wishlist, logout, and login.
9. Place a test order. Verify server-calculated totals, stock decrement, and cart clearing.
10. Run the simulated payment flow only; verify the UI states that no real money was charged.
11. Confirm a second customer cannot access the first customer's orders, profile, addresses, cart, or wishlist.
12. Check Render deploy logs, health checks, TiDB usage, and GitHub Actions.

## 9. Render Free plan limitations

- A Free Render web service spins down after 15 minutes without incoming traffic. Its first request after sleeping can take about one minute to become available.
- Free web services have limited CPU and memory and can restart.
- Free web services cannot attach persistent disks. Never use their local filesystem for database files or uploaded product images.
- Render Free Postgres databases expire after 30 days; this runbook deliberately does not use one.
- Free instance hours, bandwidth, and build minutes are subject to the workspace's current included usage.

This is suitable for learning, staging, and demonstrations, not a dependable production store.

## 10. Rate limiting and trusted proxy headers

The API applies in-memory limits to login, registration, checkout, password changes, and simulated payments. The default implementation uses the socket peer address for IP buckets and does not trust client-supplied forwarding headers. On a hosting proxy, confirm whether it strips/replaces inbound `X-Forwarded-For` before setting `APP_RATE_LIMIT_TRUST_FORWARDED_FOR=true`. Do not enable that switch on an untrusted/direct listener. In-memory limits reset on service restart and are not shared across multiple API instances.

## 11. Before real production

The current payment integration is simulated only. Do not accept real payments until a real provider, verified server-side webhooks, refunds, and reconciliation are implemented and tested.

Before public production, also complete the security work in `docs/SECURITY.md`: safer token handling (HttpOnly/Secure cookies with CSRF protection or a backend-for-frontend), strict CSP/security headers, rate limits, email verification and password reset, audit/monitoring, database backups and restore tests, and end-to-end testing. Confirm a durable paid database plan and operational recovery process before storing real customer/order data.
