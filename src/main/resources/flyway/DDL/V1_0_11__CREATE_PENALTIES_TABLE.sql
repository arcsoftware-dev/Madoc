CREATE TABLE IF NOT EXISTS "madoc"."penalties" (
    "id" SERIAL PRIMARY KEY,
    "game_id" integer REFERENCES "madoc"."games"("id") NOT NULL,
    "player_id" integer REFERENCES "madoc"."players"("id") NOT NULL,
    "infraction" varchar(50) NOT NULL,
    "minutes" int NOT NULL,
    "period" int NOT NULL,
    "time" varchar(5) NOT NULL,
    "uploaded_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
