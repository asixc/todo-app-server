package dev.jotxee.todo.repository;

import dev.jotxee.todo.entities.OtpToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpToken> findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
            String email, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.email = :email")
    void deleteAllByEmail(String email);
}
