CREATE TABLE trip.processed_events (
    event_id      UUID         PRIMARY KEY,
    event_type    VARCHAR(60)  NOT NULL,
    processed_at  TIMESTAMP    NOT NULL
);
