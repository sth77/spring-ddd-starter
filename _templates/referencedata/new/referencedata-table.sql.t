---
to: src/main/resources/db/migration/V0001__initial_schema.sql
inject: true
append: true
skip_if: IF NOT EXISTS "<%= referenceDataName %>"
---


CREATE TABLE IF NOT EXISTS "<%= referenceDataName %>" (
    "id" UUID PRIMARY KEY,
    "name_en" VARCHAR(255) NOT NULL,
    "name_de" VARCHAR(255) NOT NULL
    );