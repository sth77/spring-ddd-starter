---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/web/<%= h.changeCase.pascal(aggregate) %>OperationsController.java
inject: true
before: "^\\s*private ResponseEntity<EntityModel<<%= h.changeCase.pascal(aggregate) %>>> doWith"
skip_if: "public ResponseEntity<EntityModel<<%= h.changeCase.pascal(aggregate) %>>> <%= h.changeCase.camel(name) %>\\("
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
    @Secured("<%= RequiredRole %>")
    @PostMapping(path = "/<%= collectionRel %>/{<%= idName %>}/<%= operationName %>")
    public ResponseEntity<EntityModel<<%= AggregateType %>>> <%= operationName %>(@PathVariable <%= IdType %> <%= idName %>, @Valid @RequestBody <%= OperationCommandType %> data) {
        return doWith<%= AggregateType %>(<%= idName %>, it -> it.<%= operationName %>(data));
    }

