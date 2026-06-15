---
to: src/main/java/com/example/app/referencedata/<%= h.inflection.pluralize(Name) %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= ReferenceDataPackage %>;

import <%= CommonPackage %>.model.ReferenceDataRepository;
import com.example.app.referencedata.<%= Name %>.<%= Name %>Id;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "<%= h.changeCase.lower(RepositoryType) %>", path = "<%= h.changeCase.lower(RepositoryType) %>")
public interface <%= RepositoryType %> extends ReferenceDataRepository<<%= ReferenceDataType %>, <%= IdType %>> {

    @Override
    default Class<<%= ReferenceDataType %>> getItemType() {
        return <%= ReferenceDataType %>.class;
    }

    List<<%= ReferenceDataType %>> findByNameEnStartingWith(String name);

}
