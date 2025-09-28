CREATE TABLE IF NOT EXISTS "madoc"."users" (
    "username" varchar(50) PRIMARY KEY,
    "password_hash" varchar(512) NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);