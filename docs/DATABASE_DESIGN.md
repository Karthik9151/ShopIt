# Database design

Flyway migrations are under backend/src/main/resources/db/migration.

| Table | Purpose | Relationships |
|---|---|---|
| users | Customer/admin identity and BCrypt hash | Owns cart, wishlist and orders |
| categories | Product taxonomy | One-to-many products |
| products | Current catalogue, stock and price | Optional category |
| cart_items | Persistent customer cart | Unique user/product pair |
| wishlist_items | Saved products | Unique user/product pair |
| customer_orders | Order header and immutable totals | Belongs to user; request ID unique per user |
| order_items | Immutable product name and unit-price snapshot | Belongs to order |
| payments | Simulated/provider transaction metadata | One payment per order |
| order_status_history | Status audit schema | Belongs to order |

Money uses DECIMAL(12,2) and Java BigDecimal. Entity relationships use lazy associations; DTO mapping avoids exposing entities. Historical line snapshots remain valid if a product is renamed or repriced.
