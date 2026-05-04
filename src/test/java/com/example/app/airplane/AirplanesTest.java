package com.example.app.airplane;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@DataJpaTest
class AirplanesTest {
    @Autowired
    Airplanes airplanes;

    @Test
    void findAll() {
    }

    @Test
    void findById() {
        Airplane instance = airplane();
        airplanes.save(instance);

        Airplane readInstance = airplanes.findById(instance.getId());
        assertEquals(instance, readInstance);
    }

    @Test
    void save() {
        Airplane instance = airplane();
        airplanes.save(instance);

    }

    private static Airplane airplane() {
        return Airplane.register(InventoryCode.of("doc-123"));
    }
}