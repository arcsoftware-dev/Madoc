CREATE TABLE IF NOT EXISTS "madoc"."role_assignments" (
    "id" SERIAL PRIMARY KEY,
    "username" varchar(50) REFERENCES "madoc"."users"("username"),
    "role_id" integer REFERENCES "madoc"."roles"("id"),
    "assigned_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint to prevent duplicate role assignments
ALTER TABLE "madoc"."role_assignments"
ADD CONSTRAINT unique_user_role UNIQUE ("username", "role_id");