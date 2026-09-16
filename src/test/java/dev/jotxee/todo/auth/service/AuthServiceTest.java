package dev.jotxee.todo.auth.service;

import dev.jotxee.todo.entities.OtpToken;
import dev.jotxee.todo.entities.RefreshToken;
import dev.jotxee.todo.repository.OtpTokenRepository;
import dev.jotxee.todo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AllowedUserService allowedUserService;

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                allowedUserService,
                otpTokenRepository,
                refreshTokenRepository,
                jwtService,
                emailService,
                passwordEncoder,
                604_800,
                7_776_000);
    }

    @Test
    void doesNotRevealWhetherAnEmailIsAllowed() {
        when(allowedUserService.isActive("unknown@example.com")).thenReturn(false);

        authService.requestOtp(" unknown@example.com ");

        verify(otpTokenRepository, never()).save(any());
        verify(emailService, never()).sendOtp(any(), any());
    }

    @Test
    void verifiesOtpAndStoresOnlyItsHashBackedRefreshToken() {
        OtpToken otpToken = new OtpToken(
                "user@example.com",
                passwordEncoder.encode("123456"),
                LocalDateTime.now().plusMinutes(5));
        when(otpTokenRepository.findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
                eq("user@example.com"), any(LocalDateTime.class))).thenReturn(Optional.of(otpToken));
        when(jwtService.generateAccessToken("user@example.com")).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.VerifiedOtpSession session = authService
                .verifyOtpAndCreateRefreshToken("USER@example.com", "123456");

        assertThat(session.accessToken()).isEqualTo("access-token");
        assertThat(session.refreshToken()).isNotBlank();
        assertThat(otpToken.isUsed()).isTrue();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isNotEqualTo(session.refreshToken());
        assertThat(captor.getValue().getFamilyId()).isNotBlank();
    }

    @Test
    void rejectsAnOtpAfterFiveFailedAttempts() {
        OtpToken otpToken = new OtpToken(
                "user@example.com",
                passwordEncoder.encode("123456"),
                LocalDateTime.now().plusMinutes(5));
        for (int attempt = 0; attempt < 5; attempt++) {
            otpToken.registerFailedAttempt();
        }
        when(otpTokenRepository.findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
                eq("user@example.com"), any(LocalDateTime.class))).thenReturn(Optional.of(otpToken));

        assertThatThrownBy(() -> authService.verifyOtpAndCreateRefreshToken(
                "user@example.com", "123456"))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(otpToken.isUsed()).isTrue();
    }

    @Test
    void rotatesRefreshTokenAndRevokesThePreviousOne() {
        String rawToken = "old-token";
        RefreshToken refreshToken = new RefreshToken(
                "user@example.com",
                hash(rawToken),
                "family-id",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(30));
        when(refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken)))
                .thenReturn(Optional.of(refreshToken));
        when(allowedUserService.isActive("user@example.com")).thenReturn(true);
        when(jwtService.generateAccessToken("user@example.com")).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.RefreshSession session = authService.refresh(rawToken);

        assertThat(session.accessToken()).isEqualTo("new-access-token");
        assertThat(session.refreshToken()).isNotEqualTo(rawToken);
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
    }

    @Test
    void revokesTheWholeFamilyWhenARefreshTokenIsReused() {
        RefreshToken revokedToken = new RefreshToken(
                "user@example.com",
                hash("reused-token"),
                "family-id",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(30));
        revokedToken.revoke(LocalDateTime.now(), "replacement-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate(hash("reused-token")))
                .thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refresh("reused-token"))
                .isInstanceOf(ResponseStatusException.class);

        verify(refreshTokenRepository).revokeFamily(any(), any());
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
