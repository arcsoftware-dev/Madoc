ALTER TABLE "madoc"."games"
ADD COLUMN IF NOT EXISTS "video_id" varchar(20) UNIQUE;
