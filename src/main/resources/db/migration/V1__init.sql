CREATE TABLE users (
    user_id VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_oauth (
    user_oauth_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_user_id VARCHAR(128) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    last_login_at DATETIME(6) NULL,
    CONSTRAINT pk_user_oauth PRIMARY KEY (user_oauth_id),
    CONSTRAINT uk_user_oauth_provider_identity UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_user_oauth_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_oauth_user_id ON user_oauth (user_id);

CREATE TABLE user_profile (
    user_profile_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_profile PRIMARY KEY (user_profile_id),
    CONSTRAINT uk_user_profile_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE skin_analysis_result (
    result_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_skin_analysis_result PRIMARY KEY (result_id),
    CONSTRAINT fk_skin_analysis_result_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_skin_analysis_result_user_id ON skin_analysis_result (user_id);

CREATE TABLE skin_metric_score (
    id BIGINT NOT NULL AUTO_INCREMENT,
    result_id VARCHAR(64) NOT NULL,
    metric VARCHAR(64) NOT NULL,
    score_value INT NOT NULL,
    CONSTRAINT pk_skin_metric_score PRIMARY KEY (id),
    CONSTRAINT fk_skin_metric_score_result_id FOREIGN KEY (result_id) REFERENCES skin_analysis_result (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_skin_metric_score_result_id ON skin_metric_score (result_id);
CREATE INDEX idx_skin_metric_score_metric ON skin_metric_score (metric);

CREATE TABLE skin_analysis_goal (
    result_id VARCHAR(64) NOT NULL,
    goal_code VARCHAR(64) NOT NULL,
    CONSTRAINT fk_skin_analysis_goal_result_id FOREIGN KEY (result_id) REFERENCES skin_analysis_result (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_skin_analysis_goal_result_id ON skin_analysis_goal (result_id);
