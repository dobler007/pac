package com.example.mas_implementation.config;

import com.example.mas_implementation.model.User;
import com.example.mas_implementation.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upgrades any legacy plain-text password to a BCrypt hash on startup. This lets accounts
 * created before BCrypt was introduced continue to log in with their original password,
 * which is now stored hashed. BCrypt hashes start with {@code $2}, so already-hashed
 * passwords are skipped and the runner is a no-op on subsequent restarts.
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (User user : userRepository.findAll()) {
            String stored = user.getPassword();
            if (stored != null && !stored.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(stored));
                userRepository.save(user);
            }
        }
    }
}
