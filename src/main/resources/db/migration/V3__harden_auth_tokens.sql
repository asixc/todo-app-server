-- Existing refresh tokens and OTPs are invalidated before changing their storage format.
DELETE FROM refresh_tokens;
DELETE FROM otp_tokens;

-- Keep legacy columns nullable so the previous image can still start after rollback.
ALTER TABLE refresh_tokens ALTER COLUMN token DROP NOT NULL;
ALTER TABLE refresh_tokens ADD COLUMN token_hash VARCHAR(64) UNIQUE;
ALTER TABLE refresh_tokens ADD COLUMN family_id VARCHAR(36);
ALTER TABLE refresh_tokens ADD COLUMN family_expires_at TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN revoked_at TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN replaced_by_hash VARCHAR(64);

CREATE INDEX refresh_tokens_family_id_idx ON refresh_tokens (family_id);

ALTER TABLE otp_tokens ALTER COLUMN otp DROP NOT NULL;
ALTER TABLE otp_tokens ADD COLUMN otp_hash VARCHAR(100);
ALTER TABLE otp_tokens ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0;

CREATE INDEX otp_tokens_email_expires_idx ON otp_tokens (email, expires_at);
