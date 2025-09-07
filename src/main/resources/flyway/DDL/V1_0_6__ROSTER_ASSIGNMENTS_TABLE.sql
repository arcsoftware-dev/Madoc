CREATE TABLE IF NOT EXISTS "madoc"."roster_assignments" (
    "id" SERIAL PRIMARY KEY,
    "team_id" integer REFERENCES "madoc"."teams"("id"),
    "player_id" integer REFERENCES "madoc"."players"("id"),
    "season_year" integer,
    "draft_position" varchar(6),
    "position" varchar(10),
    "is_rookie" boolean DEFAULT false
);
