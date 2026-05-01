CREATE TABLE IF NOT EXISTS items (
   id       SERIAL PRIMARY KEY,
   name     VARCHAR(255) NOT NULL,
   done     BOOLEAN DEFAULT FALSE,
   quantity INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS allowed_users (
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255),
    active     BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS otp_tokens (
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    otp        VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked    BOOLEAN DEFAULT FALSE
);