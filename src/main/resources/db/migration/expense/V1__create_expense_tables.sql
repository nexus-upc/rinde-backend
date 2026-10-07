CREATE TABLE expense.expenses (
    id                  UUID            PRIMARY KEY,
    tenant_id           UUID            NOT NULL,
    trip_id             UUID            NOT NULL,
    driver_id           UUID            NOT NULL,
    category            VARCHAR(30)     NOT NULL,
    amount              DECIMAL(10,2)   NOT NULL,
    currency            CHAR(3)         NOT NULL,
    expense_date        DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    idempotency_key     VARCHAR(64)     NOT NULL,
    observation_reason  VARCHAR(255),
    is_locked           BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    CONSTRAINT uk_expenses_idempotency_key UNIQUE (idempotency_key)
);

CREATE TABLE expense.evidences (
    id                  UUID            PRIMARY KEY,
    tenant_id           UUID            NOT NULL,
    expense_id          UUID            NOT NULL REFERENCES expense.expenses (id) ON DELETE CASCADE,
    image_url           VARCHAR(500)    NOT NULL,
    file_size_bytes     BIGINT          NOT NULL,
    uploaded_at         TIMESTAMP       NOT NULL,
    CONSTRAINT uk_evidences_expense_id UNIQUE (expense_id)
);

CREATE INDEX idx_expenses_tenant_trip ON expense.expenses (tenant_id, trip_id);
CREATE INDEX idx_expenses_trip_status ON expense.expenses (trip_id, status);
CREATE INDEX idx_evidences_expense_id ON expense.evidences (expense_id);
