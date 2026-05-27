package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Goal;
import com.example.mas_implementation.model.Player;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends CrudRepository<Goal, Long> {

    @EntityGraph(attributePaths = {"scorer"})
    List<Goal> findByGame(Game game);

    @EntityGraph(attributePaths = {"scorer", "game"})
    List<Goal> findByGameIn(Collection<Game> games);

    Optional<Goal> findTopByGameAndScorerOrderByIdDesc(Game game, Player scorer);

    long countByGameAndScorer(Game game, Player scorer);
}
