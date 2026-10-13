package com.example.app.common.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.Named;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Turns a command link into a form: attaches a {@code POST} affordance whose input is the command record, so that a
 * HAL-FORMS response ({@code application/prs.hal-forms+json}) carries a {@code _templates} entry next to the link.
 * The template is named after the link's rel, which makes the two addressable by the same key; its target is the link
 * itself, and its properties are derived from the record components and their Bean Validation constraints.
 *
 * <p>
 * Plain HAL ignores affordances, so a link passed through here renders exactly as before. It takes the finished link
 * and therefore works the same for aggregate, collection and child entity links.
 */
public final class CommandLinks {

    private CommandLinks() {
    }

    public static Link afford(Link link, Class<?> commandType) {
        return Affordances.of(link)
                .afford(HttpMethod.POST)
                .withInput(new CommandPayloadMetadata(PropertyUtils.getExposedProperties(commandType)))
                .withName(link.getRel().value())
                .toLink();
    }

    /**
     * Spring HATEOAS reads the input properties of a type as JavaBean properties, which misdescribes a command record
     * in two ways that {@link CommandPropertyMetadata} corrects:
     * <ul>
     * <li>A record component has no setter - Jackson writes it through the canonical constructor - so every property
     * would be rendered {@code readOnly}, i.e. as a form a client must not fill in.
     * <li>{@code @Size} on a text is rendered as a numeric {@code min}/{@code max} of {@code type: range}; HAL-FORMS
     * describes the length of a text by {@code minLength}/{@code maxLength}.
     * </ul>
     */
    private record CommandPayloadMetadata(InputPayloadMetadata delegate) implements InputPayloadMetadata {

        @Override
        public Stream<PropertyMetadata> stream() {
            return delegate.stream().map(CommandPropertyMetadata::new);
        }

        @Override
        public <T extends Named> T customize(T target, Function<PropertyMetadata, T> customizer) {
            Map<String, PropertyMetadata> properties = stream()
                    .collect(Collectors.toMap(PropertyMetadata::getName, Function.identity()));
            return Optional.ofNullable(properties.get(target.getName())).map(customizer).orElse(target);
        }

        @Override
        public List<String> getI18nCodes() {
            return delegate.getI18nCodes();
        }

        @Override
        public InputPayloadMetadata withMediaTypes(List<MediaType> mediaTypes) {
            return new CommandPayloadMetadata(delegate.withMediaTypes(mediaTypes));
        }

        @Override
        public List<MediaType> getMediaTypes() {
            return delegate.getMediaTypes();
        }

        @Override
        public @Nullable Class<?> getType() {
            // the HAL-FORMS options registered for a command are looked up by this type
            return delegate.getType();
        }
    }

    private record CommandPropertyMetadata(PropertyMetadata delegate) implements PropertyMetadata {

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public boolean isRequired() {
            return delegate.isRequired();
        }

        @Override
        public boolean isReadOnly() {
            return false; // a command is an input payload: each of its components is filled in by the client
        }

        @Override
        public Optional<String> getPattern() {
            return delegate.getPattern();
        }

        @Override
        public ResolvableType getType() {
            return delegate.getType();
        }

        @Override
        public @Nullable Number getMin() {
            return isText() ? null : delegate.getMin();
        }

        @Override
        public @Nullable Number getMax() {
            return isText() ? null : delegate.getMax();
        }

        @Override
        public @Nullable Long getMinLength() {
            // @Size(min = 0) is the default and no constraint at all
            return isText() && delegate.getMinLength() == null
                    ? Optional.ofNullable(delegate.getMin()).map(Number::longValue).filter(min -> min > 0).orElse(null)
                    : delegate.getMinLength();
        }

        @Override
        public @Nullable Long getMaxLength() {
            return isText() && delegate.getMaxLength() == null
                    ? Optional.ofNullable(delegate.getMax()).map(Number::longValue).orElse(null)
                    : delegate.getMaxLength();
        }

        @Override
        public @Nullable String getInputType() {
            val inputType = delegate.getInputType();
            return isText() && "range".equals(inputType) ? "text" : inputType;
        }

        private boolean isText() {
            return CharSequence.class.isAssignableFrom(delegate.getType().resolve(Object.class));
        }
    }
}
