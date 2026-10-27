CREATE TABLE iam.demo_mail_messages (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    tenant_id UUID NOT NULL,
    recipient VARCHAR(150) NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    company_name VARCHAR(120) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    encrypted_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP
);
CREATE INDEX idx_demo_mail_created ON iam.demo_mail_messages (created_at DESC, id);
CREATE INDEX idx_demo_mail_recipient ON iam.demo_mail_messages (lower(recipient));
