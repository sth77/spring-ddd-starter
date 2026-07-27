package com.example.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Enumerated;
import java.util.stream.Stream;
import lombok.val;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.jspecify.annotations.NullMarked;

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
