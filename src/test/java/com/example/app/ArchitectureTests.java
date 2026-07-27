package com.example.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.app.common.model.Command;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.Enumerated;
import java.util.stream.Stream;
import lombok.val;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

/**
 * Executes the architecture rules against the production code. The {@link AnalyzeClasses} annotation is what
 * actually makes ArchUnit's JUnit engine pick up and run the {@link ArchTest} members below — without it the
 * rules are silently skipped.
 */
@AnalyzeClasses(packagesOf = SampleApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    @ArchTest
    static final ArchRule dddRules = JMoleculesDddRules.all();

    @ArchTest
    static final ArchRule onion = JMoleculesArchitectureRules.ensureOnionSimple();

    @ArchTest
    static final ArchRule noEnumeratedFields = noFields()
            .should().beAnnotatedWith(Enumerated.class)
            .because("enums are persisted through an AttributeConverter (extend common.persistence.EnumConverter);"
                    + " @Enumerated either breaks on constant reordering (ordinal default) or scatters persistence"
                    + " annotations over the domain model");

    @ArchTest
    static final ArchRule noEnumeratedMethods = noMethods()
            .should().beAnnotatedWith(Enumerated.class)
            .because("enums are persisted through an AttributeConverter (extend common.persistence.EnumConverter)");

    /**
     * {@code SecuredAggregateCommands} derives HAL command-link visibility from the {@code @Secured}
     * annotation on the operations-controller method handling the command. Any other security annotation
     * would be enforced at invocation time but stay invisible to the link layer, so links and
     * authorization would silently drift apart: the link is offered to every user, the call then fails.
     */
    @ArchTest
    static final ArchRule onlySecuredOnOperationsControllers = noMethods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RepositoryRestController.class)
            .should().beAnnotatedWith(PreAuthorize.class)
            .orShould().beAnnotatedWith(PostAuthorize.class)
            .orShould().beAnnotatedWith(PreFilter.class)
            .orShould().beAnnotatedWith(PostFilter.class)
            .orShould().beAnnotatedWith(RolesAllowed.class)
            .orShould().beAnnotatedWith(PermitAll.class)
            .orShould().beAnnotatedWith(DenyAll.class)
            .because("operations controllers must declare roles exclusively via @Secured, the single"
                    + " source SecuredAggregateCommands reads to decide HAL command-link visibility");

    /**
     * Aggregates change state exclusively through business operations that take a command: no setters, no
     * loose parameter lists. Read accessors (get/is), the {@code can(...)} guard and the {@code Object}
     * methods are exempt; static factories are covered by the sealed command pattern itself
     * ({@code create} takes the Create command).
     */
    @ArchTest
    static final ArchRule aggregateOperationsTakeCommands = methods()
            .that().areDeclaredInClassesThat().areAssignableTo(AggregateRoot.class)
            .and().arePublic()
            .and().areNotStatic()
            .and(DescribedPredicate.not(accessorGuardOrObjectMethod()))
            .should(takeExactlyOneCommandParameter())
            .because("aggregate state changes go through commands only (see INSTRUCTIONS.md,"
                    + " 'Adding business operations')");

    private static DescribedPredicate<JavaMethod> accessorGuardOrObjectMethod() {
        return DescribedPredicate.describe(
                "a get/is accessor, the can() guard, or an Object method",
                method -> method.getName().startsWith("get")
                        || method.getName().startsWith("is")
                        || method.getName().equals("can")
                        || method.getName().equals("toString")
                        || method.getName().equals("equals")
                        || method.getName().equals("hashCode"));
    }

    private static ArchCondition<JavaMethod> takeExactlyOneCommandParameter() {
        return new ArchCondition<>("take the operation's command as their only parameter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                val parameters = method.getRawParameterTypes();
                val satisfied = parameters.size() == 1 && parameters.get(0).isAssignableTo(Command.class);
                events.add(new SimpleConditionEvent(
                        method,
                        satisfied,
                        method.getFullName()
                                + (satisfied
                                        ? " takes its command as the only parameter"
                                        : " must take its command as the only parameter")));
            }
        };
    }

    @ArchTest
    static void packagesShouldBeAnnotatedWithNullMarked(JavaClasses classes) {
        val rootPackage = classes.getPackage(SampleApplication.class.getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream()
                        .anyMatch(not(c -> c.getSimpleName().equals("package-info"))))
                .filter(not(p -> p.isAnnotatedWith(NullMarked.class)))
                .map(p -> p.getDescription() + " is not annotated with @NullMarked");
        assertThat(violations).as("violations").isEmpty();
    }

    @ArchTest
    static void emptyPackagesShouldNotBeAnnotatedWithNullMarked(JavaClasses classes) {
        val rootPackage = classes.getPackage(SampleApplication.class.getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream().allMatch(c -> c.getSimpleName().equals("package-info")))
                .filter(p -> p.isAnnotatedWith(NullMarked.class))
                .map(p -> p.getDescription() + " is unnecessarily annotated with @NullMarked");
        assertThat(violations).as("violations").isEmpty();
    }
}
