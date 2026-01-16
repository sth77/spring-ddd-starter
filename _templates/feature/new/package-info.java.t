---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
@NullMarked
@DomainRing
package <%= FeaturePackage %>;

import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.jspecify.annotations.NullMarked;
