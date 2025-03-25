package com.example.app.domain.common.model;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.hateoas.server.core.Relation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public final class CommandHelper<A extends AggregateRoot<?, ?>, C extends Command, W> {

    private final Class<A> aggregateType;
    private final Class<C> commandType;
    private final Class<W> controllerType;

    public List<Class<C>> getCommands() {
        return Arrays.stream(commandType.getPermittedSubclasses())
                .map(it -> (Class<C>) it)
                .toList();
    }

    public String getRel(Class<? extends C> commandType) {
        val annotatedRel = Optional.ofNullable(commandType.getAnnotation(Relation.class));
        return annotatedRel
                .map(Relation::value)
                .orElseGet(() -> extractRelFromController(commandType)
                        .orElseGet(() -> extractRelFromCommandType(commandType)));
    }

    private Optional<String> extractRelFromController(Class<? extends C> commandType) {
        return Arrays.stream(controllerType.getDeclaredMethods())
                .filter(method -> method.getParameterCount() <= 2)
                .filter(method -> Identifier.class.isAssignableFrom(method.getParameters()[0].getType()))
                .filter(method -> commandType.equals(method.getParameters()[1].getType()))
                .map(Method::getName)
                .findFirst();
    }

    private String extractRelFromCommandType(Class<? extends C> commandType) {
        val commandName = commandType.getSimpleName().replace(aggregateType.getSimpleName(), "");
        return !commandName.isEmpty()
                ? commandName
                : commandType.getSimpleName();
    }

}
