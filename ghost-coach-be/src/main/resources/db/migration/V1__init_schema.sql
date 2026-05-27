CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    sport VARCHAR(30) NOT NULL,
    position VARCHAR(50) NOT NULL,
    experience_level VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE coaching_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_path VARCHAR(500) NOT NULL,
    overall_score NUMERIC(3,1),
    strengths JSONB,
    areas_to_improve JSONB,
    priority_fix TEXT,
    drill_suggestion TEXT,
    confidence_level VARCHAR(10),
    raw_ai_response TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sessions_user_created
    ON coaching_sessions(user_id, created_at DESC);
