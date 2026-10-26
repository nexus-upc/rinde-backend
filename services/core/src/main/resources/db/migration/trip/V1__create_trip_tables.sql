CREATE TABLE trip.trips (
    id                          UUID         PRIMARY KEY,
    tenant_id                   UUID         NOT NULL,
    code                        VARCHAR(20)  NOT NULL,
    origin                      VARCHAR(120) NOT NULL,
    destination                 VARCHAR(120) NOT NULL,
    cargo_description           VARCHAR(200) NOT NULL,
    cargo_weight_kg             DECIMAL(10,2),
    departure_date              DATE         NOT NULL,
    status                      VARCHAR(20)  NOT NULL,
    vehicle_id                  UUID,
    driver_id                   UUID,
    assigned_at                 TIMESTAMP,
    maintenance_confirmed_by    UUID,
    started_at                  TIMESTAMP,
    finished_at                 TIMESTAMP,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_trips_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE trip.trip_status_changes (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL,
    trip_id     UUID         NOT NULL REFERENCES trip.trips (id),
    status      VARCHAR(20)  NOT NULL,
    changed_at  TIMESTAMP    NOT NULL,
    changed_by  UUID,
    CONSTRAINT ck_trip_status_changes_actor
        CHECK (status = 'SETTLED' OR changed_by IS NOT NULL)
);

CREATE INDEX idx_trips_tenant_status ON trip.trips (tenant_id, status);
CREATE INDEX idx_trips_tenant_driver ON trip.trips (tenant_id, driver_id);
CREATE INDEX idx_trip_status_changes_trip_id ON trip.trip_status_changes (trip_id);
