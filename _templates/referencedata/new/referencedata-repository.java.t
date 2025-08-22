---
to: src/main/java/com/example/app/referencedata/<%= h.inflection.pluralize(Name) %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= ReferenceDataPackage %>;

import <%= CommonPackage %>.model.ReferenceDataRepository;
import com.example.app.referencedata.<%= Name %>.<%= Name %>Id;

import java.util.List;

public interface <%= RepositoryType %> extends ReferenceDataRepository<<%= ReferenceDataType %>, <%= IdType %>> {

    @Override
    default Class<<%= ReferenceDataType %>> getItemType() {
        return <%= ReferenceDataType %>.class;
    }

    List<<%= ReferenceDataType %>> findByNameEnStartingWith(String name);

}
