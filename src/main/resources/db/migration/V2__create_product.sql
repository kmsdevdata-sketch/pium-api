CREATE TABLE product (
    product_id VARCHAR(64) NOT NULL,
    source_url VARCHAR(1024) NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    usage_step VARCHAR(64) NOT NULL,
    price INT NOT NULL,
    image_url VARCHAR(1024) NULL,
    ingredient_text TEXT NULL,
    claims TEXT NULL,
    status VARCHAR(32) NOT NULL,
    admin_memo TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_product_status ON product (status);
CREATE INDEX idx_product_category ON product (category);
CREATE INDEX idx_product_brand_name ON product (brand_name);

CREATE TABLE product_functional_label (
    product_id VARCHAR(64) NOT NULL,
    functional_label VARCHAR(64) NOT NULL,
    CONSTRAINT fk_product_functional_label_product_id FOREIGN KEY (product_id) REFERENCES product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_product_functional_label_product_id ON product_functional_label (product_id);
