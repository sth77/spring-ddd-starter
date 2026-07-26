package com.example.app;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.security.test.context.support.WithMockUser;

@Slf4j
@WithMockUser
@SpringBootTest
@AutoConfigureMockMvc
class SampleApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void writeDocumentationSnippets() {
        var modules = ApplicationModules.of(SampleApplication.class).verify();
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

}
