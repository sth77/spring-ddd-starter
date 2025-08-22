---
to: src/test/java/com/example/app/referencedata/<%= h.inflection.pluralize(Name) %>Test.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= ReferenceDataPackage %>;

import <%= CommonPackage %>.model.I18nText;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class <%= RepositoryType %>Test {

    @Autowired
    <%= RepositoryType %> <%= repositoryName %>;

    @Test
    void save_validDataGiven_savedToDb() {
        // arrange
        val initialCount = <%= repositoryName %>.count();

        // act
        val result = <%= repositoryName %>.save(<%= referenceDataName %>());

        // assert
        assertThat(result).isNotNull();
        assertThat(<%= repositoryName %>.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val <%= referenceDataName %> = <%= repositoryName %>.save(<%= referenceDataName %>());

        // act
        val result = <%= repositoryName %>.findById(<%= referenceDataName %>.getId());

        // assert
        assertThat(result).hasValue(<%= referenceDataName %>);
    }

    @Test
    void findByNameEnStartingWith_exists_returned() {
        // arrange
        val <%= referenceDataName %> = <%= repositoryName %>.save(<%= referenceDataName %>());

        // act
        val result = <%= repositoryName %>.findByNameEnStartingWith("EN F");

        // assert
        assertThat(result).containsExactly(<%= referenceDataName %>);
    }


    private static <%= ReferenceDataType %> <%= referenceDataName %>() {
        return <%= ReferenceDataType %>.of(I18nText.builder().en("EN Foo").de("DE Foo").build());
    }
}
