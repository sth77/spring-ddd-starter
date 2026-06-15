package com.example.app._infrastructure.persistence;

import lombok.val;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins the {@link ValueObjectAwareImplicitNamingStrategy} naming matrix, independent of any schema.
 * The strategy produces the implicit identifier; the physical {@link CamelCaseToUnderscoresNamingStrategy}
 * then snake-cases it. Both are composed so assertions are the exact column names that land in the database.
 */
class ValueObjectAwareImplicitNamingStrategyTest {

    private static final CamelCaseToUnderscoresNamingStrategy PHYSICAL = new CamelCaseToUnderscoresNamingStrategy();

    private final ValueObjectAwareImplicitNamingStrategy strategy = new ValueObjectAwareImplicitNamingStrategy();
    private MetadataBuildingContext buildingContext;

    @BeforeEach
    void setUp() {
        val helper = mock(IdentifierHelper.class);
        when(helper.toIdentifier(anyString())).thenAnswer(i -> Identifier.toIdentifier(i.getArgument(0)));
        val jdbcEnvironment = mock(JdbcEnvironment.class);
        when(jdbcEnvironment.getIdentifierHelper()).thenReturn(helper);
        val database = mock(Database.class);
        when(database.getJdbcEnvironment()).thenReturn(jdbcEnvironment);
        val collector = mock(InFlightMetadataCollector.class);
        when(collector.getDatabase()).thenReturn(database);
        buildingContext = mock(MetadataBuildingContext.class);
        when(buildingContext.getMetadataCollector()).thenReturn(collector);
    }

    // Embeddables inlined into the owning table: keep the owning-attribute path prefix.

    @Test
    void inlineMultiFieldEmbeddable_getsPathPrefix() {
        assertThat(columnFor("name.en")).isEqualTo("name_en");
    }

    @Test
    void inlineNestedEmbeddable_getsFullPathPrefix() {
        assertThat(columnFor("city.name.en")).isEqualTo("city_name_en");
    }

    @Test
    void inlineNestedEmbeddable_trimsTrailingUnderscoreSegment() {
        assertThat(columnFor("city.break_.en")).isEqualTo("city_break_en");
    }

    @Test
    void inlineSingleValueWrapper_collapsesToOwningAttribute() {
        assertThat(columnFor("owner.uuidValue")).isEqualTo("owner");
    }

    @Test
    void inlineIntValueWrapper_collapsesToOwningAttribute() {
        assertThat(columnFor("postalCode.intValue")).isEqualTo("postal_code");
    }

    @Test
    void rootEmbeddedId_collapsesToTheIdField() {
        assertThat(columnFor("id.uuidValue")).isEqualTo("id");
    }

    @Test
    void trulyParentlessWrapper_isNotCollapsed() {
        assertThat(columnFor("uuidValue")).isEqualTo("uuid_value");
    }

    // @ElementCollection elements live in their own table: drop the collection-attribute prefix.

    @Test
    void collectionElementField_dropsCollectionPrefix() {
        assertThat(columnFor("addresses.{element}.street")).isEqualTo("street");
    }

    @Test
    void collectionElementCamelField_dropsPrefixThenSnakeCases() {
        assertThat(columnFor("items.{element}.firstName")).isEqualTo("first_name");
    }

    @Test
    void collectionElementIdWrapper_dropsPrefixThenCollapses() {
        assertThat(columnFor("owners.{element}.ownerId.uuidValue")).isEqualTo("owner_id");
    }

    @Test
    void collectionElementNestedEmbeddable_keepsInElementPathOnly() {
        assertThat(columnFor("addresses.{element}.geo.latitude")).isEqualTo("geo_latitude");
    }

    private String columnFor(String attributePath) {
        val source = mock(ImplicitBasicColumnNameSource.class);
        when(source.getAttributePath()).thenReturn(AttributePath.parse(attributePath));
        when(source.getBuildingContext()).thenReturn(buildingContext);
        val implicit = strategy.determineBasicColumnName(source);
        return PHYSICAL.toPhysicalColumnName(implicit, null).getText();
    }
}
