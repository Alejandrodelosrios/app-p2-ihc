ALTER TABLE care_record
    ADD COLUMN IF NOT EXISTS original_care_date DATE;

UPDATE care_record
SET original_care_date = care_date
WHERE original_care_date IS NULL;

ALTER TABLE care_record
    ALTER COLUMN original_care_date SET NOT NULL;

ALTER TABLE care_record
    ADD COLUMN IF NOT EXISTS series_id UUID;

ALTER TABLE care_record
    ADD COLUMN IF NOT EXISTS change_reason VARCHAR(500);

ALTER TABLE care_history_entry
    ADD COLUMN IF NOT EXISTS change_reason VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_care_record_series_date
    ON care_record (series_id, care_date);
