package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {
    private final PlayerRepository playerRepo;

    public CurrentUserAdvice(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @ModelAttribute("currentUser")
    public Player currentUser(HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return null;
        return playerRepo.findById(currentId).orElse(null);
    }
}
