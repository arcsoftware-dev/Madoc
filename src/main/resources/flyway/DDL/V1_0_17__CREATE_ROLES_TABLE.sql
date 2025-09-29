CREATE TABLE IF NOT EXISTS "madoc"."roles" (
    "id" SERIAL PRIMARY KEY,
    "role" varchar(20) UNIQUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Populate Basic Roles
INSERT INTO madoc.roles (role) VALUES
    ('ADMIN'),
    ('LEAGUE_STAFF'),
    ('TEAM_REP'),
    ('TIMEKEEPER'),
    ('PLAYER');