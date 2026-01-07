package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Location;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.model.Review;
import com.example.mas_implementation.repository.LocationRepository;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationRepository locationRepo;
    private final ReviewRepository reviewRepo;
    private final PlayerRepository playerRepo;

    public LocationController(LocationRepository locationRepo, ReviewRepository reviewRepo, PlayerRepository playerRepo) {
        this.locationRepo = locationRepo;
        this.reviewRepo = reviewRepo;
        this.playerRepo = playerRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("locations", locationRepo.findAll());
        return "locations";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Location loc = locationRepo.findById(id).orElse(null);
        if (loc == null) return "redirect:/locations";

        List<Review> reviews = reviewRepo.findByLocationId(id);

        Long currentId = (Long) session.getAttribute("currentUserId");
        Review myReview = null;
        if (currentId != null) {
            myReview = reviewRepo.findByLocationIdAndUserId(id, currentId).orElse(null);
        }

        boolean isAdmin = false;
        if (currentId != null) {
            Player current = playerRepo.findById(currentId).orElse(null);
            isAdmin = (current instanceof com.example.mas_implementation.model.IAdmin);
        }

        model.addAttribute("location", loc);
        model.addAttribute("reviews", reviews);
        model.addAttribute("myReview", myReview);
        model.addAttribute("isAdmin", isAdmin);

        return "location";
    }

    @PostMapping("/{id}/review")
    public String addOrUpdateReview(@PathVariable Long id,
                                    @RequestParam String description,
                                    @RequestParam int stars,
                                    HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return "redirect:/login";
        if (stars < 1 || stars > 5) return "redirect:/locations/" + id;

        Location loc = locationRepo.findById(id).orElse(null);
        if (loc == null) return "redirect:/locations";

        Player user = playerRepo.findById(currentId).orElse(null);
        if (user == null) return "redirect:/login";

        String text = description == null ? "" : description.trim();
        if (text.isBlank()) return "redirect:/locations/" + id;
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

        return "redirect:/locations/" + id;
    }
}
