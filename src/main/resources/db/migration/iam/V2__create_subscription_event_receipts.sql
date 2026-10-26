CREATE TABLE iam.subscription_event_receipts (
    event_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES iam.tenants (id),
    event_type VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_subscription_event_receipts_tenant_time
    ON iam.subscription_event_receipts (tenant_id, occurred_at DESC);
