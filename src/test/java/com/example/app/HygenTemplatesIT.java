package com.example.app;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Integration test that validates hygen-generated code.
 *
 * Run with: mvn verify -Phygen-it
 *
 * This test:
 * 1. Requires hygen to generate the world/Human aggregate with an added Archive operation, and the Country reference
 * data (pre-integration-test phase, where Country is also wired into Human as its citizenship)
 * 2. Tests the full aggregate lifecycle via REST API, including the master data it refers to
 * 3. Verifies HAL links change based on state, and that HAL-FORMS offers a template per command link
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HygenTemplatesIT {

    private static final RequestPostProcessor USER = user("testuser").roles("USER", "ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String countryLocation;
    private static String humanLocation;
    private static String updateLink;
    private static String publishLink;
    private static String archiveLink;

    /**
     * Master data is served read-only, so the row is seeded through the schema the generated migration
     * created rather than over the API.
     */
    @Test
    @Order(1)
    void listCountries_shouldServeSeededCountryUnderAFollowableSelfLink() throws Exception {
        jdbcTemplate.update("INSERT INTO country (id, name_en, name_de) VALUES (?, ?, ?)",
                UUID.randomUUID(), "Switzerland", "Schweiz");

        String collection = mockMvc.perform(get("/api/countries").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.countries[0].name.en").value("Switzerland"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        countryLocation = JsonPath.read(collection, "$._embedded.countries[0]._links.self.href");

        mockMvc.perform(get(URI.create(countryLocation)).with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name.de").value("Schweiz"));
    }

    @Test
    @Order(2)
    void createHuman_shouldReturnDraftWithUpdateAndPublishLinks() throws Exception {
        String response = mockMvc.perform(post("/api/humans")
                .with(USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "H1", "country": "%s"}
                        """.formatted(URI.create(countryLocation).getPath())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("H1"))
                .andExpect(jsonPath("$.state").value("DRAFT"))
                // the aggregate embeds a copy of the referenced master data
                .andExpect(jsonPath("$.citizenship.en").value("Switzerland"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.publish.href").exists())
                // added by `hygen operation new Archive`
                .andExpect(jsonPath("$._links.archive.href").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract links for subsequent tests
        humanLocation = JsonPath.read(response, "$._links.self.href");
        updateLink = JsonPath.read(response, "$._links.update.href");
        publishLink = JsonPath.read(response, "$._links.publish.href");
        archiveLink = JsonPath.read(response, "$._links.archive.href");
    }

    /**
     * The generated link processors attach a form to every command link, so a HAL-FORMS client gets a template per
     * command, keyed by the same rel as the link.
     */
    @Test
    @Order(3)
    void getHumanAsHalForms_shouldOfferATemplatePerCommandLink() throws Exception {
        mockMvc.perform(get(URI.create(humanLocation))
                .with(USER)
                .accept(MediaTypes.HAL_FORMS_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._templates.update.method").value("POST"))
                .andExpect(jsonPath("$._templates.update.target").value(updateLink))
                .andExpect(jsonPath("$._templates.update.properties[?(@.name == 'name')].required").value(true))
                .andExpect(jsonPath("$._templates.publish.method").value("POST"))
                .andExpect(jsonPath("$._templates.publish.target").value(publishLink))
                .andExpect(jsonPath("$._templates.archive.target").value(archiveLink));
    }

    @Test
    @Order(4)
    void updateHuman_shouldReturnUpdatedNameWithLinks() throws Exception {
        mockMvc.perform(post(updateLink)
                .with(USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "H1'"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("H1'"))
                .andExpect(jsonPath("$.state").value("DRAFT"))
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.publish.href").exists());
    }

    /** The operation added by the operation generator is routed, guarded and linked like the generated ones. */
    @Test
    @Order(5)
    void archiveHuman_followedLink_shouldBeExecuted() throws Exception {
        mockMvc.perform(post(archiveLink)
                .with(USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DRAFT"))
                .andExpect(jsonPath("$._links.archive.href").value(archiveLink));
    }

    @Test
    @Order(6)
    void publishHuman_shouldReturnPublishedWithOnlySelfLink() throws Exception {
        mockMvc.perform(post(publishLink)
                .with(USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update").doesNotExist())
                .andExpect(jsonPath("$._links.publish").doesNotExist())
                .andExpect(jsonPath("$._links.archive").doesNotExist());
    }
}
