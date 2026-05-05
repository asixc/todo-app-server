package dev.jotxee.todo.auth.filter;

import dev.jotxee.todo.auth.service.JwtService;
import dev.jotxee.todo.repository.AllowedUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AllowedUserRepository allowedUserRepository;

    public JwtAuthFilter(JwtService jwtService, AllowedUserRepository allowedUserRepository) {
        this.jwtService = jwtService;
        this.allowedUserRepository = allowedUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isValid(token)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
            return;
        }

        String email = jwtService.extractEmail(token);

        // Verificar que el usuario sigue activo en la whitelist
        boolean isActive = allowedUserRepository.findByEmailAndActiveTrue(email).isPresent();
        if (!isActive) {
            writeError(response, HttpStatus.FORBIDDEN, "Usuario no autorizado");
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":" + status.value() + ",\"error\":\"" + message + "\"}");
    }
}
