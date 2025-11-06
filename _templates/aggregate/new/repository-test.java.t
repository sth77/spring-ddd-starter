---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.inflection.pluralize(Name) %>Test.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= StateType %>;
import jakarta.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static <%= FeaturePackage %>.<%= TestDataType %>.<%= aggregateName %>;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class <%= RepositoryType %>Test {

    @Autowired
    <%= RepositoryType %> <%= repositoryName %>;

    @Autowired
    EntityManager entityManager;

    @Test
    void save_valid<%= AggregateType %>Given_savedToDb() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();
        val initialCount = <%= repositoryName %>.count();

        // act
        <%= repositoryName %>.save(<%= aggregateName %>);

        // assert
        assertThat(<%= repositoryName %>.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();
        <%= repositoryName %>.save(<%= aggregateName %>);
        entityManager.flush();
        entityManager.clear();

        // act
        val result = <%= repositoryName %>.findById(<%= aggregateName %>.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual).isEqualTo(<%= aggregateName %>);
            assertThat(actual).isNotSameAs(<%= aggregateName %>);
            assertThat(actual.getName()).isEqualTo(<%= aggregateName %>.getName());
            assertThat(actual.getState()).isEqualTo(<%= StateType %>.DRAFT);
        });
    }

}
