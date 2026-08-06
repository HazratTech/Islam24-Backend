
CREATE TABLE users
(
    id UUID PRIMARY KEY,
    google_id VARCHAR(128) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE  NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);