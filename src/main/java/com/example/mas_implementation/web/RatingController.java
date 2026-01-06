package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class RatingController {

    private final PlayerRepository playerRepo;

    public RatingController(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @PostMapping("/players/{id}/rate")
    @Transactional
    public String ratePlayer(
            @PathVariable("id") Long ratedId,
            @RequestParam int skill,
            @RequestParam int behavior,
            HttpSession session
    ) {
        Long raterId = (Long) session.getAttribute("currentUserId");
        if (raterId == null) return "redirect:/login";
        if (raterId.equals(ratedId)) return "redirect:/players/" + ratedId;
        if (skill < 1 || skill > 5 || behavior < 1 || behavior > 5) return "redirect:/players/" + ratedId;

        Player rated = playerRepo.findById(ratedId).orElse(null);
        if (rated == null) return "redirect:/";

        if (rated.getSkillRatings() == null) rated.setSkillRatings(new HashMap<>());
        if (rated.getBehaviorRatings() == null) rated.setBehaviorRatings(new HashMap<>());

        rated.getSkillRatings().put(raterId, skill);
        rated.getBehaviorRatings().put(raterId, behavior);

        playerRepo.save(rated);

        return "redirect:/players/" + ratedId;
    }
}
