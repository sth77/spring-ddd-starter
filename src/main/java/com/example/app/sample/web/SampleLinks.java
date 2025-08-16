package com.example.app.sample.web;

import com.example.app.common.model.AggregateCommands;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleLinks implements RepresentationModelProcessor<EntityModel<Sample>> {

    private final EntityLinks entityLinks;
    private final AggregateCommands<Sample, SampleCommand> aggregateCommands = new AggregateCommands<>(Sample.class, SampleCommand.class);

    @Override
    public EntityModel<Sample> process(EntityModel<Sample> model) {
        if (model.getContent() instanceof Sample sample) {
            aggregateCommands.getCommands().forEach(
                    command -> addCommandLink(model, sample, command));
            model.addIf(!model.hasLink(IanaLinkRelations.SELF),
                    () -> entityLinks.linkForItemResource(Sample.class, sample.getId()).withSelfRel());
        }
        return model;
    }

    private void addCommandLink(
            EntityModel<Sample> model,
            Sample sample,
            Class<? extends SampleCommand> commandType) {
        val rel = aggregateCommands.getRel(commandType);
        model.addIf(sample.can(commandType), () -> entityLinks
                .linkFor(Sample.class).slash(rel)
                .withRel(rel));
    }
}
