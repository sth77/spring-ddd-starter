package com.example.app.infrastructure.web.sampleapi;

import com.example.app.domain.sample.Sample;
import com.example.app.infrastructure.web.ProjectionLinks;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@InfrastructureRing
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

    @Component
    static class SampleSummaryLinks extends ProjectionLinks<Sample, SampleSummary> {
        public SampleSummaryLinks(SampleLinks delegate) {
            super(delegate);
        }
    }

    @Component
    static class SampleDetailLinks extends ProjectionLinks<Sample, SampleDetail> {
        public SampleDetailLinks(SampleLinks delegate) {
            super(delegate);
        }
    }

}
