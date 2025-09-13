CREATE TABLE IF NOT EXISTS "madoc"."attendance" (
    "id" SERIAL PRIMARY KEY,
    "game_id" integer REFERENCES "madoc"."games"("id") NOT NULL,
    "player_id" integer REFERENCES "madoc"."players"("id") NOT NULL,
    "jersey_number" integer NOT NULL,
    "team_id" integer REFERENCES "madoc"."teams"("id") NOT NULL,
    "attended" boolean NOT NULL
);

ALTER TABLE "madoc"."attendance"
    ADD CONSTRAINT unique_player_game_constraint UNIQUE (game_id, player_id);