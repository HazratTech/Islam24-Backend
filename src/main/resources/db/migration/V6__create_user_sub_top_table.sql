CREATE TABLE user_subscriptions
(
    id UUID PRIMARY KEY ,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL UNIQUE ,
    original_app_user_id TEXT,
    product_id VARCHAR(128) NOT NULL ,
    entitlement_id VARCHAR(128) NOT NULL ,
    status VARCHAR(50) NOT NULL, -- 'ACTIVE', 'EXPIRED' , 'CANCELED'
    store VARCHAR(50) NOT NULL, -- 'PLAY STORE', 'APP_STORE'
    environment VARCHAR(20) NOT NULL , -- 'SANDBOX', 'PRODUCTION'
    purchased_at TIMESTAMPTZ NOT NULL ,
    expires_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE user_tips
(
    id UUID PRIMARY KEY ,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(128) NOT NULL,
    store VARCHAR(50) NOT NULL,
    purchased_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE webhook_event_logs(
    event_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
)


