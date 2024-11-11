---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>

@NonNullApi
package <%= FeaturePackage %>;

import org.springframework.lang.NonNullApi;