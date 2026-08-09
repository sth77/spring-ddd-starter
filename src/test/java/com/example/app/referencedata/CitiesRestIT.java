package com.example.app.referencedata;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Reference data is served as a REST resource, so a client has to be able to reach an item by following the
 * link the collection hands out - and must not be able to change it.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CitiesRestIT {

    private static final RequestPostProcessor USER = user("user").roles("USER");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void selfLinkOfListedCity_canBeFollowed() throws Exception {
        mockMvc.perform(get(URI.create(anyCityHref())).with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postalCode").exists());
    }

    /**
     * Reference data is read-only over REST. Spring Data REST would otherwise export the write methods of the
     * {@code CrudRepository} it is built on, reachable by any authenticated user and outside the {@code @Secured}
     * annotations that guard the operations controllers.
     */
    @Test
    void writeMethodsOnCities_areNotExposed() throws Exception {
        var city = URI.create(anyCityHref()).getPath();

        mockMvc.perform(options("/api/cities").with(USER))
                .andExpect(header().string(ALLOW, not(containsString("POST"))));
        mockMvc.perform(options(city).with(USER))
                .andExpect(header().string(ALLOW, allOf(
                        not(containsString("PUT")),
                        not(containsString("PATCH")),
                        not(containsString("DELETE")))));
        mockMvc.perform(delete(city).with(USER))
                .andExpect(status().isMethodNotAllowed());
    }

    private String anyCityHref() throws Exception {
        var collection = mockMvc.perform(get("/api/cities").with(USER))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(collection, "$._embedded.cities[0]._links.self.href");
    }
}
