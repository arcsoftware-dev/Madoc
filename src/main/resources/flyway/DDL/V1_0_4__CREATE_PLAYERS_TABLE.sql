CREATE TABLE IF NOT EXISTS "madoc"."players" (
    "id" SERIAL PRIMARY KEY,
    "first_name" varchar(20),
    "last_name" varchar(20),
    "email" varchar(100),
    "phone_number" varchar(15),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);