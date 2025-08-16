---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/package-info.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
@NonNullApi
@NonNullFields
@DomainRing
package <%= FeaturePackage %>;

import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;