CREATE TABLE notification.email_outbox (
    event_id          UUID         PRIMARY KEY,
    tenant_id         UUID         NOT NULL,
    recipient_email   VARCHAR(150) NOT NULL,
    subject           VARCHAR(160) NOT NULL,
    body              VARCHAR(1000) NOT NULL,
    delivery_status   VARCHAR(30)  NOT NULL,
    delivery_attempts INTEGER      NOT NULL DEFAULT 0,
    last_error        VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL,
    delivered_at      TIMESTAMP
);

CREATE INDEX idx_email_outbox_tenant_status
    ON notification.email_outbox (tenant_id, delivery_status);
