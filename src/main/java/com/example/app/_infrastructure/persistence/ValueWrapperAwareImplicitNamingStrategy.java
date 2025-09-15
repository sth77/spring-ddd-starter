package com.example.app._infrastructure.persistence;

import lombok.val;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.springframework.util.StringUtils;

import java.util.List;

public class ValueWrapperAwareImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    private static final List<String> WRAPPED_FIELDS = List.of("stringValue", "intValue", "longValue", "booleanValue", "decimalValue", "uuidValue");

    @Override
    public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
        val property = source.getAttributePath().getProperty();
        val parent = source.getAttributePath().getParent().getFullPath();
        if (WRAPPED_FIELDS.contains(property) && StringUtils.hasText(parent)) {
            return toIdentifier(parent, source.getBuildingContext());
        }
        return super.determineBasicColumnName(source);
    }

}
