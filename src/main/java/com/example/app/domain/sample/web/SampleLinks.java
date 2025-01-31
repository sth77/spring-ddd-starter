package com.example.app.domain.sample.web;

import com.example.app.domain.sample.Sample;
import com.example.app.infrastructure.web.ProjectionLinks;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleLinks implements RepresentationModelProcessor<EntityModel<Sample>> {

    private final EntityLinks entityLinks;

    @Override
    public EntityModel<Sample> process(EntityModel<Sample> model) {
        if (model.getContent() instanceof Sample sample) {
            addOperationLink(model, sample, Sample.Operation.UPDATE);
            addOperationLink(model, sample, Sample.Operation.PUBLISH);
        }
        return model;
    }

    private void addOperationLink(EntityModel<Sample> model, Sample sample, Sample.Operation operation) {
        model.addIf(sample.can(Sample.Operation.UPDATE), () -> entityLinks
                .linkFor(Sample.class).slash(operation.rel)
                .withRel(operation.rel));
    }

}
