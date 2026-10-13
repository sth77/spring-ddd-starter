package com.example.app.common.web;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jmolecules.ddd.types.Entity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * Adds the links an aggregate's link processor has stored in {@link ChildEntityLinks} to the nested child entity
 * being rendered (see there for the ordering it relies on). It is generic over all jMolecules entities; aggregate
 * roots match as well, since {@code AggregateRoot} extends {@code Entity}, and simply get no links from here.
 */
@Component
@RequiredArgsConstructor
class ChildEntityLinksProcessor implements RepresentationModelProcessor<EntityModel<Entity<?, ?>>> {

    private final ChildEntityLinks childEntityLinks; // request-scoped proxy

    @Override
    public EntityModel<Entity<?, ?>> process(EntityModel<Entity<?, ?>> model) {
        val content = model.getContent();
        return content == null ? model : model.add(childEntityLinks.linksFor(content));
    }
}
