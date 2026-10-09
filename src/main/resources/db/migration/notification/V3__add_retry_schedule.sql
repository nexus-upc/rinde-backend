-- Próximo intento de entrega y estado FAILED para los avisos y correos que agotaron sus reintentos.
ALTER TABLE notification.retry_store ADD COLUMN next_attempt_at TIMESTAMP;
ALTER TABLE notification.email_outbox ADD COLUMN next_attempt_at TIMESTAMP;

-- Los pendientes previos no tenían calendario: los agotados pasan a FAILED y el resto se reintenta en el siguiente sondeo.
UPDATE notification.retry_store SET delivery_status = 'FAILED'
    WHERE delivery_status = 'PENDING' AND retry_count >= 5;
UPDATE notification.retry_store SET next_attempt_at = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
    WHERE delivery_status = 'PENDING';
UPDATE notification.email_outbox SET delivery_status = 'FAILED'
    WHERE delivery_status = 'PENDING' AND delivery_attempts >= 6;
UPDATE notification.email_outbox SET next_attempt_at = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
    WHERE delivery_status = 'PENDING';

CREATE INDEX idx_notification_retry_due
    ON notification.retry_store (delivery_status, next_attempt_at);
CREATE INDEX idx_email_outbox_due
    ON notification.email_outbox (delivery_status, next_attempt_at);
