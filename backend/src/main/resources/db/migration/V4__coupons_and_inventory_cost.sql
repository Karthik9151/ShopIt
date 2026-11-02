ALTER TABLE products
    ADD COLUMN cost_price DECIMAL(12,2) NOT NULL DEFAULT 0.00;

CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(240),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    minimum_order_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    maximum_discount DECIMAL(12,2),
    starts_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NOT NULL,
    usage_limit INT,
    usage_count INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_coupon_discount_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT chk_coupon_discount_value CHECK (discount_value > 0),
    INDEX idx_coupon_active_dates (active, starts_at, ends_at)
);

ALTER TABLE customer_orders ADD COLUMN coupon_id BIGINT NULL;
ALTER TABLE customer_orders ADD COLUMN coupon_code VARCHAR(40) NULL;
ALTER TABLE customer_orders ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;
