package dev.jotxee.todo.auth.service;

import dev.jotxee.todo.entities.OtpToken;
import dev.jotxee.todo.entities.RefreshToken;
import dev.jotxee.todo.repository.AllowedUserRepository;
import dev.jotxee.todo.repository.OtpTokenRepository;
import dev.jotxee.todo.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AllowedUserRepository allowedUserRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final long refreshExpirationSeconds;

    public AuthService(
            AllowedUserRepository allowedUserRepository,
            OtpTokenRepository otpTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            EmailService emailService,
            @Value("${jwt.refresh-expiration}") long refreshExpirationSeconds) {
        this.allowedUserRepository = allowedUserRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    @Transactional
    public void requestOtp(String email) {
        allowedUserRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid request"));

                // Invalidate previous OTPs for the same email
        otpTokenRepository.deleteAllByEmail(email);

        String otp = generateOtp();
        OtpToken otpToken = new OtpToken(
                email,
                otp,
                LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        otpTokenRepository.save(otpToken);

        emailService.sendOtp(email, otp);
    }

    @Transactional
    public String verifyOtp(String email, String otp) {
        OtpToken otpToken = otpTokenRepository
                .findTopByEmailAndOtpAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
                        email, otp, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid or expired OTP"));

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return jwtService.generateAccessToken(email);
    }

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        RefreshToken refreshToken = new RefreshToken(
                email,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusSeconds(refreshExpirationSeconds));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String refresh(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndExpiresAtAfter(rawToken, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));

        // Verify the user is still in the whitelist
        allowedUserRepository.findByEmailAndActiveTrue(refreshToken.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "User not authorized"));

        return jwtService.generateAccessToken(refreshToken.getEmail());
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokenRepository.revokeByToken(rawToken);
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
