package com.example.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.app.common.model.Principal;
import com.example.app.sample.Sample.SampleId;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

/**
 * Verifies that jMolecules identifiers and single-attribute value objects are serialized to / deserialized
 * from their bare wrapped value by jmolecules-jackson, without any Jackson annotations on the types.
 */
@SpringBootTest
class WrapperSerializationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void identifier_serializesToBareUuidAndRoundTrips() {
        val id = SampleId.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        assertThat(objectMapper.writeValueAsString(id)).isEqualTo("\"11111111-1111-1111-1111-111111111111\"");
        assertThat(objectMapper.readValue("\"11111111-1111-1111-1111-111111111111\"", SampleId.class))
                .isEqualTo(id);
    }

    @Test
    void valueObject_serializesToBareStringAndRoundTrips() {
        val principal = Principal.of("alice");

        assertThat(objectMapper.writeValueAsString(principal)).isEqualTo("\"alice\"");
        assertThat(objectMapper.readValue("\"alice\"", Principal.class)).isEqualTo(principal);
    }
}
