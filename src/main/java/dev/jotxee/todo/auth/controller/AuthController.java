package dev.jotxee.todo.auth.controller;

import dev.jotxee.todo.auth.dto.AuthResponseDto;
import dev.jotxee.todo.auth.dto.OtpRequestDto;
import dev.jotxee.todo.auth.dto.OtpVerifyDto;
import dev.jotxee.todo.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final boolean secureCookie;
    private final String frontendUrl;

    public AuthController(
            AuthService authService,
            @Value("${app.secure-cookie:false}") boolean secureCookie,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.authService = authService;
        this.secureCookie = secureCookie;
        this.frontendUrl = frontendUrl;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequestDto dto,
                                           HttpServletRequest request) {
        validateOrigin(request);
        authService.requestOtp(dto.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @Valid @RequestBody OtpVerifyDto dto,
            HttpServletRequest request,
            HttpServletResponse response) {
        validateOrigin(request);

        AuthService.VerifiedOtpSession session = authService.verifyOtpAndCreateRefreshToken(dto.email(), dto.otp());

        setRefreshCookie(response, session.refreshToken(), session.refreshExpiresAt());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AuthResponseDto(session.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        validateOrigin(request);
        String rawToken = extractRefreshCookie(request);
        AuthService.RefreshSession session = authService.refresh(rawToken);
        setRefreshCookie(response, session.refreshToken(), session.refreshExpiresAt());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AuthResponseDto(session.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        validateOrigin(request);
        String rawToken = extractRefreshCookieIfPresent(request).orElse(null);
        authService.logout(rawToken);
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    // --- helpers ---

    private String extractRefreshCookie(HttpServletRequest request) {
        return extractRefreshCookieIfPresent(request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Refresh token no encontrado"));
    }

    private Optional<String> extractRefreshCookieIfPresent(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (var cookie : request.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private void setRefreshCookie(
            HttpServletResponse response,
            String value,
            LocalDateTime expiresAt) {
        long maxAge = Math.max(0, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void validateOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null && !isAllowedOrigin(origin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Origin not allowed");
        }
    }

    private boolean isAllowedOrigin(String origin) {
        return frontendUrl.equals(origin)
                || (frontendUrl.endsWith("*")
                && origin.startsWith(frontendUrl.substring(0, frontendUrl.length() - 1)));
    }
}
