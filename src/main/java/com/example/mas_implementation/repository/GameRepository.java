package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GameRepository extends CrudRepository<Game, Long> {
    @Override
    @EntityGraph(attributePaths = {
            "sport",
            "location",
            "owner",
            "players",
            "waitList"
    })
    Optional<Game> findById(Long id);
}
