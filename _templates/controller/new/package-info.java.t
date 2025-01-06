---
to: src/main/java/com/example/app/domain/<%= h.changeCase.lower(name) %>/web/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>

@NonNullApi
@NonNullFields
@InfrastructureRing
package <%= FeaturePackage %>;

import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;