CREATE SEQUENCE IF NOT EXISTS "hibernate_sequence";

CREATE TABLE IF NOT EXISTS "aggregate_name" (
    "id" VARCHAR(36) PRIMARY KEY,
    "version" INT
    -- add more fileds as required
);
