DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_refresh_tokens_token'
    ) THEN
        ALTER TABLE refresh_tokens ADD CONSTRAINT uq_refresh_tokens_token UNIQUE (token);
    END IF;
END $$;