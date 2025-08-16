package com.example.app._infrastructure.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.data.projection.TargetAware;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

/**
 * In spring-data-rest, projections need their own representation model processors.
 * To avoid code duplication, the {@link ProjectionLinks} delegates the link creation to
 * the representation model processor of the underlying entity.
 *
 * @param <T> entity type the projection is associated with
 */
@Slf4j
@RequiredArgsConstructor
public class ProjectionLinks<T extends AggregateRoot<?, ?>> implements RepresentationModelProcessor<EntityModel<TargetAware>> {

    private final RepresentationModelProcessor<EntityModel<T>> delegate;
    private final Class<T> aggregateType;

    @Override
    public EntityModel<TargetAware> process(EntityModel<TargetAware> model) {
        if (model.getContent() instanceof TargetAware targetAware
                && aggregateType.isInstance(targetAware.getTarget())) {
            val target = aggregateType.cast(targetAware.getTarget());
            log.info("Creating links for " + target + " with input " + model.getContent());
            model.add(delegate.process(EntityModel.of(target)).getLinks());
        }
        return model;
    }

}