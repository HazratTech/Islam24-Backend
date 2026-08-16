-- 1. Khatam Quran Plans Table

CREATE TABLE khatam_plans
(
    id                           UUID PRIMARY KEY,
    user_id                      UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title                        VARCHAR(255) NOT NULL DEFAULT 'Khatam Quran',
    start_date_timestamp         BIGINT       NOT NULL,
    target_end_date_timestamp    BIGINT       NOT NULL,
    last_read_surah_number       INT          NOT NULL DEFAULT 1,
    last_read_ayah_number        INT          NOT NULL DEFAULT 1,
    last_read_global_ayah_number INT          NOT NULL DEFAULT 1,
    completed_ayahs_count        INT          NOT NULL DEFAULT 0,
    status                       VARCHAR(50)  NOT NULL DEFAULT 'IN_PROGRESS',
    completed_timestamp          BIGINT,
    is_deleted                   BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_timestamp            BIGINT       NOT NULL,
    created_at                   TIMESTAMPTZ           DEFAULT now()
);

CREATE INDEX idx_khatam_plans_user_id ON khatam_plans (user_id);
CREATE INDEX idx_khatam_plans_user_updated ON khatam_plans (user_id, updated_timestamp);

-- 2.Quran Bookmark Table
CREATE TABLE quran_bookmarks
(
    id                 UUID PRIMARY KEY,
    user_id            UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    surah_number       INT     NOT NULL,
    ayah_number        INT     NOT NULL,
    global_ayah_number INT     NOT NULL,
    is_deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at         BIGINT  NOT NULL,
    created_at         TIMESTAMPTZ      DEFAULT now()
);
CREATE INDEX idx_quran_bookmarks_user_id ON quran_bookmarks (user_id);
CREATE INDEX idx_quran_bookmarks_user_updated ON quran_bookmarks (user_id, updated_at);

-- 3. Recent Surah Reading Position Table (Unique per user and surah)
CREATE TABLE recent_surahs
(
    id             UUID PRIMARY KEY,
    user_id        UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    surah_number   INT          NOT NULL,
    surah_name     VARCHAR(100) NOT NULL,
    ayah_number    INT          NOT NULL,
    formatted_date VARCHAR(50)  NOT NULL,
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    timestamp      BIGINT       NOT NULL,
    CONSTRAINT uq_user_recent_surah UNIQUE (user_id, surah_number)
);
CREATE INDEX idx_recent_surahs_user_id ON recent_surahs (user_id);
CREATE INDEX idx_recent_surahs_user_timestamp ON recent_surahs (user_id, timestamp);