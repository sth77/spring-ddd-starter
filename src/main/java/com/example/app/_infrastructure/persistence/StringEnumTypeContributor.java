package com.example.app._infrastructure.persistence;

import com.example.app.SampleApplication;
import jakarta.persistence.EnumType;
import java.util.Objects;
import lombok.val;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.java.EnumJavaType;
import org.hibernate.type.descriptor.jdbc.DelegatingJdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * Persists every enum of this application by name instead of by ordinal, declared once for the whole
 * model, so neither the aggregates nor an {@code AttributeConverter} per enum have to say so.
 *
 * <p>
 * Hibernate offers no "enums as string" setting - {@code hibernate.type.prefer_native_enum_types} only
 * picks a <em>native</em> enum SQL type once the style is already {@code STRING}, and the style itself
 * is populated from {@code @Enumerated} alone; unset means {@link EnumType#ORDINAL}, which silently
 * rewrites the meaning of stored data when constants are reordered. What Hibernate does offer is the
 * {@link TypeContributor} SPI, which runs before any entity is bound: registering a
 * {@link org.hibernate.type.descriptor.java.JavaType} for an enum there wins over the
 * {@link EnumJavaType} Hibernate would otherwise derive, and {@code InferredBasicValueResolver} asks
 * exactly that descriptor for its recommended JDBC type.
 *
 * <p>
 * A {@code TypeContributor} is the only hook early enough. Setting the enumeration style later - from an
 * {@code AdditionalMappingContributor}, say - has no effect: entity binding has already resolved every
 * {@code BasicValue} by then, and {@code BasicValue.setEnumerationStyle} is a plain field write that
 * does not invalidate that resolution.
 *
 * <p>
 * The enums are discovered by scanning this application's packages rather than listed here, which is
 * what keeps a new enum free of any declaration. Only classes whose metadata says they extend
 * {@link Enum} are loaded, and registering a descriptor for an enum that is never persisted (a logging
 * enum, say) costs nothing - the mapping model only consults the descriptors it actually needs. The
 * scan deliberately accepts every candidate: an enum whose constants have class bodies compiles to an
 * <em>abstract</em> class, which the default component filter rejects, and such an enum would then
 * silently fall back to ordinal mapping - a behavioural refactoring inside the domain model must not
 * change how it is stored.
 *
 * <p>
 * Registered through {@code META-INF/services/org.hibernate.boot.model.TypeContributor}, so Hibernate
 * picks it up wherever it bootstraps - including the narrowed context of an
 * {@code @ApplicationModuleTest}, which never scans this package for beans. An {@code @Enumerated}
 * annotation would still win over this default, which is why an ArchUnit rule forbids one: it belongs
 * to neither the domain model nor the ordinal-mapping trap.
 */
public class StringEnumTypeContributor implements TypeContributor {

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        val scanner = new ClassPathScanningCandidateComponentProvider(false) {

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                // the default check demands a concrete class, which an enum with constant class bodies is not
                return true;
            }
        };
        scanner.addIncludeFilter(new AssignableTypeFilter(Enum.class));

        scanner.findCandidateComponents(SampleApplication.class.getPackageName()).stream()
                .map(candidate -> Objects.requireNonNull(candidate.getBeanClassName()))
                .map(className -> ClassUtils.resolveClassName(className, null))
                // an enum constant's class body is an anonymous subclass, not an enum of its own
                .filter(Class::isEnum)
                .forEach(enumType -> typeContributions.contributeJavaType(nameBased(enumType)));
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> EnumJavaType<E> nameBased(Class<?> enumType) {
        return new NameBasedEnumJavaType<>((Class<E>) enumType);
    }

    /**
     * An enum descriptor that recommends the JDBC type Hibernate would pick for
     * {@link EnumType#STRING}, whatever the attribute says. Deriving it from Hibernate's own logic
     * rather than hard-coding {@code VARCHAR} keeps the dialect's nationalization and native-enum
     * decisions intact.
     */
    private static final class NameBasedEnumJavaType<E extends Enum<E>> extends EnumJavaType<E> {

        private NameBasedEnumJavaType(Class<E> enumType) {
            super(enumType);
        }

        @Override
        public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
            return super.getRecommendedJdbcType(new DelegatingJdbcTypeIndicators(indicators) {

                @Override
                public EnumType getEnumeratedType() {
                    return EnumType.STRING;
                }
            });
        }
    }
}
