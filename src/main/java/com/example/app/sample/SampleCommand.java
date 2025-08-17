package com.example.app.sample;

import com.example.app.common.model.Command;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import lombok.Builder;
import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.springframework.hateoas.server.core.Relation;

@DomainRing
public sealed interface SampleCommand extends Command {

    @Builder
    @Relation("produce") // showcase override
    record CreateSample(
            I18nText name,
            String description,
            Person owner
            // TODO add the fields required to create the aggregate
    ) implements SampleCommand {
    }

    @Builder
    record UpdateSample(
            I18nText name,
            String description) implements SampleCommand {
    }

    record PublishSample() implements SampleCommand {
        public static PublishSample create() {
            return new PublishSample();
        }
    }

}
