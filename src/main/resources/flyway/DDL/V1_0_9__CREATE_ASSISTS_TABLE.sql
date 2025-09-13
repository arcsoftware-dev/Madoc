CREATE TABLE IF NOT EXISTS "madoc"."assists" (
    "id" SERIAL PRIMARY KEY,
    "player_id" integer REFERENCES "madoc"."players"("id") NOT NULL,
    "is_primary" boolean NOT NULL
);
