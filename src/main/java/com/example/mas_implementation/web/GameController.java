package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Location;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.model.Sport;
import com.example.mas_implementation.model.State;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.repository.SportRepository;
import com.example.mas_implementation.repository.LocationRepository;
import com.example.mas_implementation.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final SportRepository sportRepository;
    private final LocationRepository locationRepository;

    public GameController(GameService gameService,
                          PlayerRepository playerRepository,
                          SportRepository sportRepository,
                          LocationRepository locationRepository) {
        this.gameService = gameService;
        this.playerRepository = playerRepository;
        this.sportRepository = sportRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameService.findAllGames();
        model.addAttribute("games", games);
        return "games";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("sports", sportRepository.findAll());
        return "new_game";
    }

    @PostMapping
    public String createGame(
            @RequestParam String title,
            @RequestParam Long sportId,
            @RequestParam String locationName,
            @RequestParam String address,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String description,
            @RequestParam int capacity,
            @RequestParam(required = false) Integer pricePerPerson,
            HttpSession session
    ) {
        Game game = new Game();
        game.setTitle(title);
        game.setSport(sportRepository.findById(sportId).orElseThrow());
        game.setDescription(description);
        game.setCapacity(capacity);
        game.setPricePerPerson(pricePerPerson);
        game.setStartDate(LocalDate.now());
        game.setStartTime(LocalDateTime.now());
        game.setState(State.UPCOMING);

        // Owner
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) {
            Player owner = playerRepository.findById(currentId).orElse(null);
            game.setOwner(owner);
            game.getPlayers().add(owner);
        }

        // Location
        Location location = new Location();
        location.setName(locationName != null && !locationName.isBlank() ? locationName : "Custom Location");
        location.setAddress(address);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        locationRepository.save(location);

        game.setLocation(location);

        gameService.saveGame(game);
        return "redirect:/games";
    }

    @GetMapping("/{id}")
    public String gameDetail(@PathVariable Long id, Model model, HttpSession session) {
        Game game = gameService.findGameById(id);
        model.addAttribute("game", game);

        // Current user
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) {
            Player currentUser = playerRepository.findById(currentId).orElse(null);
            model.addAttribute("currentUser", currentUser);
        } else {
            model.addAttribute("currentUser", null);
        }

        return "game";
    }

    @PostMapping("/{id}/join")
    public String joinGame(@PathVariable Long id, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        gameService.joinGame(id, currentId);
        return "redirect:/games/" + id;
    }

    @PostMapping("/{id}/resign")
    public String resignGame(@PathVariable Long id, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        gameService.resignGame(id, currentId);
        return "redirect:/games/" + id;
    }
}
