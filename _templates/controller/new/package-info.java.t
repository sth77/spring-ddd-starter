---
to: src/main/java/com/example/app/<%= feature %>/web/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>

@NonNullApi
@NonNullFields
@InfrastructureRing
@NamedInterface("web")
package <%= FeatureWebPackage %>;

import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
import org.springframework.modulith.NamedInterface;
