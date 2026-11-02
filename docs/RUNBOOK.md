# ShopIt operations runbook

This runbook covers local and isolated staging operation. It does not authorize production database access or changes.

## Environment safety

Never copy secrets into source control, issue comments, CI logs, shell history, or support messages. Keep database credentials and JWT signing keys in a managed secret store. Do not point local tests or demo profiles at a shared database.

The normal application profile uses `ddl-auto: validate` and Flyway for schema changes. Do not modify historical migrations V1–V4; add a forward migration only when a schema change is justified. Verify the migration on MySQL CI and then independently on an isolated TiDB staging cluster.

## Environment variables

| Variable | Purpose | Safe baseline |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL/TiDB connection | Environment/secret store only |
| `JWT_SECRET_BASE64` | JWT signing key | Random key of at least 32 bytes; never commit |
| `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` | Admin bootstrap | Dedicated owner account; remove bootstrap password after setup |
| `APP_ADMIN_TAKE_OVER_EXISTING` | Explicit existing-account promotion | `false`; enable only for a controlled one-deploy takeover |
| `APP_PAYMENTS_SIMULATED_ENABLED` | Exposes simulated payments | `false` by default; staging-only when needed |
| `APP_API_DOCS_ENABLED` | Swagger/OpenAPI visibility | `false` outside trusted local/test |
| `APP_RATE_LIMIT_ENABLED` | Process-local rate limits | `true` outside tests |
| `APP_RATE_LIMIT_TRUST_FORWARDED_FOR` | Trust first X-Forwarded-For address | `false` unless trusted edge overwrites it |
| `APP_PRICING_SHIPPING_FLAT` | Shipping charge | `49.00` |
| `APP_PRICING_FREE_SHIPPING_THRESHOLD` | Discounted-subtotal free-shipping limit | `1999.00` |
| `APP_PRICING_TAX_RATE` | Tax-exclusive rate | `0.18` |
| `APP_ORDERS_UNPAID_TTL_MINUTES` | Unpaid order expiry | `30` |

The startup logger emits selected non-secret switches and pricing/expiry values. It does not print database settings, administrator identity, passwords, signing keys, bearer tokens, or request bodies.

## Deploy and verify

1. Deploy the API and static frontend from the intended branch through the normal review workflow.
2. Confirm the API health endpoint responds: `GET /actuator/health`.
3. Run `scripts/smoke.sh` against a local API by default. Set `BASE_URL` only to an explicitly authorized isolated test environment.
4. Sign in with a test customer and owner account. Confirm customer requests to `/api/admin/**` return 403 and unauthenticated requests return 401.
5. Verify the frontend's configured API origin and exact CORS origin.
6. Confirm payment simulation is off except for the isolated staging workflow that explicitly needs it.
7. Check CI against the exact commit SHA. MySQL CI is not a TiDB compatibility result.

## Admin bootstrap

Set the dedicated admin email and password before the first local/staging deployment. Confirm the bootstrap log reports creation. Remove the bootstrap password after account setup. If the email already exists, the default behavior is to leave the account untouched; takeover must be explicitly authorized and enabled for one deployment only.

## Incidents and recovery

- **API fails health:** inspect deploy logs and trace IDs; do not dump environment variables into a ticket.
- **HTTP 429:** respect `Retry-After`; check whether the trusted proxy and rate-limit forwarding configuration match.
- **HTTP 409 at checkout:** inspect item availability, coupon validity, and order state using admin APIs; do not edit rows directly as a first response.
- **Unpaid order remains pending:** check scheduler logs and verify the service is awake; expiration is processed in per-order transactions and startup catch-up runs after service readiness.
- **Simulated payment endpoint returns 404:** confirm `APP_PAYMENTS_SIMULATED_ENABLED` only in the intended staging service.
- **Migration failure:** stop promotion, preserve Flyway history, inspect migration logs, and restore using the approved provider backup/PITR plan. Never repair production schema manually without a reviewed recovery plan.

## Owner-only follow-up

The owner must configure provider backups/PITR, define RPO/RTO, execute a restore rehearsal, verify TiDB locking and migration behavior in isolated staging, inspect report-only CSP violations, and review Render environment values. No live database, Render service, or production deployment was accessed during this code change.
