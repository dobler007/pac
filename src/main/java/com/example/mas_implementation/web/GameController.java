package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Location;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.model.State;
import com.example.mas_implementation.repository.GameRepository;
import com.example.mas_implementation.repository.LocationRepository;
import com.example.mas_implementation.repository.PlayerRepository;
import com.example.mas_implementation.repository.SportRepository;
import com.example.mas_implementation.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final SportRepository sportRepository;
    private final LocationRepository locationRepository;
    private final GameRepository gameRepository;

    public GameController(GameService gameService,
                          PlayerRepository playerRepository,
                          SportRepository sportRepository,
                          LocationRepository locationRepository,
                          GameRepository gameRepository) {
        this.gameService = gameService;
        this.playerRepository = playerRepository;
        this.sportRepository = sportRepository;
        this.locationRepository = locationRepository;
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameService.findUpcomingGames();
        model.addAttribute("games", games);
        return "games";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("sports", sportRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        return "new_game";
    }

    @PostMapping
    public String createGame(
            @RequestParam String title,
            @RequestParam Long sportId,

            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,

            @RequestParam String description,
            @RequestParam int capacity,
            @RequestParam(required = false) Integer pricePerPerson,
            @RequestParam LocalDate startDate,
            @RequestParam LocalTime startTime,
            HttpSession session
    ) {
        Game game = new Game();
        game.setTitle(title);
        game.setSport(sportRepository.findById(sportId).orElseThrow());
        game.setDescription(description);
        game.setCapacity(capacity);
        game.setPricePerPerson(pricePerPerson);

        game.setStartDate(startDate);
        game.setStartTime(LocalDateTime.of(startDate, startTime));
        game.setState(State.UPCOMING);

        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) {
            Player owner = playerRepository.findById(currentId).orElse(null);
            game.setOwner(owner);
            if (owner != null) {
                game.getPlayers().add(owner);
            }
        }

        Location location;
        if (locationId != null) {
            location = locationRepository.findById(locationId).orElse(null);
            if (location == null) return "redirect:/games/new";
        } else {
            if (locationName == null || locationName.isBlank()) return "redirect:/games/new";
            if (address == null || address.isBlank()) return "redirect:/games/new";
            if (latitude == null || longitude == null) return "redirect:/games/new";

            location = new Location();
            location.setName(locationName != null && !locationName.isBlank() ? locationName : "Custom Location");
            location.setAddress(address);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            locationRepository.save(location);
        }

        game.setLocation(location);

        gameService.saveGame(game);
        return "redirect:/games";
    }

    @GetMapping("/{id}")
    public String gameDetail(@PathVariable Long id, Model model, HttpSession session) {
        Game game = gameService.findGameById(id);
        model.addAttribute("game", game);

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
