package com.example.app._infrastructure.persistence;

import lombok.val;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;

public class EmbeddedPrefixImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    @Override
    public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
        val columnName = source.getAttributePath().getFullPath()
                .replace('.', '_');
        if (columnName.startsWith("id_")) {
            return super.determineBasicColumnName(source);
        }
        return toIdentifier(columnName, source.getBuildingContext());
    }

}
