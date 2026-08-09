CREATE TABLE user_prayer_settings
(
    id UUID PRIMARY KEY,
    user_id UUID  REFERENCES users(id) ON DELETE CASCADE ,
    calculation_method INT NOT NULL DEFAULT 1,
    juristic_method INT NOT NULL DEFAULT 0,
    master_notification BOOLEAN DEFAULT TRUE,
    notification_settings JSONB NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE prayer_logs
(
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    log_date DATE NOT NULL ,
    fajr BOOLEAN DEFAULT FALSE,
    dhuhr BOOLEAN DEFAULT FALSE,
    asr BOOLEAN DEFAULT FALSE,
    maghrib BOOLEAN DEFAULT FALSE,
    isha BOOLEAN DEFAULT FALSE,
    update_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT idx_user_logs_date UNIQUE (user_id, log_date)

);
