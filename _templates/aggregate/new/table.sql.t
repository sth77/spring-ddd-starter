---
to: src/main/resources/db/migration/V0001__initial_schema.sql
inject: true
append: true
skip_if: IF NOT EXISTS "<%= h.changeCase.snakeCase(name) %>"
---


CREATE TABLE IF NOT EXISTS "<%= h.changeCase.snakeCase(name) %>" (
    "id" UUID PRIMARY KEY,
    "version" INT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "state" VARCHAR(20) NOT NULL
    );