package com.example.app.common.web;

import com.example.app.common.model.AggregateCommands;
import com.example.app.common.model.Command;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A role-aware view over an aggregate's {@link AggregateCommands}. It composes the plain command/relation
 * metadata and adds, scanned once at construction, the {@code @Secured} roles required by the operations
 * controller method that handles each command (matched by the command parameter type).
 *
 * <p>
 * {@link #getAllowedCommands()} returns the commands the current user may invoke; only the per-request
 * check of the current user's authorities happens at call time. Spring HATEOAS does not derive link
 * visibility from Spring Security, so this bridges the two, keeping the controller's {@code @Secured}
 * annotation as the single source of truth for the required role.
 *
 * <p>
 * Lives in the infrastructure ring's shared {@code common.web} named interface so feature web packages
 * can use it without forming a module cycle.
 *
 * @param <A>
 *            the aggregate type
 * @param <C>
 *            the sealed command parent interface of the aggregate
 */
public final class SecuredAggregateCommands<A extends AggregateRoot<?, ?>, C extends Command> {

    private final AggregateCommands<A, C> commands;
    private final Set<Class<C>> handledCommands;
    private final Map<Class<C>, Set<String>> requiredRolesByCommand;

    public SecuredAggregateCommands(Class<A> aggregateType, Class<C> commandType, Class<?> operationsControllerType) {
        this.commands = new AggregateCommands<>(aggregateType, commandType);
        this.handledCommands = commands.getCommands().stream()
                .filter(command -> Arrays.stream(operationsControllerType.getDeclaredMethods())
                        .anyMatch(method -> handles(method, command)))
                .collect(Collectors.toSet());
        this.requiredRolesByCommand = commands.getCommands().stream()
                .collect(Collectors.toMap(
                        Function.identity(), command -> resolveRequiredRoles(operationsControllerType, command)));
    }

    public List<Class<C>> getCommands() {
        return commands.getCommands();
    }

    public String getRel(Class<? extends C> commandType) {
        return commands.getRel(commandType);
    }

    /**
     * @return the commands the current user is allowed to invoke through the REST API, i.e. those the
     *         operations controller declares a handler method for, and without a role restriction or whose
     *         required role the authenticated user holds. Commands the controller does not expose (internal
     *         commands only executed by the application itself) are never offered.
     */
    public List<Class<C>> getAllowedCommands() {
        return commands.getCommands().stream()
                .filter(handledCommands::contains)
                .filter(this::isAllowedForCurrentUser)
                .toList();
    }

    public boolean isAllowedForCurrentUser(Class<? extends C> commandType) {
        val requiredRoles = requiredRolesByCommand.getOrDefault(commandType, Set.of());
        if (requiredRoles.isEmpty()) {
            return true;
        }
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        val heldAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return requiredRoles.stream().anyMatch(heldAuthorities::contains);
    }

    /**
     * Resolves the roles required for a command from the {@code @Secured} annotation of the handler method.
     * {@code Class#getDeclaredMethods()} returns methods in no particular order, so picking any one of several
     * annotated handlers would make link visibility depend on JVM internals. Disagreeing handlers are a
     * configuration error and fail fast at construction, i.e. at application startup.
     */
    private Set<String> resolveRequiredRoles(Class<?> operationsControllerType, Class<? extends C> commandType) {
        val roleSets = Arrays.stream(operationsControllerType.getDeclaredMethods())
                .filter(method -> handles(method, commandType))
                .map(method -> method.getAnnotation(Secured.class))
                .filter(Objects::nonNull)
                // Set.copyOf, not Set.of: the latter rejects a repeated role in @Secured({"ROLE_A", "ROLE_A"})
                .map(secured -> Set.copyOf(Arrays.asList(secured.value())))
                .distinct()
                .toList();
        if (roleSets.size() > 1) {
            throw new IllegalStateException(("%s declares handlers for %s requiring different roles (%s);"
                    + " the roles a command requires must be unambiguous")
                    .formatted(operationsControllerType.getName(), commandType.getSimpleName(), roleSets));
        }
        return roleSets.isEmpty() ? Set.of() : roleSets.getFirst();
    }

    /**
     * A method handles a command when it declares exactly that command as a parameter. The match is on the
     * exact type, not assignability: a parameter typed as the sealed command interface (or as {@code Object})
     * would otherwise claim every command of the aggregate and impose its role on all of them.
     */
    private boolean handles(Method method, Class<? extends C> commandType) {
        return Arrays.stream(method.getParameterTypes()).anyMatch(parameterType -> parameterType.equals(commandType));
    }
}
