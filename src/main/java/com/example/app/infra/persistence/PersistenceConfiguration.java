package com.example.app.infra.persistence;

import com.example.app.common.model.Email;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Configuration
@AllArgsConstructor
public class PersistenceConfiguration {

    protected @NonNull List<?> userConverters() {
        return List.of(
                new EmailConverter());
    }

    private static class EmailConverter implements Converter<Email, String> {

        @Override
        public String convert(@NonNull Email source) {
            return source.stringValue();
        }
    }
}