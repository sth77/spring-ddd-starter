---
to: <%= migrationIsAppend ? "src/main/resources/db/migration/V0001__initial_schema.sql" : migrationFile %>
<% if (migrationIsAppend) { -%>
inject: true
append: true
skip_if: IF NOT EXISTS "<%= h.changeCase.snakeCase(name) %>"
<% } -%>
---
<%
   include(`${templates}/variables.ejs`)
-%>
<% if (migrationIsAppend) { -%>

CREATE TABLE IF NOT EXISTS <%= h.changeCase.snakeCase(name) %> (
    id               UUID          PRIMARY KEY,
    version          INT           NOT NULL DEFAULT 0,
    name             VARCHAR(255)  NOT NULL,
    state            VARCHAR(20)   NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);
<% } else { -%>
-- <%= h.changeCase.pascal(feature.split(".").pop()) %> module: <%= AggregateType %> aggregate
CREATE TABLE <%= h.changeCase.snakeCase(name) %> (
    id               UUID          PRIMARY KEY,
    version          INT           NOT NULL DEFAULT 0,
    name             VARCHAR(255)  NOT NULL,
    state            VARCHAR(20)   NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);
<% } -%>
