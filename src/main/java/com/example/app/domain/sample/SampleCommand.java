package com.example.app.domain.sample;

import com.example.app.common.model.Command;
import com.example.app.domain.person.Person;
import lombok.Builder;
import org.jmolecules.architecture.onion.simplified.DomainRing;

@DomainRing
public sealed interface SampleCommand extends Command {

    @Builder
    record CreateSample(
        String name,
        String description,
        Person owner
        // TODO add the fields required to create the aggregate
        ) implements SampleCommand { }

    @Builder
    record UpdateSample(
        String name,
        String description) implements SampleCommand { }

    record PublishSample() implements SampleCommand {
        public static PublishSample create() {
            return new PublishSample();
        }
    }

}
