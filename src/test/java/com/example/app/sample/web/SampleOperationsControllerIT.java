package com.example.app.sample.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand;
import com.example.app.referencedata.Cities;
import com.example.app.referencedata.City;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.Samples;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Integration tests for {@link SampleOperationsController}.
 *
 * Tests cover:
 * - Security validation (authentication required for all endpoints)
 * - Publish operation with proper authentication
 * - State machine constraints
 * - Error handling for non-existent resources
 */
@SpringBootTest
@AutoConfigureMockMvc
class SampleOperationsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Samples samples;

    @Autowired
    private Cities cities;

    @Autowired
    private People people;

    private Person testOwner;

    @BeforeEach
    void setUp() {
        testOwner = people.save(Person.create(PersonCommand.CreatePerson.builder()
                .name("Test Owner " + UUID.randomUUID())
                .build()));
    }

    @Nested
    @DisplayName("Security Tests - Unauthenticated Access")
    class UnauthenticatedAccessTests {

        @Test
        @DisplayName("GET /samples without authentication should return 401 Unauthorized")
        void getSamples_withoutAuthentication_returnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/samples")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /samples without authentication should return 401 Unauthorized")
        void create_withoutAuthentication_returnsUnauthorized() throws Exception {
            mockMvc.perform(post("/api/samples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /samples/{id}/update without authentication should return 401 Unauthorized")
        void update_withoutAuthentication_returnsUnauthorized() throws Exception {
            mockMvc.perform(post("/api/samples/" + UUID.randomUUID() + "/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /samples/{id}/publish without authentication should return 401 Unauthorized")
        void publish_withoutAuthentication_returnsUnauthorized() throws Exception {
            mockMvc.perform(post("/api/samples/" + UUID.randomUUID() + "/publish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /samples/{id} without authentication should return 401 Unauthorized")
        void getSample_withoutAuthentication_returnsUnauthorized() throws Exception {
            Sample sample = createDraftSample();

            mockMvc.perform(get("/api/samples/" + sample.getId().uuidValue())).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Security Tests - Authenticated Access")
    class AuthenticatedAccessTests {

        @Test
        @DisplayName("GET /samples with authentication should return 200 OK")
        void getSamples_withAuthentication_returnsOk() throws Exception {
            mockMvc.perform(get("/api/samples").with(user("testuser").roles("USER")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /samples/{id} with authentication should return sample")
        void getSample_withAuthentication_returnsSample() throws Exception {
            Sample sample = createDraftSample();

            mockMvc.perform(get("/api/samples/" + sample.getId().uuidValue())
                    .with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("DRAFT"));
        }
    }

    @Nested
    @DisplayName("Create Sample Tests")
    class CreateSampleTests {

        @Test
        @DisplayName("POST /samples resolves owner and city from their URIs and denormalizes the owner name")
        void create_withOwnerAndCityUris_resolvesReferencesAndCopiesData() throws Exception {
            City city = cities.save(City.ofPostalCodeAndName(
                    3000, I18nText.builder().en("Bern").de("Bern").build()));

            mockMvc.perform(post("/api/samples")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": { "en": "Sample 1", "de": "DE_Sample 1" },
                                "description": "Sample description",
                                "owner": "/api/people/%s",
                                "city": "/api/cities/%s"
                            }
                            """.formatted(
                            testOwner.getId().uuidValue(), city.getId().uuidValue())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name.en").value("Sample 1"))
                    .andExpect(jsonPath("$.ownerName").value(testOwner.getName()))
                    .andExpect(jsonPath("$.city.postalCode").value(3000))
                    .andExpect(jsonPath("$.city.name.en").value("Bern"));
        }
    }

    @Nested
    @DisplayName("Update Sample Tests")
    class UpdateSampleTests {

        @Test
        @DisplayName("POST /samples/{id}/update for non-existent sample should return 404")
        void update_nonExistentSample_returnsNotFound() throws Exception {
            mockMvc.perform(post("/api/samples/" + UUID.randomUUID() + "/update")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validUpdateSampleJson()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /samples/{id}/update without name should return 400 Bad Request")
        void update_withoutName_returnsBadRequest() throws Exception {
            Sample sample = createDraftSample();

            mockMvc.perform(post("/api/samples/" + sample.getId().uuidValue() + "/update")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "description": "Updated description without name"
                            }
                            """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Publish Sample Tests")
    class PublishSampleTests {

        @Test
        @DisplayName("POST /samples/{id}/publish should transition sample to PUBLISHED state")
        void publish_draftSample_returnsPublishedSample() throws Exception {
            Sample sample = createDraftSample();

            mockMvc.perform(post("/api/samples/" + sample.getId().uuidValue() + "/publish")
                    .with(user("testuser").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("PUBLISHED"));
        }

        @Test
        @DisplayName("POST /samples/{id}/publish for non-existent sample should return 404")
        void publish_nonExistentSample_returnsNotFound() throws Exception {
            mockMvc.perform(post("/api/samples/" + UUID.randomUUID() + "/publish")
                    .with(user("testuser").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("State Machine Tests")
    class StateMachineTests {

        @Test
        @DisplayName("POST /samples/{id}/update on PUBLISHED sample should fail")
        void update_publishedSample_returnsBadRequest() throws Exception {
            Sample sample = createPublishedSample();

            mockMvc.perform(post("/api/samples/" + sample.getId().uuidValue() + "/update")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validUpdateSampleJson()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /samples/{id}/publish on PUBLISHED sample should fail")
        void publish_publishedSample_returnsBadRequest() throws Exception {
            Sample sample = createPublishedSample();

            mockMvc.perform(post("/api/samples/" + sample.getId().uuidValue() + "/publish")
                    .with(user("testuser").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Full Lifecycle Tests")
    class FullLifecycleTests {

        @Test
        @DisplayName("Sample should support full lifecycle via repository: update -> publish")
        void fullLifecycle_updateAndPublish_succeeds() throws Exception {
            // Create sample via repository
            Sample sample = createDraftSample();
            String sampleId = sample.getId().uuidValue().toString();

            // Update via repository (simulating business logic)
            sample.update(UpdateSample.builder()
                    .name(I18nText.en("Final Name"))
                    .description("Final description")
                    .build());
            samples.save(sample);

            // Verify via API that the sample is still in DRAFT
            mockMvc.perform(get("/api/samples/" + sampleId)
                    .with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("DRAFT"))
                    .andExpect(jsonPath("$.name.en").value("Final Name"));

            // Publish via API
            mockMvc.perform(post("/api/samples/" + sampleId + "/publish")
                    .with(user("testuser").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("PUBLISHED"));

            // Verify cannot update after publish
            mockMvc.perform(post("/api/samples/" + sampleId + "/update")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validUpdateSampleJson()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Projection Tests")
    class ProjectionTests {

        @Test
        @DisplayName("GET /samples/{id}?projection=summary renders the aggregate's own field types")
        void getSample_summaryProjection_returnsNameAndOwnerName() throws Exception {
            Sample sample = createDraftSample();

            mockMvc.perform(get("/api/samples/" + sample.getId().uuidValue() + "?projection=summary")
                    .with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name.en").value("Test Sample"))
                    .andExpect(jsonPath("$.ownerName").value(testOwner.getName()));
        }

        @Test
        @DisplayName("GET /samples/{id}?projection=detail resolves the owner behind the association")
        void getSample_detailProjection_returnsNameDescriptionAndOwner() throws Exception {
            Sample sample = createDraftSample();

            mockMvc.perform(get("/api/samples/" + sample.getId().uuidValue() + "?projection=detail")
                    .with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name.en").value("Test Sample"))
                    .andExpect(jsonPath("$.description").value("Test description"))
                    .andExpect(jsonPath("$.owner.name").value(testOwner.getName()));
        }
    }

    @Nested
    @DisplayName("HAL Command Link Tests")
    class CommandLinkTests {

        @Test
        @DisplayName("update/publish links in the response point to endpoints that actually accept the command")
        void createdSample_commandLinksAreFollowable() throws Exception {
            City city = cities.save(City.ofPostalCodeAndName(
                    3000, I18nText.builder().en("Bern").de("Bern").build()));

            // create a sample and read the command links straight off the HAL response
            String createResponse = mockMvc.perform(post("/api/samples")
                    .with(user("admin").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": { "en": "Linkable", "de": "DE_Linkable" },
                                "description": "Sample with command links",
                                "owner": "/api/people/%s",
                                "city": "/api/cities/%s"
                            }
                            """.formatted(
                            testOwner.getId().uuidValue(), city.getId().uuidValue())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._links.update.href").exists())
                    .andExpect(jsonPath("$._links.publish.href").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String updateHref = JsonPath.read(createResponse, "$._links.update.href");
            String publishHref = JsonPath.read(createResponse, "$._links.publish.href");

            // follow the update link verbatim (do NOT reconstruct the URL) — this is what caught the href bug
            mockMvc.perform(post(URI.create(updateHref))
                    .with(user("admin").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validUpdateSampleJson()))
                    .andExpect(status().isOk());

            // follow the publish link verbatim
            mockMvc.perform(post(URI.create(publishHref))
                    .with(user("admin").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("PUBLISHED"));
        }
    }

    @Nested
    @DisplayName("Child Entity Command Link Tests")
    class ItemCommandLinkTests {

        private static final String ITEM_A = "$.items[?(@.label == 'A')]";
        private static final String ITEM_B = "$.items[?(@.label == 'B')]";

        @Test
        @DisplayName("items added through the addItem link carry their own command links")
        void addItem_twice_bothItemsCarryAllItemLinks() throws Exception {
            String json = sampleWithItemsAAndB();

            for (String item : List.of(ITEM_A, ITEM_B)) {
                for (String rel : ITEM_RELS) {
                    assertThat(values(json, item + "._links." + rel + ".href")).as("%s of %s", rel, item).hasSize(1);
                }
            }
            // the item commands are offered on the items only, the root keeps its own commands
            assertThat(JsonPath.<Map<String, Object>>read(json, "$._links"))
                    .containsKeys("update", "addItem")
                    .doesNotContainKeys(ITEM_RELS.toArray(String[]::new));
        }

        @Test
        @DisplayName("after following an item's checkItem link, that item offers nothing while its sibling still does")
        void checkItem_followedLink_onlyCheckedItemLosesItsLinks() throws Exception {
            String json = sampleWithItemsAAndB();

            String checked = postAsUser(single(values(json, ITEM_A + "._links.checkItem.href")), "{}")
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(values(checked, ITEM_A + ".checked")).containsExactly(true);
            assertThat(values(checked, ITEM_A + "._links.*")).isEmpty();
            for (String rel : ITEM_RELS) {
                assertThat(values(checked, ITEM_B + "._links." + rel + ".href")).as(rel).hasSize(1);
            }
        }

        @Test
        @DisplayName("renameItem and removeItem links are followable")
        void renameAndRemoveItem_followedLinks_applied() throws Exception {
            String json = sampleWithItemsAAndB();

            String renamed = postAsUser(single(values(json, ITEM_A + "._links.renameItem.href")), """
                    { "label": "A2" }
                    """)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            assertThat(values(renamed, "$.items[*].label")).containsExactlyInAnyOrder("A2", "B");

            String removed = postAsUser(single(values(renamed, ITEM_B + "._links.removeItem.href")), "{}")
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            assertThat(values(removed, "$.items[*].label")).containsExactly("A2");
        }

        @Test
        @DisplayName("GET of the sample and of the samples collection render the same item links")
        void get_itemAndCollectionResource_renderItemLinks() throws Exception {
            String json = sampleWithItemsAAndB();
            String self = JsonPath.read(json, "$._links.self.href");
            String checkB = single(values(json, ITEM_B + "._links.checkItem.href"));

            String item = mockMvc.perform(get(URI.create(self)).with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            assertThat(values(item, ITEM_B + "._links.checkItem.href")).containsExactly(checkB);

            String collection = mockMvc.perform(get("/api/samples?size=1000").with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            assertThat(values(collection, "$._embedded.samples[*].items[?(@.label == 'B')]._links.checkItem.href"))
                    .contains(checkB);
        }

        @Test
        @DisplayName("a published sample offers no item links")
        void publish_followedLink_noItemLinksLeft() throws Exception {
            String self = JsonPath.read(sampleWithItemsAAndB(), "$._links.self.href");
            // publishing requires the admin role, so only an admin is offered the link
            String json = mockMvc.perform(get(URI.create(self)).with(user("admin").roles("USER", "ADMIN")))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String published = mockMvc.perform(post(URI.create(JsonPath.read(json, "$._links.publish.href")))
                    .with(user("admin").roles("USER", "ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(values(published, "$.items[*].label")).hasSize(2);
            assertThat(values(published, "$.items[*]._links.*")).isEmpty();
            assertThat(JsonPath.<Map<String, Object>>read(published, "$._links")).doesNotContainKey("addItem");
        }

        @Test
        @DisplayName("items do not expose their id - clients address them through the link hrefs")
        void get_items_noIdProperty() throws Exception {
            String json = sampleWithItemsAAndB();

            assertThat(values(json, "$.items[*].label")).containsExactlyInAnyOrder("A", "B");
            assertThat(values(json, "$.items[*].id")).isEmpty();
        }

        @Test
        @DisplayName("a command on an item the sample does not contain is rejected with 400")
        void checkItem_unknownItem_returnsBadRequest() throws Exception {
            String json = sampleWithItemsAAndB();
            String self = JsonPath.read(json, "$._links.self.href");

            postAsUser(self + "/items/" + UUID.randomUUID() + "/checkItem", "{}")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("item operations require authentication")
        void itemOperations_withoutAuthentication_returnUnauthorized() throws Exception {
            String json = sampleWithItemsAAndB();
            List<String> hrefs = new ArrayList<>(List.of((String) JsonPath.read(json, "$._links.addItem.href")));
            for (String rel : ITEM_RELS) {
                hrefs.add(single(values(json, ITEM_A + "._links." + rel + ".href")));
            }

            for (String href : hrefs) {
                mockMvc.perform(post(URI.create(href))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "label": "X" }
                                """))
                        .andExpect(status().isUnauthorized());
            }
        }

        /** Creates a draft sample and adds the items A and B by following its addItem link, as a client would. */
        private String sampleWithItemsAAndB() throws Exception {
            Sample sample = createDraftSample();
            String json = mockMvc.perform(get("/api/samples/" + sample.getId().uuidValue())
                    .with(user("testuser").roles("USER")))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            for (String label : List.of("A", "B")) {
                json = postAsUser(JsonPath.read(json, "$._links.addItem.href"), """
                        { "label": "%s" }
                        """.formatted(label))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
            }
            return json;
        }

        private ResultActions postAsUser(String href, String body) throws Exception {
            return mockMvc.perform(post(URI.create(href))
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
        }

        /** Evaluates a JsonPath that may match any number of nodes - with a filter, it always yields a list. */
        private static List<Object> values(String json, String path) {
            return JsonPath.read(json, path);
        }

        private static String single(List<Object> values) {
            assertThat(values).hasSize(1);
            return (String) values.getFirst();
        }
    }

    private static final List<String> ITEM_RELS = List.of("renameItem", "checkItem", "removeItem");

    private Sample createDraftSample() {
        return samples.save(Sample.create(CreateSample.builder()
                .name(I18nText.en("Test Sample"))
                .description("Test description")
                .owner(testOwner)
                .build()));
    }

    private Sample createPublishedSample() {
        Sample sample = createDraftSample();
        sample.publish(PublishSample.create());
        return samples.save(sample);
    }

    private String validUpdateSampleJson() {
        return """
                {
                    "name": {
                        "en": "Updated Sample",
                        "de": "DE_Updated Sample"
                    },
                    "description": "Updated description"
                }
                """;
    }
}
