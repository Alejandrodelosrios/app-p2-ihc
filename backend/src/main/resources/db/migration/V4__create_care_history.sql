CREATE TABLE IF NOT EXISTS care_history_entry (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    care_record_id UUID NOT NULL,
    pet_name VARCHAR(255) NOT NULL,
    care VARCHAR(255),
    action VARCHAR(32) NOT NULL,
    previous_care_date DATE,
    current_care_date DATE,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);
