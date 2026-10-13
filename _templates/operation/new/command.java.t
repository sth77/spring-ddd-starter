---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(aggregate) %>Command.java
inject: true
before: "^}"
skip_if: "record <%= h.changeCase.pascal(name) %><%= h.changeCase.pascal(aggregate) %>\\("
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
    /**
     * Executes the <%= operationName %> operation (see {@link <%= AggregateType %>#<%= operationName %>}). Add the
     * data the operation needs as record components, with Bean Validation constraints: they are validated on the
     * request and describe the fields of the operation's HAL-FORMS template.
     */
    record <%= OperationCommandType %>() implements <%= CommandType %> {
        public static <%= OperationCommandType %> create() {
            return new <%= OperationCommandType %>();
        }
    }

