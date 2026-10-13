---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(aggregate) %>.java
inject: true
before: "^\\s*private void assertCan\\("
skip_if: "public void <%= h.changeCase.camel(name) %>\\("
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
    /**
     * Executes the <%= operationName %> operation. Allowed whenever {@link #can(Class)} allows
     * {@link <%= OperationCommandType %>}, which decides about the HAL link of the operation as well.
     */
    public void <%= operationName %>(<%= OperationCommandType %> data) {
        assertCan(<%= OperationCommandType %>.class);
        // TODO change the state of the aggregate as the operation requires
        registerEvent(<%= OperationEventType %>.of(id));
    }

