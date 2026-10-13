package com.example.app.common.web;

import java.util.IdentityHashMap;
import java.util.Map;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped handoff of the command links of child entities, from the aggregate's link processor to the
 * {@link ChildEntityLinksProcessor} that renders them on the nested child.
 *
 * <p>
 * Spring Data REST renders a child entity inline and invokes the representation model processors on it, but such a
 * processor only sees the child - not the aggregate root whose state decides which commands the child allows. The
 * ordering of a request bridges that gap:
 * <ol>
 * <li>Before message conversion, the return value handler invokes the processors of the root
 * ({@code EntityModel<Sample>}; for a collection, of each element). The aggregate's child link processor (e.g.
 * {@code SampleItemLinks}) computes each child's allowed links and {@link #put stores} them here.
 * <li>Jackson then serializes the root. Spring Data REST's {@code NestedEntitySerializer} wraps every child entity in
 * an entity model and invokes the processors again, which lets {@link ChildEntityLinksProcessor} look up the links
 * stored for that child.
 * </ol>
 * The registry is keyed by object identity, since the serialized child is the very instance the root processor saw.
 */
@Component
@RequestScope
public class ChildEntityLinks {

    private final Map<Object, Links> linksByChild = new IdentityHashMap<>();

    /**
     * Stores the complete set of links of a child, replacing any earlier one: a root processor may run more than
     * once per request, and always computes the full set.
     */
    public void put(Object child, Iterable<Link> links) {
        linksByChild.put(child, Links.of(links));
    }

    Links linksFor(Object child) {
        return linksByChild.getOrDefault(child, Links.NONE);
    }
}
