// src/main/java/com/example/mas_implementation/web/ProfileController.java
package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    private final PlayerRepository playerRepo;

    public ProfileController(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @GetMapping("/profile")
    public String myProfile(Model model, HttpSession session) {
        Long id = (Long) session.getAttribute("currentUserId");
        if (id == null) {
            return "redirect:/login";
        }
        return viewProfile(id, model);
    }

    @GetMapping("/players/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        Player p = playerRepo.findById(id).orElse(null);
        if (p == null) {
            return "redirect:/";
        }
        model.addAttribute("player", p);
        model.addAttribute("games", p.getGames());
        model.addAttribute("avgSkill", String.format("%.2f", p.getAverageSkill()));
        model.addAttribute("avgBehavior", String.format("%.2f", p.getAverageBehavior()));
        model.addAttribute("age", p.getAge());
        return "profile";
    }
}
