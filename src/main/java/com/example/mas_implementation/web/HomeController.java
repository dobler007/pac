package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PlayerRepository playerRepo;
    private final GameService gameService;

    public HomeController(PlayerRepository playerRepo, GameService gameService) {
        this.playerRepo = playerRepo;
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) {
            playerRepo.findById(currentId).ifPresent(p -> model.addAttribute("currentUser", p));
        }
        model.addAttribute("games", gameService.findUpcomingGames());
        return "index";
    }
}
