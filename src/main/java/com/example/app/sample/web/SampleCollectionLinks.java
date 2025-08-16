package com.example.app.sample.web;

import com.example.app.common.model.AggregateCommands;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand;
import com.example.app.sample.SampleCommand.CreateSample;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleCollectionLinks implements RepresentationModelProcessor<CollectionModel<EntityModel<Sample>>> {

    private final EntityLinks entityLinks;
    private final AggregateCommands<Sample, SampleCommand> aggregateCommands = new AggregateCommands<>(Sample.class, SampleCommand.class);

    @Nonnull
    @Override
    public CollectionModel<EntityModel<Sample>> process(CollectionModel<EntityModel<Sample>> model) {
        return model.add(entityLinks.linkFor(Sample.class).withRel(aggregateCommands.getRel(CreateSample.class)));
    }

}
