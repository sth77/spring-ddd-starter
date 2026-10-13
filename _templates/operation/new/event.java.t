---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(aggregate) %>Event.java
inject: true
before: "^}"
skip_if: "record <%= locals.event ? h.changeCase.pascal(locals.event) : h.changeCase.pascal(aggregate) + h.changeCase.pascal(name) %>"
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
    /**
     * Registered once the <%= operationName %> operation has been executed on a <%= AggregateType %>.
     */
    record <%= OperationEventType %>(
        <%= IdType %> <%= idName %>) implements <%= EventType %> {

        public static <%= OperationEventType %> of(<%= IdType %> <%= idName %>) {
            return new <%= OperationEventType %>(<%= idName %>);
        }
    }

