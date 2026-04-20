---
to: "<%= locals.type === 'enum' ? 'src/main/java/com/example/app/' + feature.toLowerCase().split('.').join('/') + '/' + h.changeCase.pascal(name) + '.java' : null %>"
---
<%
    include(`${templates}/variables.ejs`)
    const enumName = h.changeCase.pascal(locals.name)
    const values = (locals.values || '').split(',').map(v => v.trim())
-%>
package <%= FeaturePackage %>;

public enum <%= enumName %> {
<% values.forEach((value, i) => { -%>
    <%= value %><%= i < values.length - 1 ? ',' : '' %>
<% }) -%>
}
