package dev.jotxee.todo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/*
@Configuration
public class CorsConfig implements WebMvcConfigurer {
 Para cuando solo queramos permitir requests desde un frontend específico
    @Value("${allowed.origin}")
    String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Permitir todas las rutas
                .allowedOrigins(allowedOrigin)  // Permitir el dominio específico
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos HTTP permitidos
                .allowedHeaders("*")  // Permitir todos los encabezados
                .maxAge(3600)  // Tiempo de vida de la configuración
                .allowCredentials(true);  // Permitir el envío de cookies o credenciales
    }
}
*/
