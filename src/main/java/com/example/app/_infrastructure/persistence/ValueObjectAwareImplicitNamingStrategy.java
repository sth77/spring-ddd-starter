package com.example.app._infrastructure.persistence;

import lombok.val;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.boot.model.source.spi.AttributePath;

import java.util.ArrayDeque;

/**
 * Implicit column naming for embedded value objects:
 * <ul>
 *   <li>Multi-field embeddables inlined into the owning table keep the owning-attribute prefix
 *       ({@code name.en} → {@code name_en}), disambiguating sibling embeddables.
 *   <li>{@code @ElementCollection} elements live in their own table, so the collection-attribute
 *       prefix is dropped and columns are named by the path within the element.
 *   <li>Single-value wrappers collapse to the owning attribute, dropping the wrapper component
 *       ({@code id.uuidValue} → {@code id}).
 * </ul>
 *
 * <p>The single-value test is name-based: the implicit-naming SPI exposes only the attribute path,
 * never the field type or arity, so single-value wrappers are recognised by their sole component
 * following the {@code <type>Value} convention (e.g. {@code uuidValue}, {@code stringValue}).
 * Multi-field value objects use semantic field names, which never end in {@code Value}.
 */
public class ValueObjectAwareImplicitNamingStrategy extends ImplicitNamingStrategyComponentPathImpl {

    private static final String WRAPPER_COMPONENT_SUFFIX = "Value";

    @Override
    public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
        val path = pathWithinCollectionElement(source.getAttributePath());
        val parent = path.getParent();
        if (path.getProperty().endsWith(WRAPPER_COMPONENT_SUFFIX) && hasText(parent)) {
            return toIdentifier(transformAttributePath(parent), source.getBuildingContext());
        }
        return toIdentifier(transformAttributePath(path), source.getBuildingContext());
    }

    /**
     * For an {@code @ElementCollection} element, returns the sub-path within the element (the segments
     * below Hibernate's collection-element marker), dropping the collection-attribute prefix. For an
     * inlined embeddable (no marker) the path is returned unchanged.
     */
    private static AttributePath pathWithinCollectionElement(AttributePath path) {
        val withinElement = new ArrayDeque<String>();
        for (AttributePath p = path; hasText(p); p = p.getParent()) {
            if (p.isCollectionElement()) {
                return withinElement.isEmpty() ? path : reroot(withinElement);
            }
            withinElement.addFirst(p.getProperty());
        }
        return path;
    }

    private static AttributePath reroot(Iterable<String> segments) {
        AttributePath path = null;
        for (String segment : segments) {
            path = (path == null) ? AttributePath.parse(segment) : path.append(segment);
        }
        return path;
    }

    /**
     * Joins the camelCase path segments with {@code '_'}, trimming any trailing underscore a segment
     * carries (e.g. a {@code break_} component becomes {@code break}). The physical naming strategy
     * then snake-cases each segment.
     */
    @Override
    protected String transformAttributePath(AttributePath attributePath) {
        val segments = new ArrayDeque<String>();
        for (AttributePath p = attributePath; hasText(p); p = p.getParent()) {
            var segment = p.getProperty();
            if (segment.startsWith("{")) {
                return super.transformAttributePath(attributePath);
            }
            while (segment.endsWith("_")) {
                segment = segment.substring(0, segment.length() - 1);
            }
            segments.addFirst(segment);
        }
        return String.join("_", segments);
    }

    private static boolean hasText(AttributePath path) {
        return path != null && path.getProperty() != null && !path.getProperty().isEmpty();
    }
}
