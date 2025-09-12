CREATE TABLE IF NOT EXISTS "madoc"."games" (
    "id" SERIAL PRIMARY KEY,
    "home_team" integer REFERENCES "madoc"."teams"("id") NOT NULL,
    "away_team" integer REFERENCES "madoc"."teams"("id") NOT NULL,
    "year" int NOT NULL,
    "season_type" varchar(16) NOT NULL,
    "venue" varchar(100),
    "game_time" TIMESTAMP NOT NULL,
    "referee_name_one" varchar(100),
    "referee_name_two" varchar(100),
    "referee_name_three" varchar(100),
    "referee_notes" text[],
    "is_finalized" boolean DEFAULT false,
    "finalized_at" TIMESTAMP,
    CONSTRAINT finalized_time_not_null_if_finalized
        CHECK (NOT is_finalized OR finalized_at IS NOT NULL)
);
