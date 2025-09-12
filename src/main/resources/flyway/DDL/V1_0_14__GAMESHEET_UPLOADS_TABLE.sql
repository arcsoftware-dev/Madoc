CREATE TABLE IF NOT EXISTS "madoc"."gamesheet_uploads" (
    "id" SERIAL PRIMARY KEY,
    "game_id" integer REFERENCES "madoc"."games"("id"),
    "file_name" varchar(255) UNIQUE,
    "file_content" BYTEA,
    "uploaded_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
