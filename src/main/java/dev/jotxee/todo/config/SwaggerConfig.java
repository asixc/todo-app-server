package dev.jotxee.todo.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("TODO API")
                        .description("Documentación de API de Todo App")
                        .version("v1.0"))
                .servers(List.of(new Server().url(baseUrl)))  // Usar la URL dinámica
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación completa")
                        .url(baseUrl + "/swagger-ui.html"));  // Configurar la URL de Swagger UI
    }
}
