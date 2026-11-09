-- 미용 제품 판매 관리 스키마
-- H2와 MariaDB 모두에서 도는 표준 문법만 씁니다(NFR9). 금액은 원 단위 BIGINT입니다(NFR7).
-- 앱이 시작할 때마다 실행되므로 모든 문장은 IF NOT EXISTS로 둡니다.

-- Auth: 사용자 계정 (FR1.1, FR1.2, FR1.3)
CREATE TABLE IF NOT EXISTS user_account (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    login_id      VARCHAR(50)  NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      DEFAULT TRUE NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uk_user_account_login_id UNIQUE (login_id)
);

-- Product: 분류 (FR2.3)
CREATE TABLE IF NOT EXISTS category (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_category_name UNIQUE (name)
);

-- Product: 상품 (FR2.1, FR2.5)
CREATE TABLE IF NOT EXISTS product (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    category_id BIGINT       NOT NULL,
    brand       VARCHAR(100),
    base_price  BIGINT       NOT NULL,
    base_cost   BIGINT       NOT NULL,
    active      BOOLEAN      DEFAULT TRUE NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT ck_product_base_price CHECK (base_price >= 0),
    CONSTRAINT ck_product_base_cost CHECK (base_cost >= 0)
);

CREATE INDEX IF NOT EXISTS ix_product_category_id ON product (category_id);

-- Product: 옵션과 옵션별 현재 재고 (FR2.2, ADR-004)
CREATE TABLE IF NOT EXISTS product_option (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_id    BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    barcode       VARCHAR(50),
    price         BIGINT       NOT NULL,
    cost          BIGINT       NOT NULL,
    min_stock     INT          DEFAULT 0 NOT NULL,
    current_stock INT          DEFAULT 0 NOT NULL,
    active        BOOLEAN      DEFAULT TRUE NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT fk_product_option_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT uk_product_option_name UNIQUE (product_id, name),
    CONSTRAINT uk_product_option_barcode UNIQUE (barcode),
    CONSTRAINT ck_product_option_price CHECK (price >= 0),
    CONSTRAINT ck_product_option_cost CHECK (cost >= 0),
    CONSTRAINT ck_product_option_min_stock CHECK (min_stock >= 0),
    CONSTRAINT ck_product_option_current_stock CHECK (current_stock >= 0)
);
