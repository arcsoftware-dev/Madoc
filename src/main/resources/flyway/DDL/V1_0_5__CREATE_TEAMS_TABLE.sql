CREATE TABLE IF NOT EXISTS "madoc"."teams" (
    "id" SERIAL PRIMARY KEY,
    "team_name" varchar(20),
    "year" integer
);

-- Insert current data
INSERT INTO "madoc"."teams" ("team_name", "year") VALUES
    ('Avalanche', 2025),
    ('Blackhawks', 2025),
    ('Canucks', 2025),
    ('Leafs', 2025),
    ('Red Wings', 2025),
    ('Golden Knights', 2025);
