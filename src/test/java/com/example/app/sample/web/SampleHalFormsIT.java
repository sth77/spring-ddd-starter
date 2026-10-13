package com.example.app.sample.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand.CreatePerson;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.AddSampleItem;
import com.example.app.sample.SampleCommand.CheckSampleItem;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.Samples;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * HAL-FORMS templates on the command links: every command link carries a {@code POST} form, keyed by the link's rel,
 * on the collection, on the aggregate and on each child entity. Plain HAL responses do not change.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SampleHalFormsIT {

    private static final RequestPostProcessor ADMIN = user("admin").roles("USER", "ADMIN");
    private static final RequestPostProcessor USER = user("user").roles("USER");
    // what a HAL-FORMS client sends: forms if there are any, plain HAL otherwise
    private static final MediaType[] HAL_FORMS_OR_HAL = {MediaTypes.HAL_FORMS_JSON, MediaTypes.HAL_JSON};

    private static final String OPEN_ITEM = "$.items[?(@.label == 'Open')]";
    private static final String CHECKED_ITEM = "$.items[?(@.label == 'Checked')]";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Samples samples;

    @Autowired
    private People people;

    private Sample sample;

    @BeforeEach
    void setUp() {
        Person owner = people.save(Person.create(CreatePerson.builder()
                .name("Form Owner " + UUID.randomUUID())
                .build()));
        Sample draft = Sample.create(CreateSample.builder()
                .name(I18nText.en("Form sample"))
                .description("Sample rendered as HAL-FORMS")
                .owner(owner)
                .build());
        draft.addItem(AddSampleItem.of("Open"));
        draft.addItem(AddSampleItem.of("Checked"));
        draft.checkItem(draft.getItems().get(1).getId(), CheckSampleItem.create());
        sample = samples.save(draft);
    }

    @Test
    @DisplayName("each command link of the aggregate has a POST template under its rel, targeting the link")
    void getSample_asHalForms_offersATemplatePerCommandLink() throws Exception {
        String json = getSample(HAL_FORMS_OR_HAL, ADMIN);

        assertThat(JsonPath.<Map<String, Object>>read(json, "$._templates"))
                .containsOnlyKeys("update", "publish", "addItem");
        for (String rel : List.of("update", "publish", "addItem")) {
            assertThat(JsonPath.<String>read(json, "$._templates." + rel + ".method")).as(rel).isEqualTo("POST");
            assertThat(JsonPath.<String>read(json, "$._templates." + rel + ".target")).as(rel)
                    .isEqualTo(JsonPath.<String>read(json, "$._links." + rel + ".href"));
        }
    }

    @Test
    @DisplayName("template properties are derived from the command record and its constraints")
    void getSample_asHalForms_describesTheCommandProperties() throws Exception {
        String json = getSample(HAL_FORMS_OR_HAL, ADMIN);

        assertThat(property(json, "update", "description"))
                .containsEntry("maxLength", 1000)
                .containsEntry("type", "text")
                .doesNotContainKeys("min", "max", "required");
        assertThat(property(json, "update", "name")).containsEntry("required", true);
        assertThat(property(json, "addItem", "label"))
                .containsEntry("required", true)
                .containsEntry("maxLength", 255)
                .containsKey("regex"); // @NotBlank
        // a command is an input payload: none of its record components may be offered as read-only
        assertThat(JsonPath.<List<Object>>read(json, "$.._templates..properties[?(@.readOnly == true)]")).isEmpty();
        assertThat(JsonPath.<List<Object>>read(json, "$._templates.publish.properties")).isEmpty();
    }

    @Test
    @DisplayName("an open item has a template per item command link, a checked item has none")
    void getSample_asHalForms_offersItemTemplatesPerItemState() throws Exception {
        String json = getSample(HAL_FORMS_OR_HAL, ADMIN);

        for (String rel : List.of("renameItem", "checkItem", "removeItem")) {
            assertThat(values(json, OPEN_ITEM + "._templates." + rel + ".target")).as(rel)
                    .isEqualTo(values(json, OPEN_ITEM + "._links." + rel + ".href"))
                    .hasSize(1);
        }
        assertThat(values(json, OPEN_ITEM + "._templates.renameItem.properties[?(@.name == 'label')].required"))
                .containsExactly(true);
        assertThat(values(json, CHECKED_ITEM + "._templates")).isEmpty();
    }

    @Test
    @DisplayName("templates follow the links: a command the user's role does not allow has neither")
    void getSample_asHalFormsForUser_offersNoPublishTemplate() throws Exception {
        String json = getSample(HAL_FORMS_OR_HAL, USER);

        assertThat(JsonPath.<Map<String, Object>>read(json, "$._templates"))
                .containsOnlyKeys("update", "addItem");
        assertThat(JsonPath.<Map<String, Object>>read(json, "$._links")).doesNotContainKey("publish");
    }

    @Test
    @DisplayName("plain HAL renders the command links without any templates")
    void getSample_asHal_rendersNoTemplates() throws Exception {
        String json = getSample(new MediaType[]{MediaTypes.HAL_JSON}, ADMIN);

        assertThat(JsonPath.<List<Object>>read(json, "$.._templates")).isEmpty();
        assertThat(JsonPath.<Map<String, Object>>read(json, "$._links")).containsKeys("update", "publish", "addItem");
        assertThat(values(json, OPEN_ITEM + "._links.renameItem.href")).hasSize(1);
    }

    @Test
    @DisplayName("a published sample offers no command, so HAL-FORMS falls back to plain HAL")
    void getPublishedSample_asHalForms_fallsBackToHal() throws Exception {
        sample.publish(PublishSample.create());
        samples.save(sample);

        mockMvc.perform(get("/api/samples/" + sample.getId()).with(ADMIN).accept(HAL_FORMS_OR_HAL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._templates").doesNotExist());
    }

    @Test
    @DisplayName("the collection offers the create command under its showcase rel")
    void getSamples_asHalFormsOnly_offersTheCreateTemplate() throws Exception {
        mockMvc.perform(get("/api/samples").with(USER).accept(MediaTypes.HAL_FORMS_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_FORMS_JSON))
                .andExpect(jsonPath("$._links.produce.href").exists())
                .andExpect(jsonPath("$._templates.produce.method").value("POST"))
                .andExpect(jsonPath("$._templates.produce.properties[?(@.name == 'owner')].required").value(true));
    }

    private String getSample(MediaType[] accept, RequestPostProcessor user) throws Exception {
        return mockMvc.perform(get("/api/samples/" + sample.getId()).with(user).accept(accept))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private static Map<String, Object> property(String json, String template, String name) {
        List<Map<String, Object>> matches = JsonPath.read(json,
                "$._templates." + template + ".properties[?(@.name == '" + name + "')]");
        assertThat(matches).as("property %s of template %s", name, template).hasSize(1);
        return matches.getFirst();
    }

    private static List<Object> values(String json, String path) {
        return JsonPath.read(json, path);
    }
}
