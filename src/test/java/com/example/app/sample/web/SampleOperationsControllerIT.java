package com.example.app.sample.web;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.Samples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private People people;

    @Autowired
    private Samples samples;

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
            mockMvc.perform(get("/api/samples"))
                .andExpect(status().isUnauthorized());
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

            mockMvc.perform(get("/api/samples/" + sample.getId().uuidValue()))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Security Tests - Authenticated Access")
    class AuthenticatedAccessTests {

        @Test
        @DisplayName("GET /samples with authentication should return 200 OK")
        void getSamples_withAuthentication_returnsOk() throws Exception {
            mockMvc.perform(get("/api/samples")
                    .with(user("testuser").roles("USER")))
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
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
        }

        @Test
        @DisplayName("POST /samples/{id}/publish for non-existent sample should return 404")
        void publish_nonExistentSample_returnsNotFound() throws Exception {
            mockMvc.perform(post("/api/samples/" + UUID.randomUUID() + "/publish")
                    .with(user("testuser").roles("USER"))
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
                    .with(user("testuser").roles("USER"))
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
                    .with(user("testuser").roles("USER"))
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

    private Sample createDraftSample() {
        return samples.save(Sample.create(CreateSample.builder()
                .name(I18nText.en("Test Sample"))
                .description("Test description")
                .owner(testOwner)
                .build()));
    }

    private Sample createPublishedSample() {
        Sample sample = createDraftSample();
        sample.publish(new PublishSample());
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
