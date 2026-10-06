ALTER TABLE trip.trip_status_changes
    DROP CONSTRAINT ck_trip_status_changes_actor;

ALTER TABLE trip.trip_status_changes
    ALTER COLUMN changed_by SET NOT NULL;
