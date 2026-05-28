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
import org.springframework.format.annotation.DateTimeFormat;
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

    private static final int PAGE_SIZE = 8;

    // ── List / search / page ─────────────────────────────────────────────────

    @GetMapping
    public String listGames(
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "false") boolean freeOnly,
            @RequestParam(defaultValue = "0") int page,
            Model model, HttpSession session) {

        String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;
        boolean hasFilter = sportId != null || searchTerm != null || date != null || freeOnly;

        List<Game> allGames = hasFilter
                ? gameRepository.findFiltered(sportId, freeOnly, date, searchTerm)
                : gameService.findUpcomingGames();

        // ── Manual pagination ────────────────────────────────────────────────
        int totalGames  = allGames.size();
        int totalPages  = Math.max(1, (int) Math.ceil((double) totalGames / PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));
        int from = page * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, totalGames);
        List<Game> games = from < totalGames ? allGames.subList(from, to) : List.of();

        model.addAttribute("games", games);
        model.addAttribute("sports", sportRepository.findAll());
        model.addAttribute("selectedSportId", sportId);
        model.addAttribute("search", search);
        model.addAttribute("selectedDate", date);
        model.addAttribute("freeOnly", freeOnly);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalGames", totalGames);

        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) {
            playerRepository.findById(currentId)
                    .ifPresent(p -> model.addAttribute("currentUser", p));
        }
        return "Games";
    }

    // ── Create ───────────────────────────────────────────────────────────────

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
            location.setName(locationName.trim());
            location.setAddress(address);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            locationRepository.save(location);
        }

        game.setLocation(location);
        gameService.saveGame(game);
        return "redirect:/games";
    }

    // ── Detail ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String gameDetail(@PathVariable Long id, Model model, HttpSession session) {
        Game game = gameService.findGameById(id);
        model.addAttribute("game", game);

        Long currentId = (Long) session.getAttribute("currentUserId");
        Player currentPlayer = null;
        if (currentId != null) {
            currentPlayer = playerRepository.findById(currentId).orElse(null);
            model.addAttribute("currentUser", currentPlayer);
        } else {
            model.addAttribute("currentUser", null);
        }

        // Waitlist helpers for the template
        boolean isFull = game.getPlayers().size() >= game.getCapacity();
        boolean onWaitList = false;
        int waitPosition = 0;
        if (currentPlayer != null) {
            final Long cid = currentId;
            List<Player> wl = game.getWaitList();
            for (int i = 0; i < wl.size(); i++) {
                if (wl.get(i).getId().equals(cid)) {
                    onWaitList = true;
                    waitPosition = i + 1;
                    break;
                }
            }
        }
        model.addAttribute("isFull", isFull);
        model.addAttribute("onWaitList", onWaitList);
        model.addAttribute("waitPosition", waitPosition);

        return "game";
    }

    // ── Join / resign / waitlist ──────────────────────────────────────────────

    @PostMapping("/{id}/join")
    public String joinGame(@PathVariable Long id, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) gameService.joinGame(id, currentId);
        return "redirect:/games/" + id;
    }

    @PostMapping("/{id}/resign")
    public String resignGame(@PathVariable Long id, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) gameService.resignGame(id, currentId);
        return "redirect:/games/" + id;
    }

    @PostMapping("/{id}/waitlist/leave")
    public String leaveWaitlist(@PathVariable Long id, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId != null) gameService.leaveWaitlist(id, currentId);
        return "redirect:/games/" + id;
    }
}
