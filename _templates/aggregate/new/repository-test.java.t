---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.inflection.pluralize(Name) %>Test.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= StateType %>;
import <%= FeaturePackage %>.<%= AggregateType %>Command.<%= CreateCommandType %>;
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

        // act
        val result = <%= repositoryName %>.findById(<%= aggregateName %>.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo(<%= aggregateName %>.getName());
            assertThat(actual.getState()).isEqualTo(<%= StateType %>.DRAFT);
        });
    }

    private static <%= AggregateType %> <%= aggregateName %>() {
        return <%= AggregateType %>.create(<%= CreateCommandType %>.builder()
                .name("<%= AggregateType %> 1")
                .build());
    }

}
