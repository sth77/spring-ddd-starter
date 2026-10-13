package com.example.app.sample;

import com.example.app.common.model.Command;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.referencedata.City;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.server.core.Relation;

public sealed interface SampleCommand extends Command {

    @Builder
    @Relation("produce") // showcase override
    record CreateSample(
            @NotNull @Valid I18nText name,
            @Size(max = 1000) String description,
            @Nullable City city, // from master data
            @NotNull Person owner)
            implements
                SampleCommand {
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

    // sample enrichment: commands on the SampleItem child entity

    record AddSampleItem(@NotBlank @Size(max = 255) String label) implements SampleCommand {
        public static AddSampleItem of(String label) {
            return new AddSampleItem(label);
        }
    }

    /**
     * Commands that operate on a single {@link SampleItem}. The item is addressed by the URI
     * ({@code /samples/{sampleId}/items/{itemId}/<rel>}), like the aggregate itself, not by the payload; the
     * aggregate operations therefore take the item id next to the command. Only this interface, not its records, is
     * a permitted subclass of {@link SampleCommand}, so the aggregate's own command links skip them.
     */
    sealed interface SampleItemCommand extends SampleCommand {
    }

    record RenameSampleItem(@NotBlank @Size(max = 255) String label) implements SampleItemCommand {
        public static RenameSampleItem of(String label) {
            return new RenameSampleItem(label);
        }
    }

    record CheckSampleItem() implements SampleItemCommand {
        public static CheckSampleItem create() {
            return new CheckSampleItem();
        }
    }

    record RemoveSampleItem() implements SampleItemCommand {
        public static RemoveSampleItem create() {
            return new RemoveSampleItem();
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
