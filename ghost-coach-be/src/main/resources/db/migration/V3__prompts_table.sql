CREATE TABLE prompts (
    id              BIGSERIAL PRIMARY KEY,
    prompt_key      VARCHAR(50)  NOT NULL UNIQUE,
    description     VARCHAR(255) NOT NULL,
    template        TEXT         NOT NULL,
    variables       JSONB        NOT NULL DEFAULT '[]'::jsonb,
    model_config    JSONB        NOT NULL DEFAULT '{}'::jsonb,
    response_format VARCHAR(20)  NOT NULL DEFAULT 'TEXT',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_prompts_key ON prompts(prompt_key);
