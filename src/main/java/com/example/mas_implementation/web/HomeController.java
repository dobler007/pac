package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PlayerRepository playerRepo;

    public HomeController(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) {
            playerRepo.findById(currentId).ifPresent(p -> model.addAttribute("currentUser", p));
        }
        return "index";
    }
}
