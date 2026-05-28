package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Review;
import com.example.mas_implementation.repository.AdminRepository;
import com.example.mas_implementation.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepo;
    private final AdminRepository adminRepo;

    public AdminReviewController(ReviewRepository reviewRepo, AdminRepository adminRepo) {
        this.reviewRepo = reviewRepo;
        this.adminRepo  = adminRepo;
    }

    private boolean isAdmin(HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return false;
        return adminRepo.findById(currentId).isPresent();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        Review r = reviewRepo.findById(id).orElse(null);
        if (r == null) return "redirect:/locations";
        Long locationId = r.getLocation() != null ? r.getLocation().getId() : null;
        reviewRepo.delete(r);
        return locationId != null ? "redirect:/locations/" + locationId : "redirect:/locations";
    }
}
