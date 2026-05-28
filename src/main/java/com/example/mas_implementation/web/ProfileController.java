package com.example.mas_implementation.web;

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

    // ── View own profile ─────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String myProfile(Model model, HttpSession session) {
        Long id = (Long) session.getAttribute("currentUserId");
        if (id == null) return "redirect:/login";
        return viewProfile(id, model, session);
    }

    // ── View any player profile ──────────────────────────────────────────────

    @GetMapping("/players/{id}")
    public String viewProfile(@PathVariable Long id, Model model, HttpSession session) {
        Player p = playerRepo.findById(id).orElse(null);
        if (p == null) return "redirect:/";

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
        model.addAttribute("avgSkill",    String.format("%.1f", p.getAverageSkill()));
        model.addAttribute("avgBehavior", String.format("%.1f", p.getAverageBehavior()));
        model.addAttribute("age", p.getAge());
        model.addAttribute("profileLink", "/players/" + p.getId());
        model.addAttribute("isOwnProfile", currentId != null && currentId.equals(id));
        model.addAttribute("alreadyRated",    alreadyRated);
        model.addAttribute("existingSkill",   existingSkill);
        model.addAttribute("existingBehavior", existingBehavior);

        return "profile";
    }

    // ── Edit profile ─────────────────────────────────────────────────────────

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, HttpSession session) {
        Long id = (Long) session.getAttribute("currentUserId");
        if (id == null) return "redirect:/login";
        Player p = playerRepo.findById(id).orElse(null);
        if (p == null) return "redirect:/login";
        model.addAttribute("player", p);
        return "profile_edit";
    }

    @PostMapping("/profile/edit")
    public String saveProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "") String phoneNumber,
            HttpSession session) {

        Long id = (Long) session.getAttribute("currentUserId");
        if (id == null) return "redirect:/login";
        Player p = playerRepo.findById(id).orElse(null);
        if (p == null) return "redirect:/login";

        if (name != null && !name.isBlank())   p.setName(name.trim());
        if (email != null && !email.isBlank())  p.setEmail(email.trim());
        p.setPhoneNumber(phoneNumber.isBlank() ? null : phoneNumber.trim());

        playerRepo.save(p);
        return "redirect:/profile";
    }
}
