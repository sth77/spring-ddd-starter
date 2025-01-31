CREATE SEQUENCE IF NOT EXISTS "hibernate_sequence";

-- table for spring modulith event publication
CREATE TABLE IF NOT EXISTS "event_publication" (
    "id" UUID PRIMARY KEY,
    "completion_date" TIMESTAMP(9) WITH TIME ZONE,
    "event_type" VARCHAR(512) NOT NULL,
    "listener_id" VARCHAR(512) NOT NULL,
    "publication_date" TIMESTAMP(9) WITH TIME ZONE NOT NULL,
    "serialized_event" VARCHAR(4000) NOT NULL
    );

CREATE TABLE IF NOT EXISTS "person" (
    "id" UUID PRIMARY KEY,
    "version" INT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    CONSTRAINT "person_unique_name" UNIQUE ("name")
    );

CREATE TABLE IF NOT EXISTS "sample" (
    "id" UUID PRIMARY KEY,
    "version" INT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" VARCHAR(2048) NOT NULL,
    "state" VARCHAR(20) NOT nULL,
    "owner" VARCHAR(36) NOT NULL,
    FOREIGN KEY ("owner") REFERENCES "person"("id")
    );
