package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Player;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    @EntityGraph(value = "Player.full", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Player> findById(Long id);

    @EntityGraph(value = "Player.full", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Player> findByLogin(String login);

}
