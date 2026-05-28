CREATE TABLE product_profile (
                                 product_id VARCHAR(64) NOT NULL,
                                 profile_json LONGTEXT NOT NULL,
                                 created_at DATETIME(6) NOT NULL,
                                 updated_at DATETIME(6) NOT NULL,
                                 CONSTRAINT pk_product_profile PRIMARY KEY (product_id),
                                 CONSTRAINT fk_product_profile_product_id FOREIGN KEY (product_id) REFERENCES product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;