---
to: src/main/java/com/example/app/<%= feature.toLowerCase().split(".").join("/") %>/web/<%= aggregate %>Detail.java
inject: true
before: "^}"
skip_if: "get<%= h.changeCase.pascal(name) %>"
---
<%
    const fieldName = h.changeCase.camel(locals.name)
    const getterName = 'get' + h.changeCase.pascal(locals.name)
    const fieldType = locals.type

    // Skip association fields in projections
    if (fieldType === 'association') return

    let javaType
    if (fieldType === 'string' || fieldType === 'nullable-string' || fieldType === 'text') javaType = 'String'
    else if (fieldType === 'enum') javaType = h.changeCase.pascal(locals.name)
    else if (fieldType === 'boolean') javaType = (locals.name.startsWith('is') ? '' : 'boolean')
    else if (fieldType === 'instant') javaType = 'Instant'

    const prefix = fieldType === 'boolean' ? 'is' : 'get'
    const methodName = fieldType === 'boolean' ? prefix + h.changeCase.pascal(locals.name) : getterName
-%>
<% if (fieldType !== 'association') { -%>
    <%= javaType %> <%= methodName %>();
<% } -%>
