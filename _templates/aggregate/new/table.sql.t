---
to: src/main/resources/db/migration/V0001__initial_schema.sql
inject: true
append: true
skip_if: IF NOT EXISTS "<%= name %>"
---


CREATE TABLE IF NOT EXISTS "<%= name %>" (
    "id" UUID PRIMARY KEY,
    "version" INT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "state" VARCHAR(20) NOT NULL
    );