package dev.jotxee.todo.config;

import dev.jotxee.todo.auth.filter.JwtAuthFilter;
import dev.jotxee.todo.auth.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final String frontendUrl;
    private final boolean corsEnabled;
    private final boolean docsEnabled;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            RateLimitFilter rateLimitFilter,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.cors.enabled:false}") boolean corsEnabled,
            @Value("${app.docs.enabled:true}") boolean docsEnabled) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.frontendUrl = frontendUrl;
        this.corsEnabled = corsEnabled;
        this.docsEnabled = docsEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable);

        if (corsEnabled) {
            http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        } else {
            http.cors(AbstractHttpConfigurer::disable);
        }

        return http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers(
                            "/actuator/health/liveness",
                            "/actuator/health/readiness").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/request-otp").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/verify-otp").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll();
                    if (docsEnabled) {
                        auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll();
                        auth.requestMatchers("/v3/api-docs/**").permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> frameOptions.deny()))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // allowedOriginPatterns soporta wildcards (ej: http://localhost:*)
        config.setAllowedOriginPatterns(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
