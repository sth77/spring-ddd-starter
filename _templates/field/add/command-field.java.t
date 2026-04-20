---
to: src/main/java/com/example/app/<%= feature.toLowerCase().split(".").join("/") %>/<%= aggregate %>Command.java
inject: true
after: "Create<%= aggregate %>\\("
skip_if: "<%= h.changeCase.camel(name) %>"
---
<%
    const fieldName = h.changeCase.camel(locals.name)
    const fieldType = locals.type
    const isRequired = locals.required || fieldType === 'string'
    const isNullable = fieldType === 'nullable-string' || fieldType === 'text'
    const maxLength = locals.maxLength

    let javaType
    if (fieldType === 'string' || fieldType === 'nullable-string' || fieldType === 'text') javaType = 'String'
    else if (fieldType === 'enum') javaType = h.changeCase.pascal(locals.name)
    else if (fieldType === 'boolean') javaType = 'boolean'
    else if (fieldType === 'instant') javaType = 'Instant'
    else if (fieldType === 'association') return // associations are not command fields

    let annotations = ''
    if (fieldType === 'string' && isRequired) annotations = '@NotBlank '
    if (maxLength) annotations += '@Size(max = ' + maxLength + ') '
    if (isNullable) annotations = '@Nullable '
-%>
<% if (fieldType !== 'association') { -%>
            <%= annotations %><%= javaType %> <%= fieldName %>,
<% } -%>
