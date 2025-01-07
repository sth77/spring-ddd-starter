---
to: src/main/java/com/example/app/infrastructure/web/<%= h.changeCase.lower(feature) %>api/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>

@NonNullApi
@NonNullFields
@InfrastructureRing
package <%= FeatureWebPackage %>;

import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;