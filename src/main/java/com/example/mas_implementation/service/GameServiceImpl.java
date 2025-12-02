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
        if (player.getGames().contains(game)) {
            return;
        }
        player.getGames().add(game);
        playerRepository.save(player);
    }

    @Override
    public void resignGame(Long gameId, Long playerId) {
        Game game = findGameById(gameId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        player.getGames().remove(game);
        playerRepository.save(player);
    }

    @Override
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }
}
