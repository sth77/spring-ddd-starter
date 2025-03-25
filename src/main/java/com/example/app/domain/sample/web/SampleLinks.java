package com.example.app.domain.sample.web;

import com.example.app.domain.common.model.CommandHelper;
import com.example.app.domain.sample.Sample;
import com.example.app.domain.sample.SampleCommand;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleLinks implements RepresentationModelProcessor<EntityModel<Sample>> {

    private final EntityLinks entityLinks;
    private final CommandHelper<Sample, SampleCommand, SampleOperationsController> commandHelper = new CommandHelper<>(Sample.class, SampleCommand.class, SampleOperationsController.class);

    @Override
    public EntityModel<Sample> process(EntityModel<Sample> model) {
        if (model.getContent() instanceof Sample sample) {
            commandHelper.getCommands().forEach(
                    command -> addCommandLink(model, sample, command));
        }
        return model;
    }

    private void addCommandLink(
            EntityModel<Sample> model,
            Sample sample,
            Class<? extends SampleCommand> commandType) {
        val rel = commandHelper.getRel(commandType);
        model.addIf(sample.can(commandType), () -> entityLinks
                .linkFor(Sample.class).slash(rel)
                .withRel(rel));
    }
}
