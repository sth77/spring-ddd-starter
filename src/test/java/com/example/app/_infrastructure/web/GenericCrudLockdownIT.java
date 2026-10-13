package com.example.app._infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand.CreatePerson;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.Samples;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * {@link WebConfiguration} disables the generic CRUD write methods of every exported repository, so that an
 * aggregate changes only through the operations of its root - validated by the domain and secured per role by the
 * operations controller. Each aggregate's write path is asserted here: a regression in that configuration would
 * otherwise open a command-free, role-free write path that no other test notices. (Reference data is covered by
 * {@code CitiesRestIT}.)
 */
@SpringBootTest
@AutoConfigureMockMvc
class GenericCrudLockdownIT {

    // the strongest role there is: the lockdown must not depend on the role of the caller
    private static final RequestPostProcessor ADMIN = user("admin").roles("USER", "ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private People people;

    @Autowired
    private Samples samples;

    private Person person;
    private Sample sample;

    @BeforeEach
    void setUp() {
        person = people.save(Person.create(CreatePerson.builder()
                .name("Locked " + UUID.randomUUID())
                .build()));
        sample = samples.save(Sample.create(CreateSample.builder()
                .name(I18nText.en("Locked sample"))
                .description("Not writable through CRUD")
                .owner(person)
                .build()));
    }

    @Test
    void writeMethodsOnPeople_areNotExposed() throws Exception {
        String item = "/api/people/" + person.getId();

        assertMethodNotAllowed(post("/api/people"));
        assertMethodNotAllowed(put(item));
        assertMethodNotAllowed(patch(item));
        assertMethodNotAllowed(delete(item));

        assertThat(people.findById(person.getId())).get()
                .extracting(Person::getName)
                .isEqualTo(person.getName());
    }

    /**
     * {@code POST /api/samples} is not generic CRUD but the create command of the operations controller, which is
     * covered by its own tests.
     */
    @Test
    void writeMethodsOnSamples_areNotExposed() throws Exception {
        String item = "/api/samples/" + sample.getId();

        assertMethodNotAllowed(put(item));
        assertMethodNotAllowed(patch(item));
        assertMethodNotAllowed(delete(item));
        // the association resource would let a client swap the owner past the domain
        assertMethodNotAllowed(put(item + "/owner"));
        assertMethodNotAllowed(delete(item + "/owner"));

        assertThat(samples.findById(sample.getId())).get().satisfies(unchanged -> {
            assertThat(unchanged.getName()).isEqualTo(sample.getName());
            assertThat(unchanged.getOwner().getId()).isEqualTo(person.getId());
        });
    }

    /**
     * PUT and PATCH must be rejected before Spring Data REST binds the body, which would otherwise fail on the
     * setter-less aggregate with a 400 or 500 instead of a 405 (see {@code GenericCrudWriteInterceptor}).
     */
    private void assertMethodNotAllowed(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request
                .with(ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Changed\"}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(ALLOW, not(containsString(request.buildRequest(null).getMethod()))));
    }
}
