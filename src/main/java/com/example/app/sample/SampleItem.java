package com.example.app.sample;

import com.example.app.sample.SampleCommand.AddSampleItem;
import com.example.app.sample.SampleCommand.SampleItemCommand;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

/**
 * Sample enrichment, not scaffolding output: a child entity of the {@link Sample} aggregate, showing commands
 * that address a single child instance. It is only ever changed through its aggregate root, hence the
 * package-private operations.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SampleItem implements Entity<Sample, SampleItem.SampleItemId> {

    private final SampleItemId id;
    private String label;
    private boolean checked;

    static SampleItem create(AddSampleItem data) {
        return new SampleItem(SampleItemId.random(), data.label(), false);
    }

    void rename(String label) {
        this.label = label;
    }

    void check() {
        checked = true;
    }

    /**
     * The item's own share of the rule; {@link Sample#can(Class, SampleItemId)} combines it with the
     * aggregate's state. A checked item is done and accepts no further operation.
     */
    boolean can(Class<? extends SampleItemCommand> operation) {
        return !checked;
    }

    public record SampleItemId(UUID uuidValue) implements Identifier {

        public static SampleItemId random() {
            return SampleItemId.of(UUID.randomUUID());
        }

        public static SampleItemId of(UUID uuidValue) {
            return new SampleItemId(uuidValue);
        }

        @Override
        public String toString() {
            return uuidValue.toString();
        }
    }
}
