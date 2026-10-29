CREATE TABLE IF NOT EXISTS care_record (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    pet_name VARCHAR(255) NOT NULL,
    care VARCHAR(255),
    animal_type VARCHAR(255),
    care_date DATE,
    created_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

