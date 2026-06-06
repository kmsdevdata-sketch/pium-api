ALTER TABLE skin_analysis_result
    ADD COLUMN analysis_type VARCHAR(32) NOT NULL DEFAULT 'SURVEY' AFTER user_id;
