CREATE TABLE fleet.vehicles (
    id                  UUID         PRIMARY KEY,
    tenant_id           UUID         NOT NULL,
    plate_number        VARCHAR(10)  NOT NULL,
    brand               VARCHAR(50)  NOT NULL,
    model               VARCHAR(50)  NOT NULL,
    model_year          INTEGER      NOT NULL,
    payload_capacity_kg DECIMAL(10,2) NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT uk_vehicles_tenant_plate UNIQUE (tenant_id, plate_number)
);

CREATE TABLE fleet.drivers (
    id                      UUID         PRIMARY KEY,
    tenant_id               UUID         NOT NULL,
    user_id                 UUID,
    full_name               VARCHAR(120) NOT NULL,
    document_type           VARCHAR(10)  NOT NULL,
    document_number         VARCHAR(20)  NOT NULL,
    license_number          VARCHAR(20)  NOT NULL,
    license_category        VARCHAR(10)  NOT NULL,
    license_expiration_date DATE         NOT NULL,
    status                  VARCHAR(20)  NOT NULL,
    created_at              TIMESTAMP    NOT NULL,
    updated_at              TIMESTAMP    NOT NULL,
    CONSTRAINT uk_drivers_tenant_document UNIQUE (tenant_id, document_number),
    CONSTRAINT uk_drivers_tenant_license UNIQUE (tenant_id, license_number)
);

CREATE TABLE fleet.maintenances (
    id                    UUID         PRIMARY KEY,
    tenant_id             UUID         NOT NULL,
    vehicle_id            UUID         NOT NULL REFERENCES fleet.vehicles (id),
    maintenance_type      VARCHAR(50)  NOT NULL,
    execution_date        DATE         NOT NULL,
    mileage               INTEGER,
    cost                  DECIMAL(10,2) NOT NULL,
    next_maintenance_date DATE         NOT NULL,
    notes                 VARCHAR(500),
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
);

CREATE INDEX idx_vehicles_tenant_status ON fleet.vehicles (tenant_id, status);
CREATE INDEX idx_drivers_tenant_status ON fleet.drivers (tenant_id, status);
CREATE INDEX idx_drivers_tenant_user ON fleet.drivers (tenant_id, user_id);
CREATE INDEX idx_maintenances_vehicle ON fleet.maintenances (vehicle_id);
CREATE INDEX idx_maintenances_tenant_vehicle ON fleet.maintenances (tenant_id, vehicle_id);
