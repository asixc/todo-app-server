package dev.jotxee.todo.auth.service;

import dev.jotxee.todo.repository.AllowedUserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AllowedUserService {

    public static final String ACTIVE_USERS_CACHE = "activeUsers";

    private final AllowedUserRepository allowedUserRepository;

    public AllowedUserService(AllowedUserRepository allowedUserRepository) {
        this.allowedUserRepository = allowedUserRepository;
    }

    @Cacheable(cacheNames = ACTIVE_USERS_CACHE, key = "#email")
    @Transactional(readOnly = true)
    public boolean isActive(String email) {
        return allowedUserRepository.findByEmailAndActiveTrue(email).isPresent();
    }
}
