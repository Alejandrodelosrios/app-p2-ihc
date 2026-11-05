CREATE TABLE pet (
    id UUID PRIMARY KEY,

    owner_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    species VARCHAR(20) NOT NULL,

    breed VARCHAR(100),

    birth_date DATE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    deleted_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_pet_owner
        FOREIGN KEY (owner_id)
        REFERENCES app_user(id),

    CONSTRAINT chk_pet_species
        CHECK (
            species IN (
                'DOG',
                'CAT',
                'BIRD',
                'RABBIT',
                'RODENT',
                'REPTILE',
                'FISH',
                'OTHER'
            )
        )
);

CREATE INDEX idx_pet_owner_id
    ON pet(owner_id);

CREATE INDEX idx_pet_owner_active
    ON pet(owner_id, created_at DESC)
    WHERE deleted_at IS NULL;