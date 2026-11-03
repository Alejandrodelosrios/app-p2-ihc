ALTER TABLE care_record
ADD COLUMN recurrence_interval_days INTEGER;

ALTER TABLE care_record
ADD CONSTRAINT chk_care_record_recurrence_interval
CHECK (
    recurrence_interval_days IS NULL
    OR recurrence_interval_days >= 1
);