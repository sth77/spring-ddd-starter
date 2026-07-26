---
to: src/main/resources/db/migration/V<%= new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14) %>__create_<%= h.changeCase.snakeCase(name) %>.sql
---
CREATE TABLE <%= h.changeCase.snakeCase(name) %> (
    id UUID PRIMARY KEY,
    version INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    state VARCHAR(20) NOT NULL
    );