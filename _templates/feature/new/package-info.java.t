---
to: src/main/java/com/example/app/domain/<%= h.changeCase.lower(name) %>/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>

@NonNullApi
@NonNullFields
@DomainRing
@NamedInterface("<%= h.changeCase.lower(name) %>")
package <%= FeaturePackage %>;

import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
import org.springframework.modulith.NamedInterface;