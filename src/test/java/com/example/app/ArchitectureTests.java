package com.example.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import lombok.val;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

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
    static void packagesShouldBeAnnotatedWithNullMarked(JavaClasses classes) {
        val rootPackage = classes.getPackage(SampleApplication.class.getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream().anyMatch(not(c -> c.getSimpleName().equals("package-info"))))
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
