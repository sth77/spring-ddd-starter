package com.example.app.sample;

import com.example.app.common.model.Command;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.referencedata.City;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.server.core.Relation;

@DomainRing
public sealed interface SampleCommand extends Command {

    @Builder
    @Relation("produce") // showcase override
    record CreateSample(
            @NotNull @Valid I18nText name,
            @Size(max = 1000) String description,
            @Nullable City city, // from master data
            @NotNull Person owner
    ) implements SampleCommand {
    }

    @Builder
    record UpdateSample(
            @NotNull @Valid I18nText name,
            @Size(max = 1000) String description,
            @Nullable City city) implements SampleCommand {
    }

    record PublishSample() implements SampleCommand {
        public static PublishSample create() {
            return new PublishSample();
        }
    }

    /**
     * Internal command, not exposed through the REST API: carries the new owner name when the owning
     * person is renamed (see the {@code SampleOwnerNameSynchronizer} application listener).
     */
    record UpdateOwnerName(String ownerName) implements SampleCommand {
        public static UpdateOwnerName of(String ownerName) {
            return new UpdateOwnerName(ownerName);
        }
    }

}
