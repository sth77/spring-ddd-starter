package com.example.app.sample.web;

import static com.example.app.common.web.CommandLinks.afford;

import com.example.app.common.web.ChildEntityLinks;
import com.example.app.common.web.SecuredAggregateCommands;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.SampleItemCommand;
import com.example.app.sample.SampleItem;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * Sample enrichment, not scaffolding output: computes the command links of every {@link SampleItem} while the
 * aggregate is processed - only the aggregate can tell what an item allows - and hands them to
 * {@link ChildEntityLinks}, which renders them on the nested item. {@link SampleLinks} stays exactly as generated.
 */
@Component
@RequiredArgsConstructor
public class SampleItemLinks implements RepresentationModelProcessor<EntityModel<Sample>> {

    private final EntityLinks entityLinks;
    private final ChildEntityLinks childEntityLinks;
    private final SecuredAggregateCommands<Sample, SampleItemCommand> itemCommands = new SecuredAggregateCommands<>(
            Sample.class, SampleItemCommand.class, SampleOperationsController.class);

    @Override
    public EntityModel<Sample> process(EntityModel<Sample> model) {
        if (model.getContent() instanceof Sample sample) {
            sample.getItems().forEach(item -> childEntityLinks.put(item, itemCommands.getAllowedCommands().stream()
                    .filter(command -> sample.can(command, item.getId()))
                    .map(command -> itemLink(sample, item, command))
                    .toList()));
        }
        return model;
    }

    private Link itemLink(Sample sample, SampleItem item, Class<? extends SampleItemCommand> command) {
        val rel = itemCommands.getRel(command);
        return afford(entityLinks.linkForItemResource(Sample.class, sample.getId())
                .slash("items")
                .slash(item.getId())
                .slash(rel)
                .withRel(rel), command);
    }
}
