CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL UNIQUE,
    email VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    balance DECIMAL DEFAULT 0.0,
    created_date DATE NOT NULL,
    confirmation_code VARCHAR
);