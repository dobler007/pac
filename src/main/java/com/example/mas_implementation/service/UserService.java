package com.example.mas_implementation.service;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.web.dto.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application logic around user accounts: registration (with password hashing) and the
 * merit-score helpers. Keeping this out of the controller keeps the web layer thin and makes
 * the registration rules testable in isolation.
 */
@Service
@Transactional
public class UserService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** True if the given login is already taken by an existing account. */
    @Transactional(readOnly = true)
    public boolean isLoginTaken(String login) {
        return playerRepository.findByLogin(login).isPresent();
    }

    /**
     * Creates and persists a new {@link Player} from a validated registration form.
     * The raw password is hashed with BCrypt before it is stored.
     */
    public Player register(RegisterForm form) {
        Player player = new Player();
        player.setLogin(form.getLogin().trim());
        player.setPassword(passwordEncoder.encode(form.getPassword()));
        player.setName(form.getName().trim());
        player.setEmail(form.getEmail().trim());
        player.setPhoneNumber(form.getPhoneNumber() != null ? form.getPhoneNumber().trim() : null);
        player.setBirthdate(form.getBirthdate());
        return playerRepository.save(player);
    }
}
