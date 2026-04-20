---
to: "<%= locals.migration ? 'src/main/resources/db/migration/' + locals.migration + '__add_' + h.changeCase.snakeCase(name) + '_to_' + h.changeCase.snakeCase(aggregate) + '.sql' : 'src/main/resources/db/migration/V0001__initial_schema.sql' %>"
<% if (!locals.migration) { -%>
inject: true
after: "CREATE TABLE.*<%= h.changeCase.snakeCase(locals.aggregate) %>"
skip_if: "<%= h.changeCase.snakeCase(locals.name) %>"
<% } -%>
---
<%
    const colName = h.changeCase.snakeCase(locals.name)
    const tableName = h.changeCase.snakeCase(locals.aggregate)
    const fieldType = locals.type
    const isRequired = locals.required || fieldType === 'string' || fieldType === 'boolean' || fieldType === 'association'
    const defaultVal = locals.default
    const maxLength = locals.maxLength || (fieldType === 'string' ? 255 : null)

    let sqlType
    if (fieldType === 'string' || fieldType === 'nullable-string') sqlType = 'VARCHAR(' + (maxLength || 255) + ')'
    else if (fieldType === 'text') sqlType = 'TEXT'
    else if (fieldType === 'enum') sqlType = 'VARCHAR(20)'
    else if (fieldType === 'association') sqlType = 'UUID'
    else if (fieldType === 'boolean') sqlType = 'BOOLEAN'
    else if (fieldType === 'instant') sqlType = 'TIMESTAMP WITH TIME ZONE'

    let constraints = ''
    if (isRequired) constraints += ' NOT NULL'
    if (defaultVal) constraints += " DEFAULT '" + defaultVal + "'"
    if (fieldType === 'boolean' && defaultVal) constraints = " NOT NULL DEFAULT " + defaultVal.toUpperCase()

    // For association: add FK reference
    const fkRef = fieldType === 'association'
        ? ' REFERENCES ' + h.changeCase.snakeCase(locals.target) + '(id)'
        : ''
-%>
<% if (locals.migration) { -%>
-- Add <%= colName %> to <%= tableName %>
ALTER TABLE <%= tableName %> ADD COLUMN <%= colName %> <%= sqlType %><%= constraints %><%= fkRef %>;
<% if (fieldType === 'association') { -%>

CREATE INDEX idx_<%= tableName %>_<%= colName %> ON <%= tableName %>(<%= colName %>);
<% } -%>
<% } else { -%>
    <%= colName %> <%= sqlType %><%= constraints %><%= fkRef %>,
<% } -%>
