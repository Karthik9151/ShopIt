# TiDB staging compatibility verification

**Status:** Owner action required. This guide does not claim the deployed TiDB database was accessed or verified.

## Why a staging check is still required

ShopIt CI runs Maven integration verification against MySQL 8.4. The documented deployment uses TiDB Cloud Starter, which speaks the MySQL protocol but is not the same server distribution. A successful MySQL CI run does not prove TiDB compatibility for every migration, SQL expression, lock, transaction, or index operation.

The repository baseline inspected for this package does not contain `scripts/verify-tidb.sh`. Do not assume that script exists or run a similarly named command. The owner should first confirm the exact intended verification command/script in a network-enabled repository checkout.

## Preconditions

1. Use a dedicated staging TiDB cluster/database, not production.
2. Confirm a current provider-managed backup/PITR recovery point and an approved restore operator before any schema-writing check.
3. Use a staging-only least-privilege database identity and TLS with server identity verification.
4. Supply connection values through the owner's secret manager or an untracked local environment file. Never paste secrets in a PR, chat, issue, shell transcript, or test report.
5. Confirm the deployment commit and its Flyway migration set. Do not edit or re-run old migration scripts manually.

## Verification matrix

| Area | Owner verification | Evidence to retain (redacted) |
|---|---|---|
| Connectivity and TLS | Connect using the app JDBC URL with TLS identity verification enabled. Confirm the expected TiDB cluster/database identity. | Cluster alias, timestamp, TLS mode, client version; no hostname credentials or password |
| Migration history | Query `flyway_schema_history`; verify versions, checksums, success flags and ordering against the approved commit. Confirm no failed or partial migration. | Migration version, checksum, success flag, installed timestamp |
| Schema validation | Start the application against staging with Hibernate `ddl-auto=validate`; confirm startup/readiness and no schema drift. | App commit, schema version, health/readiness status |
| Transaction behavior | In disposable test data, create and cancel an unpaid order; confirm stock restoration and actor timeline. | Test order surrogate IDs and resulting statuses |
| Locking and concurrency | Exercise coupon usage cap and product-stock concurrency using controlled disposable rows; confirm no over-redemption/oversell. | Test case, concurrent request count, final stock and coupon usage count |
| Index/constraint behavior | Exercise uniqueness, foreign keys, non-null constraints and query plans used by admin pagination. | Migration table names, constraint/index names, query outcome |
| Cleanup | Remove only test fixtures and verify no unrelated staging rows were changed. | Fixture IDs and cleanup result |

## Safe execution

- Use disposable test customers/products/coupons and uniquely generated request IDs.
- Never run a full destructive reset against a shared staging schema.
- Avoid manual DDL or editing `flyway_schema_history` to make a migration appear successful.
- Do not test against live customer orders or real-money payment systems; ShopIt's payment flow is simulated.
- Stop on a failed migration, schema validation error, lock timeout, unexpected stock/coupon count, or data integrity issue. Preserve redacted logs and escalate before retrying.

## Flyway immutability

Flyway V1–V4 are immutable. No checksum repair, manual history rewrite, or edit of an already-applied migration is allowed as a compatibility shortcut. Any schema correction must be a new forward migration V5 or later with a tested rollback/forward-recovery procedure. This package adds no migration.

## Owner completion record

Record:
- staging cluster alias and UTC timestamp;
- application commit SHA;
- verified Flyway versions/checksums;
- MySQL driver/JDK versions;
- results for all verification matrix rows;
- relevant redacted log excerpt and any deviations;
- backup identifier and cleanup confirmation.

**Not performed in this run:** no TiDB connection, staging deployment, database query, migration, lock/concurrency test, backup/restore, or `scripts/verify-tidb.sh` execution.
