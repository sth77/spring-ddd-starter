package com.example.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import lombok.val;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;

import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

@AnalyzeClasses(packages = "com.example.app")
public class NullabilityAnnotationTests {

    @ArchTest
    void packagesShouldBeAnnotatedWithNonNulls(JavaClasses classes) {
        val rootPackage = classes.getPackage(getClass().getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream().anyMatch(not(c -> c.getSimpleName().equals("package-info"))))
                .filter(not(p -> p.isAnnotatedWith(NonNullApi.class)))
                .filter(not(p -> p.isAnnotatedWith(NonNullFields.class)))
                .map(p -> p.getDescription() + " is not annotated with both NonNullApi and NonNullFields");
        assertThat(violations).as("violations").isEmpty();
    }

    @ArchTest
    void emptyPackagesShouldNotBeAnnotatedWithNonNulls(JavaClasses classes) {
        val rootPackage = classes.getPackage(getClass().getPackageName());
        val violations = Stream.concat(Stream.of(rootPackage), rootPackage.getSubpackagesInTree().stream())
                .filter(p -> p.getClasses().stream().allMatch(c -> c.getSimpleName().equals("package-info")))
                .filter(p -> p.isAnnotatedWith(NonNullApi.class) || p.isAnnotatedWith(NonNullFields.class))
                .map(p -> p.getDescription() + " is unnecessarily annotated with NonNullApi and/or NonNullFields");
        assertThat(violations).as("violations").isEmpty();
    }

}
