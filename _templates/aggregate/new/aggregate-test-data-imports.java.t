---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.changeCase.pascal(feature) %>TestData.java
inject: true
before: "import lombok.experimental.UtilityClass"
skip_if: "<%= h.changeCase.pascal(name) %>Command"
---
<%
   include(`${templates}/variables.ejs`)
-%>

import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
