package com.example.app;

import lombok.val;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AggregateEvents {

    private AggregateEvents() { }

    @SuppressWarnings("unchecked")
    public static List<DomainEvent> getEvents(AbstractAggregateRoot<?> aggregate) {
        try {
            val method = AbstractAggregateRoot.class.getDeclaredMethod("domainEvents");
            method.setAccessible(true);
            return new ArrayList<>((Collection<DomainEvent>) method.invoke(aggregate));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void clearEvents(AbstractAggregateRoot<?> aggregate) {
        try {
            val method = AbstractAggregateRoot.class.getDeclaredMethod("clearDomainEvents");
            method.setAccessible(true);
            method.invoke(aggregate);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}