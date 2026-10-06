-- Esquema iam: empresas, usuarios y enlaces de acceso (ver iam_database_diagram del Capítulo IV).
CREATE TABLE iam.tenants (
    id          UUID         PRIMARY KEY,
    trade_name  VARCHAR(120) NOT NULL,
    ruc         CHAR(11)     NOT NULL UNIQUE,
    status      VARCHAR(30)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE TABLE iam.users (
    id             UUID         PRIMARY KEY,
    tenant_id      UUID         NOT NULL REFERENCES iam.tenants (id),
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255),
    role           VARCHAR(30)  NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE TABLE iam.access_tokens (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES iam.tenants (id),
    user_id     UUID         NOT NULL REFERENCES iam.users (id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    purpose     VARCHAR(20)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL
);

CREATE INDEX idx_users_tenant_id ON iam.users (tenant_id);
CREATE INDEX idx_access_tokens_user_id ON iam.access_tokens (user_id);
