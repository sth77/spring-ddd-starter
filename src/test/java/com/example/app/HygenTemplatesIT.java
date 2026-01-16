package com.example.app;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test that validates hygen-generated code.
 *
 * Run with: mvn verify -Phygen-it
 *
 * This test:
 * 1. Requires hygen to generate fleet/Airplane code (pre-integration-test phase)
 * 2. Tests the full aggregate lifecycle via REST API
 * 3. Verifies HAL links change based on state
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HygenTemplatesIT {

    @Autowired
    private MockMvc mockMvc;

    private static String airplaneLocation;
    private static String updateLink;
    private static String publishLink;

    @Test
    @Order(1)
    void createAirplane_shouldReturnDraftWithUpdateAndPublishLinks() throws Exception {
        String response = mockMvc.perform(post("/api/airplanes")
                .with(user("testuser").roles("USER", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "P1"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("P1"))
            .andExpect(jsonPath("$.state").value("DRAFT"))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.update.href").exists())
            .andExpect(jsonPath("$._links.publish.href").exists())
            .andReturn().getResponse().getContentAsString();

        // Extract links for subsequent tests
        airplaneLocation = JsonPath.read(response, "$._links.self.href");
        updateLink = JsonPath.read(response, "$._links.update.href");
        publishLink = JsonPath.read(response, "$._links.publish.href");
    }

    @Test
    @Order(2)
    void updateAirplane_shouldReturnUpdatedNameWithLinks() throws Exception {
        mockMvc.perform(post(updateLink)
                .with(user("testuser").roles("USER", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "P1'"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("P1'"))
            .andExpect(jsonPath("$.state").value("DRAFT"))
            .andExpect(jsonPath("$._links.update.href").exists())
            .andExpect(jsonPath("$._links.publish.href").exists());
    }

    @Test
    @Order(3)
    void publishAirplane_shouldReturnPublishedWithOnlySelfLink() throws Exception {
        mockMvc.perform(post(publishLink)
                .with(user("testuser").roles("USER", "ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("PUBLISHED"))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.update").doesNotExist())
            .andExpect(jsonPath("$._links.publish").doesNotExist());
    }
}
