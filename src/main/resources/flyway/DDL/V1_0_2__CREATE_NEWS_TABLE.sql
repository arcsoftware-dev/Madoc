CREATE TABLE IF NOT EXISTS "madoc"."news" (
    "id" SERIAL PRIMARY KEY,
    "title" VARCHAR(100) NOT NULL,
    "summary" TEXT NOT NULL,
    "content" TEXT NOT NULL,
    "author" varchar(20) NOT NULL DEFAULT 'admin',
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default data
INSERT INTO "madoc"."news" ("title", "content", "summary", "author", "created_at") VALUES (
  'Welcome to Madoc!',
  'Madoc is a new platform for managing your gaming news and standings.',
  'Madoc is a new platform for managing your gaming news and standings.',
  'Admin',
  '2025-08-01 14:55:00'
);

INSERT INTO "madoc"."news" ("title", "content", "summary", "author", "created_at") VALUES (
  'League Fees 2025',
  'League fees for the 2025 season are now due. Please ensure you pay your fees by the August 15th. The deposit amount for the season is $100. The total amount for the season is $515.  Full payment date will be provided closer to the beginning of the season. ',
  'League fees for the 2025 season are now due. Please ensure you pay your fees by the August 15th. The deposit amount for the season is $100. The total amount for the season is $515.  Full payment date will be provided closer to the beginning of the season. ',
  'Admin',
  '2025-08-11 19:01:00'
);

INSERT INTO "madoc"."news" ("title", "content", "summary", "author", "created_at") VALUES (
  '2025/2026 Season Information',
  'The returning player draft will be held on September 3rd, 2025 at 7PM at Oscars. The 2025/2026 season will begin the week of September 14th 2025, with all games being played at Century Gardens. Rosters and schedules will be finalized and send out the week of September 8th. ',
  'The returning player draft will be held on September 3rd, 2025 at 7PM at Oscars. The 2025/2026 season will begin the week of September 14th 2025, with all games being played at Century Gardens. Rosters and schedules will be finalized and sent out the week of September 8th. ',
  'Admin',
  '2025-08-20 13:36:00'
);