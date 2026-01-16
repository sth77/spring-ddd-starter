package com.example.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import lombok.val;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

public class NullabilityAnnotationTests {

    @ArchTest
    void packagesShouldBeAnnotatedWithNullMarked(JavaClasses classes) {
        val rootPackage = classes.getPackage(getClass().getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream().anyMatch(not(c -> c.getSimpleName().equals("package-info"))))
                .filter(not(p -> p.isAnnotatedWith(NullMarked.class)))
                .map(p -> p.getDescription() + " is not annotated with @NullMarked");
        assertThat(violations).as("violations").isEmpty();
    }

    @ArchTest
    void emptyPackagesShouldNotBeAnnotatedWithNullMarked(JavaClasses classes) {
        val rootPackage = classes.getPackage(getClass().getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream().allMatch(c -> c.getSimpleName().equals("package-info")))
                .filter(p -> p.isAnnotatedWith(NullMarked.class))
                .map(p -> p.getDescription() + " is unnecessarily annotated with @NullMarked");
        assertThat(violations).as("violations").isEmpty();
    }

}
