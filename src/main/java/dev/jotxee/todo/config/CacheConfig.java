package dev.jotxee.todo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import dev.jotxee.todo.auth.service.AllowedUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.whitelist.ttl:PT60S}") Duration whitelistTtl,
            @Value("${app.cache.whitelist.maximum-size:1000}") long whitelistMaximumSize) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(AllowedUserService.ACTIVE_USERS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(whitelistTtl)
                .maximumSize(whitelistMaximumSize)
                .recordStats());
        return cacheManager;
    }
}
