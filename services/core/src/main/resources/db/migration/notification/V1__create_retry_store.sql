CREATE TABLE notification.retry_store (
    event_id            UUID         PRIMARY KEY,
    tenant_id           UUID         NOT NULL,
    trip_id             UUID         NOT NULL,
    recipient_driver_id UUID         NOT NULL,
    trip_code           VARCHAR(20)  NOT NULL,
    destination         VARCHAR(120) NOT NULL,
    departure_date      DATE         NOT NULL,
    message             VARCHAR(500) NOT NULL,
    delivery_status     VARCHAR(20)  NOT NULL,
    retry_count         INTEGER      NOT NULL DEFAULT 0,
    last_error          VARCHAR(500),
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    delivered_at        TIMESTAMP
);

CREATE INDEX idx_notification_retry_pending
    ON notification.retry_store (delivery_status, updated_at);
