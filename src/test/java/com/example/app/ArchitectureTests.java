package com.example.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.app.common.model.Command;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
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
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.MapKeyJoinColumns;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SecondaryTables;
import jakarta.persistence.Table;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.jmolecules.architecture.onion.simplified.ApplicationRing;
import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

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
            .because("enums are mapped by name globally by StringEnumTypeContributor, so no attribute needs"
                    + " @Enumerated - which would either break on constant reordering (ordinal default) or"
                    + " scatter persistence annotations over the domain model");

    @ArchTest
    static final ArchRule noEnumeratedMethods = noMethods()
            .should().beAnnotatedWith(Enumerated.class)
            .because("enums are mapped by name globally by StringEnumTypeContributor");

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
     * The rule above forbids the wrong annotation but says nothing about none at all, and an unannotated
     * operation is the more dangerous case: it is reachable by every authenticated user, and
     * {@code SecuredAggregateCommands} treats "no required role" as "allowed", so its HAL link is offered to
     * everyone as well. Read mappings are exempt - only state changes must name the role they require.
     */
    @ArchTest
    static final ArchRule stateChangingOperationsAreSecured = methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RepositoryRestController.class)
            .and().arePublic()
            .and(aStateChangingMapping())
            .should().beAnnotatedWith(Secured.class)
            .because("an operation without @Secured is silently open to every authenticated user and its HAL"
                    + " command link is shown to all of them");

    private static DescribedPredicate<JavaMethod> aStateChangingMapping() {
        return DescribedPredicate.describe(
                "annotated with a state-changing request mapping",
                method -> Stream.of(PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class)
                        .anyMatch(method::isAnnotatedWith));
    }

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

    /**
     * Annotations that pin a domain type to a physical schema name. The jMolecules byte-buddy transform adds the
     * structural JPA annotations ({@code @Entity}, {@code @Embeddable}, {@code @EmbeddedId}, ...) to the compiled
     * classes, so a blanket ban on {@code jakarta.persistence} would fail against the transformed bytecode; these
     * are the ones only a developer writes.
     *
     * <p>
     * The {@code @...Overrides} containers have to be listed next to the repeatable annotations they wrap: two
     * {@code @AttributeOverride}s on one attribute compile to a single {@code @AttributeOverrides}, which is not
     * annotated with the member type. {@code @AttributeOverride} matters most of all here - it is the one way to
     * rename the column of an embedded value object, so it is the escape hatch this rule exists to close.
     */
    private static final List<Class<? extends Annotation>> PHYSICAL_MAPPING_ANNOTATIONS = List.of(
            Table.class, SecondaryTable.class, SecondaryTables.class,
            Column.class, OrderColumn.class,
            JoinColumn.class, JoinColumns.class, JoinTable.class,
            CollectionTable.class,
            MapKeyColumn.class, MapKeyJoinColumn.class, MapKeyJoinColumns.class,
            AttributeOverride.class, AttributeOverrides.class,
            AssociationOverride.class, AssociationOverrides.class,
            PrimaryKeyJoinColumn.class, PrimaryKeyJoinColumns.class);

    /**
     * The physical schema is none of the domain's business: a column or table name belongs to the naming
     * strategies, which are also the one place where the mapping and the hand-written Flyway schema can be kept
     * in agreement. The usual reason to reach for {@code @Table} or {@code @Column} is a name that collides with
     * a SQL keyword ("order"); see the rule's message for the ways to solve that without annotating the model.
     */
    @ArchTest
    static void domainRingShouldNotNameSchemaObjects(JavaClasses classes) {
        val violations = classes.stream()
                .filter(ArchitectureTests::residesInDomainRing)
                .flatMap(c -> Stream.<HasAnnotations<?>>concat(Stream.of(c), c.getMembers().stream()))
                .flatMap(annotated -> PHYSICAL_MAPPING_ANNOTATIONS.stream()
                        .filter(annotated::isAnnotatedWith)
                        .map(annotation -> "%s is annotated with @%s - a physical schema name in the domain ring."
                                .formatted(annotated.getDescription(), annotation.getSimpleName())))
                .toList();
        assertThat(violations).as("violations - resolve a keyword collision by overriding Spring Boot's default"
                + " PhysicalNamingStrategy (CamelCaseToUnderscoresNamingStrategy), by enabling"
                + " hibernate.auto_quote_keyword, or in META-INF/orm.xml instead").isEmpty();
    }

    /**
     * A ring annotation on a package covers its subpackages too - that is why {@code sample.web} has to declare
     * {@code @InfrastructureRing} to escape the {@code @DomainRing} of {@code sample}. Testing the class's own
     * package alone would therefore exempt every future domain subpackage from the rule above, so walk outwards
     * until some ring claims the class.
     */
    private static boolean residesInDomainRing(JavaClass type) {
        for (var pkg = Optional.of(type.getPackage()); pkg.isPresent(); pkg = pkg.get().getParent()) {
            if (pkg.get().isAnnotatedWith(DomainRing.class)) {
                return true;
            }
            if (pkg.get().isAnnotatedWith(ApplicationRing.class)
                    || pkg.get().isAnnotatedWith(InfrastructureRing.class)) {
                return false;
            }
        }
        return false;
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
