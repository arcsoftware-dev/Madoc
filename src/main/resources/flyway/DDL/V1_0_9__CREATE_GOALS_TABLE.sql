CREATE TABLE IF NOT EXISTS "madoc"."goals" (
    "id" SERIAL PRIMARY KEY,
    "game_id" integer REFERENCES "madoc"."games"("id") NOT NULL,
    "goal_type" varchar(20) NOT NULL,
    "player_id" integer REFERENCES "madoc"."players"("id") NOT NULL,
    "primary_assist_id" integer REFERENCES "madoc"."assists"("id"),
    "secondary_assist_id" integer REFERENCES "madoc"."assists"("id"),
    "period" int NOT NULL,
    "time" varchar(5) NOT NULL,
    "uploaded_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
