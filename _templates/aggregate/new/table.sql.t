---
to: src/main/resources/db/migration/V<%= new Date().toISOString().replace(/[-:T.]/g, '').slice(0, 17) %>__create_<%= h.changeCase.snakeCase(name) %>.sql
---
-- Table of the <%= Name %> aggregate. Its column names must match the ones the naming strategies derive from the
-- aggregate's fields (ddl-auto: validate checks it at startup); change the schema with a new migration only.
CREATE TABLE <%= h.changeCase.snakeCase(name) %> (
    id UUID PRIMARY KEY,
    version INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    state VARCHAR(20) NOT NULL
    );