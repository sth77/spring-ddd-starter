package com.example.app.sample.web;

import static com.example.app.common.web.CommandLinks.afford;

import com.example.app.common.web.SecuredAggregateCommands;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * Renders the operations of a {@link Sample} as HAL links: one link per command that the aggregate allows in its
 * current
 * state ({@code can}) and the current user's role permits ({@code @Secured} on the operations controller), each
 * carrying the HAL-FORMS template of its command.
 */
@Component
@RequiredArgsConstructor
public class SampleLinks implements RepresentationModelProcessor<EntityModel<Sample>> {

    private final EntityLinks entityLinks;
    private final SecuredAggregateCommands<Sample, SampleCommand> aggregateCommands = new SecuredAggregateCommands<>(
            Sample.class, SampleCommand.class, SampleOperationsController.class);

    @Override
    public EntityModel<Sample> process(EntityModel<Sample> model) {
        if (model.getContent() instanceof Sample sample) {
            aggregateCommands.getAllowedCommands().forEach(command -> addCommandLink(model, sample, command));
            model.addIf(
                    !model.hasLink(IanaLinkRelations.SELF),
                    () -> entityLinks
                            .linkForItemResource(Sample.class, sample.getId())
                            .withSelfRel());
        }
        return model;
    }

    private void addCommandLink(EntityModel<Sample> model, Sample sample, Class<? extends SampleCommand> commandType) {
        val rel = aggregateCommands.getRel(commandType);
        model.addIf(
                sample.can(commandType),
                () -> afford(entityLinks
                        .linkForItemResource(Sample.class, sample.getId())
                        .slash(rel)
                        .withRel(rel), commandType));
    }
}
