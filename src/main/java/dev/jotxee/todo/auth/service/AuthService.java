package dev.jotxee.todo.auth.service;

import dev.jotxee.todo.entities.OtpToken;
import dev.jotxee.todo.entities.RefreshToken;
import dev.jotxee.todo.repository.OtpTokenRepository;
import dev.jotxee.todo.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AllowedUserService allowedUserService;
    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshExpirationSeconds;
    private final long sessionMaxExpirationSeconds;

    public record VerifiedOtpSession(
            String accessToken,
            String refreshToken,
            LocalDateTime refreshExpiresAt) {
    }

    public record RefreshSession(
            String accessToken,
            String refreshToken,
            LocalDateTime refreshExpiresAt) {
    }

    public AuthService(
            AllowedUserService allowedUserService,
            OtpTokenRepository otpTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.refresh-expiration}") long refreshExpirationSeconds,
            @Value("${jwt.session-max-expiration:7776000}") long sessionMaxExpirationSeconds) {
        this.allowedUserService = allowedUserService;
        this.otpTokenRepository = otpTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        this.sessionMaxExpirationSeconds = sessionMaxExpirationSeconds;
    }

    @Transactional
    public void requestOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (!allowedUserService.isActive(normalizedEmail)) {
            return;
        }

        // Invalidate previous OTPs for the same email
        otpTokenRepository.deleteAllByEmail(normalizedEmail);

        String otp = generateOtp();
        OtpToken otpToken = new OtpToken(
                normalizedEmail,
                passwordEncoder.encode(otp),
                LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        otpTokenRepository.save(otpToken);

        emailService.sendOtp(normalizedEmail, otp);
    }

    @Transactional
    public VerifiedOtpSession verifyOtpAndCreateRefreshToken(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        OtpToken otpToken = otpTokenRepository
                .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
                        normalizedEmail, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid or expired OTP"));

        if (otpToken.getAttempts() >= MAX_OTP_ATTEMPTS
                || !passwordEncoder.matches(otp, otpToken.getOtpHash())) {
            otpToken.registerFailedAttempt();
            if (otpToken.getAttempts() >= MAX_OTP_ATTEMPTS) {
                otpToken.setUsed(true);
            }
            otpTokenRepository.save(otpToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired OTP");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        String accessToken = jwtService.generateAccessToken(normalizedEmail);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime familyExpiresAt = now.plusSeconds(sessionMaxExpirationSeconds);
        String rawRefreshToken = generateRefreshToken();
        LocalDateTime refreshExpiresAt = calculateRefreshExpiration(now, familyExpiresAt);
        RefreshToken refreshToken = new RefreshToken(
                normalizedEmail,
                hashToken(rawRefreshToken),
                UUID.randomUUID().toString(),
                refreshExpiresAt,
                familyExpiresAt);
        refreshTokenRepository.save(refreshToken);
        return new VerifiedOtpSession(accessToken, rawRefreshToken, refreshExpiresAt);
    }

    @Transactional
    public RefreshSession refresh(String rawToken) {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashForUpdate(hashToken(rawToken))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeFamily(refreshToken.getFamilyId(), now);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        if (!refreshToken.getExpiresAt().isAfter(now)
                || !refreshToken.getFamilyExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        // Verify the user is still in the whitelist
        if (!allowedUserService.isActive(refreshToken.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized");
        }

        String rawReplacementToken = generateRefreshToken();
        String replacementHash = hashToken(rawReplacementToken);
        LocalDateTime replacementExpiresAt = calculateRefreshExpiration(now, refreshToken.getFamilyExpiresAt());
        refreshToken.revoke(now, replacementHash);
        refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.save(new RefreshToken(
                refreshToken.getEmail(),
                replacementHash,
                refreshToken.getFamilyId(),
                replacementExpiresAt,
                refreshToken.getFamilyExpiresAt()));

        return new RefreshSession(
                jwtService.generateAccessToken(refreshToken.getEmail()),
                rawReplacementToken,
                replacementExpiresAt);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenHashForUpdate(hashToken(rawToken)).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.revoke(LocalDateTime.now(), null);
                refreshTokenRepository.save(token);
            }
        });
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String generateRefreshToken() {
        byte[] token = new byte[REFRESH_TOKEN_BYTES];
        RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private LocalDateTime calculateRefreshExpiration(LocalDateTime now, LocalDateTime familyExpiresAt) {
        LocalDateTime slidingExpiration = now.plusSeconds(refreshExpirationSeconds);
        return slidingExpiration.isBefore(familyExpiresAt) ? slidingExpiration : familyExpiresAt;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
