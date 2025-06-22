package com.example.app.domain.sample;

import com.example.app.domain.common.model.Command;
import com.example.app.domain.person.Person;
import lombok.Builder;
import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.springframework.hateoas.server.core.Relation;

@DomainRing
public sealed interface SampleCommand extends Command {

    @Builder
    @Relation("produce") // showcase override
    record CreateSample(
            String name,
            String description,
            Person owner
            // TODO add the fields required to create the aggregate
    ) implements SampleCommand {
    }

    @Builder
    record UpdateSample(
            String name,
            String description) implements SampleCommand {
    }

    record PublishSample() implements SampleCommand {
    }

}
