package com.example.mas_implementation.service;

import com.example.mas_implementation.model.Game;
import java.util.List;

public interface GameService {
    List<Game> findAllGames();
    Game findGameById(Long gameId);
    void joinGame(Long gameId, Long playerId);
    void resignGame(Long gameId, Long playerId);
    Game saveGame(Game game);
}
