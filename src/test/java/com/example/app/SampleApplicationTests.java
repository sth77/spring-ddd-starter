package com.example.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.security.test.context.support.WithMockUser;

@Slf4j
@WithMockUser
@SpringBootTest
@AutoConfigureMockMvc
class SampleApplicationTests {

    @ArchTest
    ArchRule dddRules = JMoleculesDddRules.all();
    @ArchTest
    ArchRule onion = JMoleculesArchitectureRules.ensureOnionSimple();

    @Test
    void contextLoads() {
    }

    @Test
    void writeDocumentationSnippets() {
        /* TODO: correctly configure modules and module dependencies
        var modules = ApplicationModules.of(SampleApplication.class).verify();
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
         */
    }

}
