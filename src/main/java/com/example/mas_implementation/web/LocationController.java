package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Location;
import com.example.mas_implementation.model.Review;
import com.example.mas_implementation.model.User;
import com.example.mas_implementation.repository.AdminRepository;
import com.example.mas_implementation.repository.LocationRepository;
import com.example.mas_implementation.repository.ReviewRepository;
import com.example.mas_implementation.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationRepository locationRepo;
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final AdminRepository adminRepo;

    public LocationController(LocationRepository locationRepo,
                              ReviewRepository reviewRepo,
                              UserRepository userRepo,
                              AdminRepository adminRepo) {
        this.locationRepo = locationRepo;
        this.reviewRepo   = reviewRepo;
        this.userRepo     = userRepo;
        this.adminRepo    = adminRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("locations", locationRepo.findAll());
        return "locations";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String returnTo,
                         Model model, HttpSession session) {
        Location loc = locationRepo.findById(id).orElse(null);
        if (loc == null) return "redirect:/locations";

        List<Review> reviews = reviewRepo.findByLocationId(id);

        // Compute average stars in Java — Thymeleaf can't project collections
        double avgStars = reviews.stream()
                .mapToInt(Review::getStars)
                .average()
                .orElse(0.0);

        Long currentId = (Long) session.getAttribute("currentUserId");
        Review myReview = null;
        boolean isAdmin = false;
        if (currentId != null) {
            myReview = reviewRepo.findByLocationIdAndUserId(id, currentId).orElse(null);
            isAdmin  = adminRepo.findById(currentId).isPresent();
        }

        model.addAttribute("location", loc);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgStars", avgStars);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("myReview", myReview);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("returnTo", returnTo);

        return "location";
    }

    @Transactional
    @PostMapping("/{id}/review")
    public String addOrUpdateReview(@PathVariable Long id,
                                    @RequestParam(required = false, defaultValue = "") String description,
                                    @RequestParam(defaultValue = "5") int stars,
                                    @RequestParam(required = false) String returnTo,
                                    HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return "redirect:/login";
        if (stars < 1 || stars > 5) stars = 5;

        Location loc = locationRepo.findById(id).orElse(null);
        if (loc == null) return "redirect:/locations";

        User user = userRepo.findById(currentId).orElse(null);
        if (user == null) return "redirect:/login";

        String text = description.trim();
        if (text.length() > 1000) text = text.substring(0, 1000);

        Review existing = reviewRepo.findByLocationIdAndUserId(id, currentId).orElse(null);
        if (existing != null) {
            existing.setDescription(text);
            existing.setStars(stars);
            reviewRepo.save(existing);
        } else {
            Review r = new Review();
            r.setLocation(loc);
            r.setUser(user);
            r.setDescription(text);
            r.setStars(stars);
            reviewRepo.save(r);
        }

        // Return to wherever the user came from (e.g. game detail page)
        if (returnTo != null && !returnTo.isBlank() && returnTo.startsWith("/")) {
            return "redirect:" + returnTo;
        }
        return "redirect:/locations/" + id;
    }
}
