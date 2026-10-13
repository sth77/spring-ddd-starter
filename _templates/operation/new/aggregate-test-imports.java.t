---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(aggregate) %>Test.java
inject: true
after: "^import .*\\.<%= h.changeCase.pascal(aggregate) %>Command\\.Create<%= h.changeCase.pascal(aggregate) %>;"
skip_if: "<%= h.changeCase.pascal(aggregate) %>Command\\.<%= h.changeCase.pascal(name) %><%= h.changeCase.pascal(aggregate) %>;"
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
import <%= FeaturePackage %>.<%= CommandType %>.<%= OperationCommandType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= OperationEventType %>;
