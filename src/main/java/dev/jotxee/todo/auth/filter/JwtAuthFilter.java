package dev.jotxee.todo.auth.filter;

import dev.jotxee.todo.auth.service.AllowedUserService;
import dev.jotxee.todo.auth.service.JwtService;
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
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AllowedUserService allowedUserService;

    public JwtAuthFilter(JwtService jwtService, AllowedUserService allowedUserService) {
        this.jwtService = jwtService;
        this.allowedUserService = allowedUserService;
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

        Optional<String> validEmail = jwtService.extractValidEmail(token);
        if (validEmail.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        String email = validEmail.get();

        // Verify the user is still active in the whitelist
        boolean isActive = allowedUserService.isActive(email);
        if (!isActive) {
            writeError(response, HttpStatus.FORBIDDEN, "User not authorized");
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
