---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>

@NonNullApi
@NonNullFields
package <%= FeaturePackage %>;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;