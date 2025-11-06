---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(feature) %>TestData.java
inject: true
after: "TestData {\n"
---
<%
   include(`${templates}/variables.ejs`)
-%>

    public static <%= AggregateType %> <%= aggregateName %>() {
        return <%= aggregateName %>("<%= AggregateType %> X");
    }

    public static <%= AggregateType %> <%= aggregateName %>(String name) {
        val result = <%= AggregateType %>.create(<%= CreateCommandType %>.builder()
                .name(name)
                .build());
        clearEvents(result);
        return result;
    }

