CREATE TABLE IF NOT EXISTS "madoc"."season_metadata" (
    "season_year" integer,
    "season_type" varchar(20),
    "is_current" boolean
);

-- Insert default data
INSERT INTO "madoc"."season_metadata" ("season_year", "season_type", "is_current") VALUES (
    2025, 'REGULAR_SEASON', true
);
