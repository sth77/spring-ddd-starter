package com.example.app.infrastructure.web;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.data.projection.TargetAware;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

/**
 * Projections need their own representation model processors in spring-data-rest.
 * To avoid code duplication the ProjectionLinks delegates the link creation to
 * the representation model processor of the underlying entity.
 *
 * @param <E> entity type the projection is associated with
 * @param <T> the projection type that this processor handles
 */
@RequiredArgsConstructor
public abstract class ProjectionLinks<E extends AggregateRoot<?, ?>, T> implements RepresentationModelProcessor<EntityModel<T>> {

    private final RepresentationModelProcessor<EntityModel<E>> delegate;

    @SuppressWarnings("unchecked")
    @Override
    public EntityModel<T> process(EntityModel<T> model) {
        if (model.getContent() instanceof TargetAware targetAware
                && targetAware.getTarget() instanceof AggregateRoot<?, ?>) {
            val target = (E) targetAware.getTarget();
            model.add(delegate.process(EntityModel.of(target)).getLinks());
        }
        return model;
    }

}