package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.model.Review;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepo;
    private final PlayerRepository playerRepo;

    public AdminReviewController(ReviewRepository reviewRepo, PlayerRepository playerRepo) {
        this.reviewRepo = reviewRepo;
        this.playerRepo = playerRepo;
    }

    private boolean isAdmin(HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return false;
        Player current = playerRepo.findById(currentId).orElse(null);
        return current instanceof com.example.mas_implementation.model.IAdmin;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        Review r = reviewRepo.findById(id).orElse(null);
        if (r == null) return "redirect:/locations";
        Long locationId = r.getLocation() != null ? r.getLocation().getId() : null;
        reviewRepo.delete(r);
        if (locationId == null) return "redirect:/locations";
        return "redirect:/locations/" + locationId;
    }
}
