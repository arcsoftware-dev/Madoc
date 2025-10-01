CREATE TABLE IF NOT EXISTS "madoc"."gamesheets" (
    "game_id" integer PRIMARY KEY REFERENCES "madoc"."games" ("id"),
    "json_string" jsonb NOT NULL
);