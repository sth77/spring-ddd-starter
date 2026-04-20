---
to: src/main/java/com/example/app/<%= feature.toLowerCase().split(".").join("/") %>/<%= aggregate %>.java
inject: true
after: "private final <%= aggregate %>Id id;"
skip_if: "<%= h.changeCase.camel(name) %>"
---
<%
    include(`${templates}/variables.ejs`)
    const fieldName = h.changeCase.camel(locals.name)
    const fieldType = locals.type
    const isNullable = fieldType === 'nullable-string' || fieldType === 'text'
    const isAssociation = fieldType === 'association'
    const isEnum = fieldType === 'enum'
    const isBoolean = fieldType === 'boolean'
    const isInstant = fieldType === 'instant'

    let javaType
    if (fieldType === 'string' || fieldType === 'nullable-string' || fieldType === 'text') javaType = 'String'
    else if (isEnum) javaType = h.changeCase.pascal(locals.name)
    else if (isAssociation) javaType = 'Association<' + locals.target + ', ' + locals.target + '.' + locals.target + 'Id>'
    else if (isBoolean) javaType = 'boolean'
    else if (isInstant) javaType = 'Instant'
-%>
<% if (isNullable) { -%>
    private @Nullable <%= javaType %> <%= fieldName %>;
<% } else { -%>
    private <%= javaType %> <%= fieldName %>;
<% } -%>
