package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.web.dto.LoginForm;
import com.example.mas_implementation.web.dto.RegisterForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final PlayerRepository playerRepo;

    public AuthController(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult result,
            Model model,
            HttpSession session) {

        // Bean Validation errors (blank fields, bad email, etc.)
        if (result.hasErrors()) {
            return "register";
        }

        // Username already taken
        if (playerRepo.findByLogin(form.getLogin()).isPresent()) {
            result.rejectValue("login", "duplicate", "Username is already taken");
            return "register";
        }

        Player player = new Player();
        player.setLogin(form.getLogin().trim());
        player.setPassword(form.getPassword());          // plain-text; BCrypt is a future improvement
        player.setName(form.getName().trim());
        player.setEmail(form.getEmail().trim());
        player.setPhoneNumber(form.getPhoneNumber() != null ? form.getPhoneNumber().trim() : null);
        player.setBirthdate(form.getBirthdate());

        playerRepo.save(player);
        session.setAttribute("currentUserId", player.getId());

        return "redirect:/";
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult result,
            HttpSession session) {

        if (result.hasErrors()) {
            return "login";
        }

        Player p = playerRepo.findByLogin(form.getLogin()).orElse(null);
        if (p == null || !p.getPassword().equals(form.getPassword())) {
            result.reject("auth.failed", "Incorrect username or password");
            return "login";
        }

        session.setAttribute("currentUserId", p.getId());
        return "redirect:/";
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
