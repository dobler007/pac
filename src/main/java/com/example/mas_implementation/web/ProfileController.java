package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
        return viewProfile(id, model, session);
    }

    @GetMapping("/players/{id}")
    public String viewProfile(@PathVariable Long id, Model model, HttpSession session) {
        Player p = playerRepo.findById(id).orElse(null);
        if (p == null) {
            return "redirect:/";
        }

        Long currentId = (Long) session.getAttribute("currentUserId");

        boolean alreadyRated = false;
        Integer existingSkill = null;
        Integer existingBehavior = null;

        if (currentId != null && !currentId.equals(id)) {
            if (p.getSkillRatings() != null && p.getSkillRatings().containsKey(currentId)) {
                alreadyRated = true;
                existingSkill = p.getSkillRatings().get(currentId);
            }
            if (p.getBehaviorRatings() != null && p.getBehaviorRatings().containsKey(currentId)) {
                alreadyRated = true;
                existingBehavior = p.getBehaviorRatings().get(currentId);
            }
        }

        model.addAttribute("player", p);
        model.addAttribute("games", p.getGames());
        model.addAttribute("avgSkill", String.format("%.2f", p.getAverageSkill()));
        model.addAttribute("avgBehavior", String.format("%.2f", p.getAverageBehavior()));
        model.addAttribute("age", p.getAge());
        model.addAttribute("profileLink", "/players/" + p.getId());

        model.addAttribute("alreadyRated", alreadyRated);
        model.addAttribute("existingSkill", existingSkill);
        model.addAttribute("existingBehavior", existingBehavior);

        return "profile";
    }
}
