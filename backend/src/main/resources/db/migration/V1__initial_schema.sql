CREATE TABLE users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(120) NOT NULL, email VARCHAR(190) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL, phone VARCHAR(30), role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
 enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
CREATE TABLE categories (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL UNIQUE, slug VARCHAR(120) NOT NULL UNIQUE,
 description VARCHAR(500), active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE products (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, category_id BIGINT, name VARCHAR(180) NOT NULL, description TEXT,
 price DECIMAL(12,2) NOT NULL, stock INT NOT NULL DEFAULT 0, image_url VARCHAR(1000),
 active BOOLEAN NOT NULL DEFAULT TRUE, version BIGINT NOT NULL DEFAULT 0,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_products_category FOREIGN KEY(category_id) REFERENCES categories(id),
 CONSTRAINT chk_products_price CHECK(price >= 0), CONSTRAINT chk_products_stock CHECK(stock >= 0),
 INDEX idx_products_category_active(category_id,active), INDEX idx_products_name(name)
);
CREATE TABLE cart_items (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, product_id BIGINT NOT NULL, quantity INT NOT NULL,
 CONSTRAINT fk_cart_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
 CONSTRAINT fk_cart_product FOREIGN KEY(product_id) REFERENCES products(id),
 CONSTRAINT uq_cart_user_product UNIQUE(user_id,product_id), CONSTRAINT chk_cart_quantity CHECK(quantity > 0)
);
CREATE TABLE wishlist_items (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_wishlist_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
 CONSTRAINT fk_wishlist_product FOREIGN KEY(product_id) REFERENCES products(id),
 CONSTRAINT uq_wishlist_user_product UNIQUE(user_id,product_id)
);
CREATE TABLE customer_orders (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, request_id VARCHAR(80) NOT NULL,
 status VARCHAR(30) NOT NULL, subtotal DECIMAL(12,2) NOT NULL, shipping DECIMAL(12,2) NOT NULL,
 tax DECIMAL(12,2) NOT NULL, total DECIMAL(12,2) NOT NULL, shipping_address VARCHAR(1000) NOT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_orders_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT uq_order_user_request UNIQUE(user_id,request_id), INDEX idx_orders_user_created(user_id,created_at),
 INDEX idx_orders_status(status)
);
CREATE TABLE order_items (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, product_id BIGINT,
 product_name VARCHAR(180) NOT NULL, unit_price DECIMAL(12,2) NOT NULL, quantity INT NOT NULL,
 CONSTRAINT fk_order_items_order FOREIGN KEY(order_id) REFERENCES customer_orders(id) ON DELETE CASCADE,
 CONSTRAINT fk_order_items_product FOREIGN KEY(product_id) REFERENCES products(id),
 CONSTRAINT chk_order_item_quantity CHECK(quantity > 0)
);
CREATE TABLE payments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL UNIQUE, provider VARCHAR(30) NOT NULL,
 status VARCHAR(30) NOT NULL, reference VARCHAR(100) NOT NULL UNIQUE, amount DECIMAL(12,2) NOT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), paid_at TIMESTAMP(6),
 CONSTRAINT fk_payment_order FOREIGN KEY(order_id) REFERENCES customer_orders(id)
);
CREATE TABLE order_status_history (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, old_status VARCHAR(30),
 new_status VARCHAR(30) NOT NULL, changed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_order_history_order FOREIGN KEY(order_id) REFERENCES customer_orders(id) ON DELETE CASCADE,
 INDEX idx_order_history(order_id,changed_at)
);
