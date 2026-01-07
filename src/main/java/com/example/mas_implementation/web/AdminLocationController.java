package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Location;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.LocationRepository;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/locations")
public class AdminLocationController {

    private final LocationRepository locationRepo;
    private final PlayerRepository playerRepo;

    public AdminLocationController(LocationRepository locationRepo, PlayerRepository playerRepo) {
        this.locationRepo = locationRepo;
        this.playerRepo = playerRepo;
    }

    private boolean isAdmin(HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return false;
        Player current = playerRepo.findById(currentId).orElse(null);
        return current instanceof com.example.mas_implementation.model.IAdmin;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("locations", locationRepo.findAll());
        return "admin_locations";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        return "admin_location_new";
    }

    @PostMapping
    public String create(@RequestParam String name,
                         @RequestParam String address,
                         @RequestParam Double latitude,
                         @RequestParam Double longitude,
                         HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";

        if (name == null || name.isBlank()) return "redirect:/admin/locations/new";
        if (address == null || address.isBlank()) return "redirect:/admin/locations/new";
        if (latitude == null || longitude == null) return "redirect:/admin/locations/new";

        Location loc = new Location();
        loc.setName(name.trim());
        loc.setAddress(address.trim());
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);

        locationRepo.save(loc);
        return "redirect:/admin/locations";
    }
}
