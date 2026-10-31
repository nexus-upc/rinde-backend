CREATE TABLE fleet.tenant_plan_limits (
    tenant_id  UUID      PRIMARY KEY,
    plan_id    UUID      NOT NULL,
    unit_limit INTEGER   NOT NULL CHECK (unit_limit > 0),
    updated_at TIMESTAMP NOT NULL
);
