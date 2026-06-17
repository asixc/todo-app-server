package dev.jotxee.todo.auth.controller;

import dev.jotxee.todo.auth.dto.AuthResponseDto;
import dev.jotxee.todo.auth.dto.OtpRequestDto;
import dev.jotxee.todo.auth.dto.OtpVerifyDto;
import dev.jotxee.todo.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            @Value("${app.secure-cookie:false}") boolean secureCookie) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Void> requestOtp(@RequestBody OtpRequestDto dto) {
        authService.requestOtp(dto.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @RequestBody OtpVerifyDto dto,
            HttpServletResponse response) {

        AuthService.VerifiedOtpSession session = authService.verifyOtpAndCreateRefreshToken(dto.email(), dto.otp());

        setRefreshCookie(response, session.refreshToken().getToken(),
                (int) session.refreshToken().getExpiresAt()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toEpochSecond() - (int) (System.currentTimeMillis() / 1000));

        return ResponseEntity.ok(new AuthResponseDto(session.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(HttpServletRequest request) {
        String rawToken = extractRefreshCookie(request);
        String accessToken = authService.refresh(rawToken);
        return ResponseEntity.ok(new AuthResponseDto(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshCookie(request);
        authService.logout(rawToken);
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    // --- helpers ---

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token no encontrado");
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Refresh token no encontrado"));
    }

    private void setRefreshCookie(HttpServletResponse response, String value, int maxAge) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(maxAge);
        // SameSite=Strict via header (Cookie API de Servlet no lo soporta directamente)
        response.addCookie(cookie);
        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/api/v1/auth; HttpOnly; SameSite=Strict%s",
                REFRESH_COOKIE_NAME, value, maxAge, secureCookie ? "; Secure" : "");
        response.setHeader("Set-Cookie", cookieHeader);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        String cookieHeader = String.format(
                "%s=; Max-Age=0; Path=/api/v1/auth; HttpOnly; SameSite=Strict%s",
                REFRESH_COOKIE_NAME, secureCookie ? "; Secure" : "");
        response.setHeader("Set-Cookie", cookieHeader);
    }
}
