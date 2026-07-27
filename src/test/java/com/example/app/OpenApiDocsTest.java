package com.example.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies that the OpenAPI spec is the complete API contract: it must list the Spring Data REST resources
 * (collections and search), not just the hand-written operations controllers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class OpenApiDocsTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void apiDocs_listSpringDataRestResources() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/samples']").exists())
                .andExpect(jsonPath("$.paths['/api/cities']").exists());
    }
}
