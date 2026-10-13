---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(aggregate) %>Test.java
inject: true
before: "^}"
skip_if: "void <%= h.changeCase.camel(name) %>_inDraftState_"
eof_last: false
---
<%
   include(`${templates}/operation-variables.ejs`)
-%>
    @Test
    void <%= operationName %>_inDraftState_eventRegistered() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();

        // act
        <%= aggregateName %>.<%= operationName %>(<%= OperationCommandType %>.create());

        // assert
        assertThat(getEvents(<%= aggregateName %>)).containsExactly(<%= OperationEventType %>.of(<%= aggregateName %>.getId()));
    }

    @Test
    void can<%= OperationName %>_inPublishedState_falseReturned() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();
        <%= aggregateName %>.publish(<%= PublishCommandType %>.create());

        // act & assert
        assertThat(<%= aggregateName %>.can(<%= OperationCommandType %>.class)).isFalse();
    }

