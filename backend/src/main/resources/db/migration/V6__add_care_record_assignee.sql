ALTER TABLE care_record
 ADD COLUMN IF NOT EXISTS assigned_to_user_id UUID;

ALTER TABLE care_record
    ADD CONSTRAINT fk_care_record_assigned_to
        FOREIGN KEY (assigned_to_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_care_record_assignee ON care_record (assigned_to_user_id);