package com.example.mas_implementation.service;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.GameRepository;
import com.example.mas_implementation.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock private GameRepository gameRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private GameServiceImpl gameService;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Player makePlayer(Long id) {
        Player p = new Player();
        p.setId(id);
        p.setLogin("player" + id);
        p.setName("Player " + id);
        p.setEmail("p" + id + "@test.com");
        p.setPassword("pass");
        p.setBirthdate(LocalDate.of(1995, 6, 15));
        p.setGames(new HashSet<>());
        return p;
    }

    private Game makeGame(Long id, int capacity, Set<Player> players, List<Player> waitList) {
        Game g = new Game();
        g.setId(id);
        g.setCapacity(capacity);
        g.setPlayers(players);
        g.setWaitList(waitList);
        return g;
    }

    // ── joinGame ─────────────────────────────────────────────────────────────

    @Test
    void joinGame_addsPlayerDirectly_whenSpotAvailable() {
        Player existing = makePlayer(1L);
        Player newcomer = makePlayer(2L);
        Game game = makeGame(10L, 2, new HashSet<>(Set.of(existing)), new ArrayList<>());

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(newcomer));

        gameService.joinGame(10L, 2L);

        assertThat(newcomer.getGames()).contains(game);
        verify(playerRepository).save(newcomer);
        verify(gameRepository, never()).save(any());   // waitlist NOT touched
    }

    @Test
    void joinGame_addsPlayerToWaitlist_whenGameFull() {
        Player existing = makePlayer(1L);
        Player newcomer = makePlayer(2L);
        // capacity=1, already 1 player → full
        Game game = makeGame(10L, 1, new HashSet<>(Set.of(existing)), new ArrayList<>());

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(newcomer));

        gameService.joinGame(10L, 2L);

        assertThat(game.getWaitList()).contains(newcomer);
        verify(gameRepository).save(game);
        verify(playerRepository, never()).save(any()); // player_game NOT touched
    }

    @Test
    void joinGame_isNoOp_whenPlayerAlreadyInGame() {
        Player p = makePlayer(1L);
        Game game = makeGame(10L, 5, new HashSet<>(Set.of(p)), new ArrayList<>());

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));

        gameService.joinGame(10L, 1L);

        verify(playerRepository, never()).save(any());
        verify(gameRepository,  never()).save(any());
    }

    @Test
    void joinGame_isNoOp_whenPlayerAlreadyOnWaitlist() {
        Player p = makePlayer(1L);
        Player other = makePlayer(2L);
        // full game, p is already on waitlist
        Game game = makeGame(10L, 1, new HashSet<>(Set.of(other)), new ArrayList<>(List.of(p)));

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));

        gameService.joinGame(10L, 1L);

        verify(playerRepository, never()).save(any());
        verify(gameRepository,  never()).save(any());
    }

    // ── resignGame ───────────────────────────────────────────────────────────

    @Test
    void resignGame_removesPlayer_andPromotesFirstWaitlistEntry() {
        Player resigning  = makePlayer(1L);
        Player waitlisted = makePlayer(2L);

        Game game = makeGame(10L, 1,
                new HashSet<>(Set.of(resigning)),
                new ArrayList<>(List.of(waitlisted)));

        resigning.setGames(new HashSet<>(Set.of(game)));

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(resigning));

        gameService.resignGame(10L, 1L);

        // Resigning player no longer in game
        assertThat(resigning.getGames()).doesNotContain(game);
        // Waitlisted player promoted
        assertThat(waitlisted.getGames()).contains(game);
        assertThat(game.getWaitList()).isEmpty();
        verify(playerRepository).save(resigning);
        verify(playerRepository).save(waitlisted);
    }

    @Test
    void resignGame_noPromotion_whenWaitlistEmpty() {
        Player p = makePlayer(1L);
        Game game = makeGame(10L, 2, new HashSet<>(Set.of(p)), new ArrayList<>());
        p.setGames(new HashSet<>(Set.of(game)));

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));

        gameService.resignGame(10L, 1L);

        assertThat(p.getGames()).doesNotContain(game);
        assertThat(game.getWaitList()).isEmpty();
        verify(gameRepository, never()).save(any()); // no waitlist update needed
    }

    // ── leaveWaitlist ─────────────────────────────────────────────────────────

    @Test
    void leaveWaitlist_removesPlayerFromQueue() {
        Player p1 = makePlayer(1L);
        Player p2 = makePlayer(2L);
        Game game = makeGame(10L, 1,
                new HashSet<>(),
                new ArrayList<>(List.of(p1, p2)));

        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));

        gameService.leaveWaitlist(10L, 1L);

        assertThat(game.getWaitList()).doesNotContain(p1);
        assertThat(game.getWaitList()).contains(p2);   // p2 still on list
        verify(gameRepository).save(game);
    }
}
