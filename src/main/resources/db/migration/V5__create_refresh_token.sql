CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    token      TEXT UNIQUE                                  NOT NULL,
    expires_at TIMESTAMPTZ                                  NOT NULL,
    revoked    BOOLEAN                                      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ                                  NOT NULL,
    updated_at TIMESTAMPTZ                                  NOT NULL
);

CREATE INDEX idx_refresh_token_user_id ON refresh_tokens (user_id);