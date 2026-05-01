package dev.jotxee.todo.repository;

import dev.jotxee.todo.entities.AllowedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedUserRepository extends JpaRepository<AllowedUser, Long> {
    Optional<AllowedUser> findByEmailAndActiveTrue(String email);
    boolean existsByEmail(String email);
}
