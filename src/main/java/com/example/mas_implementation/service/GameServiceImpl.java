// src/main/java/com/example/mas_implementation/service/GameServiceImpl.java
package com.example.mas_implementation.service;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.repository.GameRepository;
import com.example.mas_implementation.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public GameServiceImpl(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Game> findUpcomingGames() {
        return gameRepository.findUpcomingGames();
    }

    @Override
    public List<Game> findAllGames() {
        return (List<Game>) gameRepository.findAll();
    }

    @Override
    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found"));
    }

    @Override
    public void joinGame(Long gameId, Long playerId) {
        Game game = findGameById(gameId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));

        // Already in the game
        boolean alreadyIn = game.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId));
        // Already on the waitlist
        boolean alreadyWaiting = game.getWaitList().stream()
                .anyMatch(p -> p.getId().equals(playerId));

        if (alreadyIn || alreadyWaiting) return;

        if (game.getPlayers().size() < game.getCapacity()) {
            // Spot available — join directly
            player.getGames().add(game);
            playerRepository.save(player);
        } else {
            // Game is full — add to waitlist queue
            game.getWaitList().add(player);
            gameRepository.save(game);
        }
    }

    @Override
    public void resignGame(Long gameId, Long playerId) {
        Game game = findGameById(gameId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));

        boolean inGame     = game.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
        boolean inWaitlist = game.getWaitList().stream().anyMatch(p -> p.getId().equals(playerId));

        if (inGame) {
            player.getGames().removeIf(g -> g.getId().equals(gameId));
            playerRepository.save(player);

            // Promote the first person on the waitlist
            if (!game.getWaitList().isEmpty()) {
                Player next = game.getWaitList().remove(0);
                next.getGames().add(game);
                playerRepository.save(next);
                gameRepository.save(game);
            }
        } else if (inWaitlist) {
            game.getWaitList().removeIf(p -> p.getId().equals(playerId));
            gameRepository.save(game);
        }
    }

    @Override
    public void leaveWaitlist(Long gameId, Long playerId) {
        Game game = findGameById(gameId);
        game.getWaitList().removeIf(p -> p.getId().equals(playerId));
        gameRepository.save(game);
    }

    @Override
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }
}
