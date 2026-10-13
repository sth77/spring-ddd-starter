---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(aggregate) %>.java
inject: true
after: "^import .*\\.<%= h.changeCase.pascal(aggregate) %>Event\\.<%= h.changeCase.pascal(aggregate) %>Created;"
skip_if: "<%= h.changeCase.pascal(aggregate) %>Event\\.<%= locals.event ? h.changeCase.pascal(locals.event) : h.changeCase.pascal(aggregate) + h.changeCase.pascal(name) %>"
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
import <%= FeaturePackage %>.<%= EventType %>.<%= OperationEventType %>;
