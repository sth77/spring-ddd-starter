---
to: src/main/java/com/example/app/<%= feature %>/web/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
@NullMarked
@InfrastructureRing
@NamedInterface("web")
package <%= FeatureWebPackage %>;

import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
