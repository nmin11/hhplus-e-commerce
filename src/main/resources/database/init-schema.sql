-- 전체 테이블들에 대한 CREATE TABLE 쿼리문 작성

-- JPA 에서 생성해주는 걸 사용하기 보다는, 직접 테이블을 생성해서 나름의 형상 관리를 하려는 목적

-- FK는 데드락 이슈를 고려하여 설정하지 않고, 대신 논리적 FK 개념으로 INDEX 사용
    -- 추후 FK 사용 가능성을 고려해 FK 설정 관련 쿼리를 주석 처리

CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    amount INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_balance_customer_id (customer_id)
    -- FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE balance_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL, -- 'CHARGE' || 'USE'
    change_amount INT NOT NULL,
    total_amount INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_balance_history_customer_id (customer_id)
    -- FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    base_price INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE product_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    option_name VARCHAR(50) NOT NULL,
    extra_price INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_product_option_product_id (product_id),
    UNIQUE KEY uq_product_option_product_id_option_name (product_id, option_name) -- 동일 상품 내 같은 옵션 생성 방지
    -- FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_stock_product_option_id (product_option_id)
    -- FOREIGN KEY (product_option_id) REFERENCES product_option(id)
);

CREATE TABLE statistic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sales_count INT NOT NULL,
    sold_at TIMESTAMP NOT NULL,

    INDEX idx_statistic_product_id (product_id),
    INDEX idx_statistic_sold_at (sold_at)
    -- FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    discount_type VARCHAR(20) NOT NULL, -- 'FIXED' || 'RATE'
    discount_amount INT NOT NULL,
    current_quantity INT NOT NULL,
    total_quantity INT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uq_coupon_name (name)
);

CREATE TABLE customer_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'ISSUED' || 'USED' || 'EXPIRED'
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uq_customer_coupon_customer_id_coupon_id (customer_id, coupon_id),
    INDEX idx_customer_coupon_customer_id (customer_id),
    INDEX idx_customer_coupon_coupon_id (coupon_id)
    -- FOREIGN KEY (customer_id) REFERENCES customer(id),
    -- FOREIGN KEY (coupon_id) REFERENCES coupon(id)
);

CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total_price INT NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'PENDING' || 'PAID' || 'CANCELLED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_order_customer_id (customer_id)
    -- FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    subtotal_price INT NOT NULL,

    INDEX idx_order_item_order_id (order_id),
    INDEX idx_order_item_product_option_id (product_option_id)
    -- FOREIGN KEY (order_id) REFERENCES `order`(id),
    -- FOREIGN KEY (product_option_id) REFERENCES product_option(id)
);

CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    coupon_id BIGINT DEFAULT NULL,
    original_price INT NOT NULL,
    discount_amount INT NOT NULL,
    discounted_price INT NOT NULL,
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_payment_order_id (order_id),
    INDEX idx_payment_customer_id (customer_id),
    INDEX idx_payment_coupon_id (coupon_id)
    -- FOREIGN KEY (order_id) REFERENCES `order`(id),
    -- FOREIGN KEY (customer_id) REFERENCES customer(id),
    -- FOREIGN KEY (coupon_id) REFERENCES coupon(id)
);
