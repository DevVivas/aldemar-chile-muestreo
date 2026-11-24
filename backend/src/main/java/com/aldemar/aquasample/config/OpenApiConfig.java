package com.aldemar.aquasample.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos generales que muestra Swagger UI (/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aquaSampleOpenApi() {
        return new OpenAPI().info(new Info()
                .title("AquaSample API")
                .description("API REST de AquaSample (ALDEMAR SpA) para registrar, consultar y sincronizar "
                        + "muestras de choritos tomadas en terreno con la app Android.")
                .version("1.0"));
    }
}
