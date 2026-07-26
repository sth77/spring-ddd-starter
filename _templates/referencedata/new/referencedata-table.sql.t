---
to: src/main/resources/db/migration/V<%= new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14) %>__create_<%= h.changeCase.snakeCase(referenceDataName) %>.sql
---
CREATE TABLE <%= h.changeCase.snakeCase(referenceDataName) %> (
    id UUID PRIMARY KEY,
    name_en VARCHAR(255) NOT NULL,
    name_de VARCHAR(255) NOT NULL
    );