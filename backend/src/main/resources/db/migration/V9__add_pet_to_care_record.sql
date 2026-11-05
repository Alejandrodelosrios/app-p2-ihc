ALTER TABLE care_record
    ADD COLUMN pet_id UUID NOT NULL;

ALTER TABLE care_record
    ADD CONSTRAINT fk_care_record_pet
        FOREIGN KEY (pet_id) REFERENCES pet (id);

CREATE INDEX idx_care_record_pet_id ON care_record (pet_id);

ALTER TABLE care_record DROP COLUMN pet_name;
ALTER TABLE care_record DROP COLUMN animal_type;

ALTER TABLE care_history_entry
    ADD COLUMN pet_id UUID NOT NULL;

ALTER TABLE care_history_entry
    ADD CONSTRAINT fk_care_history_pet
        FOREIGN KEY (pet_id) REFERENCES pet (id);

CREATE INDEX idx_care_history_pet_id ON care_history_entry (pet_id);