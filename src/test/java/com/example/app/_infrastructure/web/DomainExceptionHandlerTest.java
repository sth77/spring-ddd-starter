package com.example.app._infrastructure.web;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.common.model.AggregateNotFoundException;
import com.example.app.common.model.DomainException;
import com.example.app.common.model.ReferenceDataNotFoundException;
import com.example.app.person.Person;
import com.example.app.person.Person.PersonId;
import com.example.app.referencedata.City;
import com.example.app.referencedata.City.CityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The domain throws exceptions without any notion of HTTP; {@link DomainExceptionHandler} decides about the status,
 * picking the handler of the most specific exception type.
 */
class DomainExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new DomainExceptionHandler())
                .build();
    }

    @Test
    void domainException_mappedToBadRequest() throws Exception {
        mockMvc.perform(get("/rule-violated"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("rule violated"));
    }

    @Test
    void aggregateNotFound_mappedToNotFound() throws Exception {
        mockMvc.perform(get("/aggregate-missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(startsWith("Aggregate Person with ID")));
    }

    @Test
    void referenceDataNotFound_mappedToNotFound() throws Exception {
        mockMvc.perform(get("/reference-data-missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(startsWith("Reference data City")));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/rule-violated")
        void ruleViolated() {
            throw new DomainException("rule violated");
        }

        @GetMapping("/aggregate-missing")
        void aggregateMissing() {
            throw new AggregateNotFoundException(Person.class, PersonId.random());
        }

        @GetMapping("/reference-data-missing")
        void referenceDataMissing() {
            throw new ReferenceDataNotFoundException(City.class, CityId.random());
        }
    }
}
