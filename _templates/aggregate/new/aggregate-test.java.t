---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>Test.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import com.example.app.common.model.DomainException;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= UpdateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= PublishCommandType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= CreatedEventType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= PublishedEventType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= UpdatedEventType %>;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.example.app.AggregateEvents.clearEvents;
import static com.example.app.AggregateEvents.getEvents;
import static <%= FeaturePackage %>.<%= AggregateType %>.<%= StateType %>.DRAFT;
import static <%= FeaturePackage %>.<%= AggregateType %>.<%= StateType %>.PUBLISHED;
import static <%= FeaturePackage %>.<%= AggregateType %>TestData.<%= aggregateName %>;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class <%= AggregateType %>Test {

    @Test
    void create_validDataGiven_created() {
        // arrange
        val name = "<%= AggregateType %> 1";

        // act
        val <%= aggregateName %> = <%= AggregateType %>.create(<%= CreateCommandType %>.builder()
                .name(name)
                .build());

        // assert
        assertThat(<%= aggregateName %>.getName()).isEqualTo(name);
        assertThat(<%= aggregateName %>.getState()).isEqualTo(DRAFT);
        assertThat(getEvents(<%= aggregateName %>)).containsExactly(
                <%= CreatedEventType %>.of(<%= aggregateName %>.getId()));
    }

    @Test
    void update_validDataGiven_updated() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();
        val updatedName = "<%= AggregateType %> with updated name";

        // act
        <%= aggregateName %>.update(<%= UpdateCommandType %>.builder()
                .name(updatedName)
                .build());

        // assert
        assertThat(<%= aggregateName %>.getName()).isEqualTo(updatedName);
        assertThat(<%= aggregateName %>.getState()).isEqualTo(DRAFT);
        assertThat(getEvents(<%= aggregateName %>)).containsExactly(<%= UpdatedEventType %>.builder()
                .<%= aggregateName %>Id(<%= aggregateName %>.getId())
                .name(updatedName)
                .build());
    }

    @Test
    void publish_inDraftState_published() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();

        // act
        <%= aggregateName %>.publish();

        // assert
        assertThat(<%= aggregateName %>.getState()).isEqualTo(PUBLISHED);
        assertThat(getEvents(<%= aggregateName %>)).containsExactly(<%= PublishedEventType %>.builder()
                .<%= aggregateName %>Id(<%= aggregateName %>.getId())
                .name(<%= aggregateName %>.getName())
                .build());
    }

    @Test
    void publish_alreadyPublished_exceptionThrown() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();
        <%= aggregateName %>.publish();
        clearEvents(<%= aggregateName %>);

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(<%= aggregateName %>::publish);
    }

    @Test
    void canPublish_inDraftState_trueReturned() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();

        // act & assert
        assertThat(<%= aggregateName %>.can(<%= PublishCommandType %>.class)).isTrue();
    }


    @Test
    void canUpdate_inPublishedState_falseReturned() {
        // arrange
        val <%= aggregateName %> = <%= aggregateName %>();
        <%= aggregateName %>.publish();

        // act & assert
        assertThat(<%= aggregateName %>.can(<%= UpdateCommandType %>.class)).isFalse();
    }

}
