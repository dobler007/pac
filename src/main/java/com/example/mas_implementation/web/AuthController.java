package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final PlayerRepository playerRepo;

    public AuthController(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam LocalDate birthdate,
            HttpSession session
    ) {
        if (playerRepo.findByLogin(login).isPresent()) {
            return "redirect:/register";
        }

        Player player = new Player();
        player.setLogin(login.trim());
        player.setPassword(password);
        player.setName(name.trim());
        player.setEmail(email.trim());
        player.setPhoneNumber(phoneNumber.trim());
        player.setBirthdate(birthdate);

        playerRepo.save(player);
        session.setAttribute("currentUserId", player.getId());

        return "redirect:/";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String login,
            @RequestParam String password,
            HttpSession session
    ) {
        Player p = playerRepo.findByLogin(login).orElse(null);
        if (p != null && p.getPassword().equals(password)) {
            session.setAttribute("currentUserId", p.getId());
        }
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
