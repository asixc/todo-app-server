package dev.jotxee.todo.repository;

import dev.jotxee.todo.entities.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndOtpAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
            String email, String otp, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.email = :email")
    void deleteAllByEmail(String email);
}
