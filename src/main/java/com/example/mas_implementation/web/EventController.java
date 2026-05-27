package com.example.mas_implementation.web;

import com.example.mas_implementation.model.*;
import com.example.mas_implementation.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
public class EventController {

    public record TopScorer(Player player, long goals) {}

    public record GameBoard(Game game, Map<Long, Long> goalsByPlayerId) {
        public long goalsFor(Long playerId) {
            return goalsByPlayerId.getOrDefault(playerId, 0L);
        }
    }

    private final EventRepository eventRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final GoalRepository goalRepository;
    private final SportRepository sportRepository;
    private final LocationRepository locationRepository;

    public EventController(EventRepository eventRepository,
                           PlayerRepository playerRepository,
                           GameRepository gameRepository,
                           GoalRepository goalRepository,
                           SportRepository sportRepository,
                           LocationRepository locationRepository) {
        this.eventRepository = eventRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.goalRepository = goalRepository;
        this.sportRepository = sportRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "events";
    }

    @GetMapping("/new")
    public String showNewEventForm(Model model, HttpSession session) {
        if (session.getAttribute("currentUserId") == null) return "redirect:/login";
        model.addAttribute("sports", sportRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        return "new_event";
    }

    @PostMapping
    public String createEvent(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Integer pricePerPerson,
            HttpSession session
    ) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return "redirect:/login";

        Player creator = playerRepository.findById(currentId).orElse(null);
        if (creator == null) return "redirect:/login";

        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setCapacity(capacity != null ? capacity : 0);
        event.setPricePerPerson(pricePerPerson);
        if (startDate != null) event.setStartDate(startDate);
        if (startDate != null && startTime != null)
            event.setStartTime(LocalDateTime.of(startDate, startTime));

        if (sportId != null)
            sportRepository.findById(sportId).ifPresent(event::setSport);

        Location location = null;
        if (locationId != null) {
            location = locationRepository.findById(locationId).orElse(null);
        } else if (locationName != null && !locationName.isBlank()
                && address != null && !address.isBlank()
                && latitude != null && longitude != null) {
            location = new Location();
            location.setName(locationName);
            location.setAddress(address);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            locationRepository.save(location);
        }
        event.setLocation(location);

        event.setCreators(new java.util.HashSet<>(Set.of(creator)));
        eventRepository.save(event);
        return "redirect:/events/" + event.getId();
    }

    @GetMapping("/{id}")
    public String eventDetail(@PathVariable Long id, Model model, HttpSession session) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        Long currentId = (Long) session.getAttribute("currentUserId");
        Player currentPlayer = currentId != null
                ? playerRepository.findById(currentId).orElse(null)
                : null;

        boolean isOwner = currentPlayer != null &&
                event.getCreators() != null &&
                event.getCreators().stream().anyMatch(c -> c.getId().equals(currentId));

        List<GameBoard> boards = buildBoards(event);
        List<TopScorer> topScorers = buildTopScorers(event);

        model.addAttribute("event", event);
        model.addAttribute("currentUser", currentPlayer);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("boards", boards);
        model.addAttribute("topScorers", topScorers);
        model.addAttribute("states", State.values());
        return "event";
    }

    @PostMapping("/{eventId}/games/{gameId}/goals/add")
    public String addGoal(@PathVariable Long eventId,
                          @PathVariable Long gameId,
                          @RequestParam Long playerId,
                          HttpSession session) {
        if (!isEventOwner(eventId, session)) return "redirect:/events/" + eventId;

        Game game = gameRepository.findById(gameId).orElseThrow();
        Player scorer = playerRepository.findById(playerId).orElseThrow();

        goalRepository.save(Goal.builder().game(game).scorer(scorer).build());
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/{eventId}/games/{gameId}/goals/remove-last")
    public String removeLastGoal(@PathVariable Long eventId,
                                 @PathVariable Long gameId,
                                 @RequestParam Long playerId,
                                 HttpSession session) {
        if (!isEventOwner(eventId, session)) return "redirect:/events/" + eventId;

        Game game = gameRepository.findById(gameId).orElseThrow();
        Player scorer = playerRepository.findById(playerId).orElseThrow();

        goalRepository.findTopByGameAndScorerOrderByIdDesc(game, scorer)
                .ifPresent(goalRepository::delete);
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/{eventId}/games/{gameId}/state")
    public String updateGameState(@PathVariable Long eventId,
                                  @PathVariable Long gameId,
                                  @RequestParam State state,
                                  HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return "redirect:/events/" + eventId;

        Game game = gameRepository.findById(gameId).orElseThrow();

        boolean isEventOwner = isEventOwner(eventId, session);
        boolean isGameOwner = game.getOwner() != null && game.getOwner().getId().equals(currentId);
        if (!isEventOwner && !isGameOwner) return "redirect:/events/" + eventId;

        game.setState(state);
        gameRepository.save(game);
        return "redirect:/events/" + eventId;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean isEventOwner(Long eventId, HttpSession session) {
        Long currentId = (Long) session.getAttribute("currentUserId");
        if (currentId == null) return false;
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null || event.getCreators() == null) return false;
        return event.getCreators().stream().anyMatch(c -> c.getId().equals(currentId));
    }

    private List<GameBoard> buildBoards(Event event) {
        if (event.getGames() == null || event.getGames().isEmpty()) return List.of();
        List<Goal> allGoals = goalRepository.findByGameIn(event.getGames());
        Map<Long, Map<Long, Long>> byGame = allGoals.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getGame().getId(),
                        Collectors.groupingBy(g -> g.getScorer().getId(), Collectors.counting())
                ));
        return event.getGames().stream()
                .sorted(Comparator.comparing(g -> g.getStartDate() != null ? g.getStartDate() : java.time.LocalDate.MAX))
                .map(game -> new GameBoard(game, byGame.getOrDefault(game.getId(), Map.of())))
                .toList();
    }

    private List<TopScorer> buildTopScorers(Event event) {
        if (event.getGames() == null || event.getGames().isEmpty()) return List.of();
        List<Goal> allGoals = goalRepository.findByGameIn(event.getGames());
        return allGoals.stream()
                .collect(Collectors.groupingBy(Goal::getScorer, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Player, Long>comparingByValue().reversed())
                .map(e -> new TopScorer(e.getKey(), e.getValue()))
                .toList();
    }
}
