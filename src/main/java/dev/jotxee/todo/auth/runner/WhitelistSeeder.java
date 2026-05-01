package dev.jotxee.todo.auth.runner;

import dev.jotxee.todo.entities.AllowedUser;
import dev.jotxee.todo.repository.AllowedUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class WhitelistSeeder implements ApplicationRunner {

    private final AllowedUserRepository allowedUserRepository;
    private final String allowedEmails;

    public WhitelistSeeder(
            AllowedUserRepository allowedUserRepository,
            @Value("${app.allowed-emails:}") String allowedEmails) {
        this.allowedUserRepository = allowedUserRepository;
        this.allowedEmails = allowedEmails;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (allowedEmails == null || allowedEmails.isBlank()) {
            return;
        }

        Arrays.stream(allowedEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .forEach(email -> {
                    if (!allowedUserRepository.existsByEmail(email)) {
                        allowedUserRepository.save(new AllowedUser(email, null));
                        System.out.println("[WhitelistSeeder] Usuario añadido: " + email);
                    }
                });
    }
}
