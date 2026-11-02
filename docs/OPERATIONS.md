# Operations, Backup and Recovery Runbook

**Scope:** ShopIt application and its MySQL-compatible data store. This document defines owner-operated procedures; it does not assert that a backup, restore, failover, or live service change has been performed.

## Operational boundaries

- The application is deployed as a React static site and a Spring Boot API. The database is an external MySQL-compatible service.
- Payment is simulated only. A simulated payment is not a real financial settlement.
- The login/registration/commerce rate limiter is process-local. Counters reset on restart and are not a substitute for edge/WAF controls.
- Render and database-provider dashboards, credentials, and production data are owner-controlled. This runbook does not create or modify their settings.
- Never put database URLs containing credentials, access tokens, JWT keys, passwords, customer data, or raw backup data in Git, issue comments, screenshots, or ordinary logs.

## Before a production-like release

1. Review the PR diff and require successful backend Maven/MySQL verification, frontend tests/build, and container build where the workflow includes it.
2. Confirm the deployment points to the intended commit and that database migration history is understood before release.
3. Confirm the configured database endpoint requires TLS and validates the server identity.
4. Confirm the owner-controlled secret store contains a strong JWT signing key, DB credentials, and administrator credentials. Do not print them during checks.
5. Confirm the administrator account and recovery contact are available to the owner. Do not use account takeover unless the owner has verified account ownership and approved a one-deployment use.
6. Keep simulated payments disabled outside the explicitly designated staging demonstration environment.

## Backup policy

The owner must select and configure database-provider backups or point-in-time recovery (PITR). The repository does not prove that provider backups are enabled, that a backup exists, or that a restore point is usable.

Recommended minimum policy to approve:
- Automated daily backup/PITR with provider-supported retention of at least 7 days; retain a separate weekly recovery point for at least 4 weeks where the provider and plan support it.
- Encrypt backups at rest and in transit; restrict restore/download permission to named operators.
- Record backup completion time, source database/cluster identity, backup identifier, schema migration version, and integrity checksum. Never record connection secrets.
- Perform a restore rehearsal at least quarterly into an isolated database. Do not test restoration by overwriting the live database.
- Define owner-approved recovery point objective (RPO) and recovery time objective (RTO) before calling the service production-ready.

For a MySQL 8-compatible database where the owner has verified that the provider supports logical dumps, a backup can be made from an operator-managed client configuration file outside the repository (for example, a file with mode 0600):

```sh
umask 077
mkdir -p "$BACKUP_DIR"
mysqldump --defaults-extra-file="$MYSQL_CLIENT_CNF" \
  --single-transaction --routines --triggers --hex-blob \
  --databases shopit > "$BACKUP_DIR/shopit-$(date -u +%Y%m%dT%H%M%SZ).sql"
sha256sum "$BACKUP_DIR"/shopit-*.sql > "$BACKUP_DIR/SHA256SUMS"
```

This is an example for compatible MySQL clients, not a claim that every TiDB Cloud tier supports the same dump/restore options. Use the database provider's supported backup/PITR feature and consult its compatibility requirements. Keep backup files and client configuration outside the Git working tree.

## Isolated restore rehearsal

1. Select a backup identifier and create a new, isolated restore target using the database provider's supported workflow.
2. Restore only into that isolated target. For a provider-confirmed MySQL-compatible logical dump, the equivalent import is:

   ```sh
   mysql --defaults-extra-file="$MYSQL_CLIENT_CNF" "$RESTORE_DATABASE" < "$BACKUP_FILE"
   ```

3. Compare the restored schema and row counts against the recovery record. Confirm all expected tables exist.
4. Inspect `flyway_schema_history` and confirm recorded migration versions, checksums, success flags, and ordering match the approved release. Do not edit V1–V4. Future schema changes must be additive V5 or later.
5. Run documented read-only application smoke checks against the isolated target. Verify login, product catalogue reads, customer-owned order history, and admin-only endpoints using designated test identities.
6. Record restore duration, backup identifier, migration version, validation results, gaps, and cleanup confirmation. Redact connection details and personal data.
7. Destroy the isolated target only after validation and evidence capture. Do not clean up or alter the production database as part of a rehearsal.

## Incident response

### API unavailable or repeated 5xx responses

- Confirm whether the frontend is reachable independently of the API.
- Check the API health endpoint and owner-visible service events/logs. Redact tokens, email addresses, DB URLs, and request bodies before sharing excerpts.
- Determine whether the service is sleeping, restarting, failing health checks, or unable to connect to the database.
- Do not repeatedly retry state-changing checkout/payment requests. The client retry policy is limited to eligible GET requests; mutating requests must be inspected by request ID/order ID.
- Escalate infrastructure failures to the service owner/provider. No restart, deploy, or environment change is performed by this runbook automatically.

### Suspected account compromise or abusive traffic

- Preserve timestamps, request IDs, affected account identifiers in the restricted incident record, and relevant redacted logs.
- Have the owner disable the affected customer/admin account through the authorized control path or provider process.
- Rotate compromised credentials through the owner-controlled secret manager. Do not paste replacement secrets into GitHub or chat.
- Review rate-limit behavior. The current token bucket is per process; restarting clears it, and multiple instances do not share counters.
- Review administrator bootstrap and account takeover settings. Keep takeover disabled by default.

### Order/payment inconsistency

- Treat payment status as authoritative only when a successful payment record exists. The currently supported payment provider is simulated, not a real payment processor.
- Reconcile the order timeline, payment row, and stock/coupon restoration state before manual correction.
- Paid-order cancellation/refund handling is intentionally unsupported; do not issue a database-only refund or mark a paid order cancelled to bypass the application's 409 guard.
- Escalate any real-money movement to the future payment provider's verified settlement/reconciliation process; no live payment integration is currently claimed.

### Suspected data exposure

- Restrict further access through the owner/provider process and preserve relevant evidence.
- Rotate affected credentials, assess the scope of accessed records, and follow applicable notification/legal obligations.
- Do not copy customer records or raw backups into issue trackers or routine logs.

## Recovery and rollback principles

- Prefer application rollback to a previously verified commit when the failure is code-only and no incompatible schema change was applied.
- Database migrations are not rolled back by rewriting old Flyway scripts. V1–V4 are immutable; any future schema correction must be a new forward migration (V5 or later) with a tested forward-recovery path.
- For a data-corruption incident, stop writes using the owner/provider procedure, preserve current state for forensics, restore to an isolated target, validate it, and obtain explicit owner authorization before any production cutover.
- Reconcile orders, payments, inventory, coupon usage, and status history after recovery. Check for duplicated or lost checkout operations using request IDs and order IDs.
- Confirm application readiness, schema version, representative read paths, and owner access before reopening traffic.

## Owner action checklist

- [ ] Select and configure provider-managed backups/PITR and approved retention.
- [ ] Record approved RPO/RTO and named recovery operators.
- [ ] Complete an isolated restore rehearsal and retain redacted evidence.
- [ ] Confirm database TLS/identity verification and least-privilege credentials.
- [ ] Confirm an owner-visible alert path for API health, database errors, failed backups, and abnormal authentication traffic.
- [ ] Review whether the process-local rate limiter is adequate for the deployed instance count.
- [ ] Document who may authorize emergency production cutover and data restoration.

**Verification status:** Runbook authored; no live backup, restore, Render action, TiDB action, or production database inspection was performed in this work package.
