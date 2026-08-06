

CREATE TABLE refresh_tokens(
    id UUID PRIMARY KEY NOT NULL ,
    token TEXT NOT NULL ,
    expires_at timestamptz NOT NULL ,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);