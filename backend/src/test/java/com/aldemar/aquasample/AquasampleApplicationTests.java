package com.aldemar.aquasample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Comprueba que la aplicación arranca completa (con H2, perfil "test"). */
@SpringBootTest
@ActiveProfiles("test")
class AquasampleApplicationTests {

    @Test
    void contextLoads() {
    }
}
