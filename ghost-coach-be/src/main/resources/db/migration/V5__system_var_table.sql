CREATE TABLE system_var (
    id          BIGSERIAL    PRIMARY KEY,
    group_code  VARCHAR(80)  NOT NULL,
    item_key    VARCHAR(80)  NOT NULL,
    label       VARCHAR(150) NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_system_var_group_item UNIQUE (group_code, item_key)
);

CREATE INDEX idx_system_var_group_active_sort
    ON system_var (group_code, active, sort_order);
