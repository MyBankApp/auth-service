CREATE TABLE IF NOT EXISTS token (
    id SERIAL PRIMARY KEY,
    access_token VARCHAR NOT NULL UNIQUE,
    refresh_token VARCHAR NOT NULL UNIQUE,
    access_expiration TIMESTAMP(6) NOT NULL,
    refresh_expiration TIMESTAMP(6) NOT NULL,
    user_id SERIAL NOT NULL,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);