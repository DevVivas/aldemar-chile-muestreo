package com.aldemar.aquasample.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos generales que muestra Swagger UI (/swagger-ui.html) y el esquema de login HTTP Basic. */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BASIC = "basicAuth";

    @Bean
    public OpenAPI aquaSampleOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AquaSample API")
                        .description("API REST de AquaSample (ALDEMAR SpA) para registrar, consultar, revisar y "
                                + "sincronizar muestras de choritos tomadas en terreno con la app Android.\n\n"
                                + "Usa el botón **Authorize** e ingresa usuario y contraseña (HTTP Basic). "
                                + "Roles: OPERARIO, SUPERVISOR y ANALISTA.")
                        .version("1.0"))
                // Define el esquema HTTP Basic → aparece el botón "Authorize" en Swagger UI
                .components(new Components().addSecuritySchemes(ESQUEMA_BASIC, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("Usuario y contraseña de AquaSample")))
                // Y lo aplica a todos los endpoints (Swagger enviará las credenciales en cada petición)
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BASIC));
    }
}
