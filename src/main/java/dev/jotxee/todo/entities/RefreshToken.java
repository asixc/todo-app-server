package dev.jotxee.todo.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "family_expires_at", nullable = false)
    private LocalDateTime familyExpiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "replaced_by_hash", length = 64)
    private String replacedByHash;

    public RefreshToken() {}

    public RefreshToken(
            String email,
            String tokenHash,
            String familyId,
            LocalDateTime expiresAt,
            LocalDateTime familyExpiresAt) {
        this.email = email;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
        this.familyExpiresAt = familyExpiresAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTokenHash() { return tokenHash; }
    public String getFamilyId() { return familyId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getFamilyExpiresAt() { return familyExpiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public String getReplacedByHash() { return replacedByHash; }

    public void revoke(LocalDateTime revokedAt, String replacedByHash) {
        this.revoked = true;
        this.revokedAt = revokedAt;
        this.replacedByHash = replacedByHash;
    }
}
