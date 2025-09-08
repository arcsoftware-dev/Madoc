CREATE TABLE IF NOT EXISTS "madoc"."roster_uploads" (
    "id" SERIAL PRIMARY KEY,
    "year" integer UNIQUE,
    "file_name" varchar(255) UNIQUE,
    "file_content" BYTEA,
    "uploaded_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
