package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.State;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    @Query("""
        SELECT g FROM Game g
        WHERE
            (g.startDate > CURRENT_DATE)
            OR
            (g.startDate = CURRENT_DATE AND g.startTime > CURRENT_TIMESTAMP)
        ORDER BY g.startDate, g.startTime
    """)
    List<Game> findUpcomingGames();

    @Query("""
        SELECT g FROM Game g
        WHERE g.sport.id = :sportId
          AND (
            (g.startDate > CURRENT_DATE)
            OR
            (g.startDate = CURRENT_DATE AND g.startTime > CURRENT_TIMESTAMP)
          )
        ORDER BY g.startDate, g.startTime
    """)
    List<Game> findUpcomingGamesBySport(@Param("sportId") Long sportId);

    @Query("""
        SELECT g FROM Game g
        WHERE
            (g.startDate > CURRENT_DATE
             OR (g.startDate = CURRENT_DATE AND g.startTime > CURRENT_TIMESTAMP))
            AND (:sportId   IS NULL OR g.sport.id   = :sportId)
            AND (:freeOnly  = false  OR g.pricePerPerson IS NULL OR g.pricePerPerson = 0)
            AND (:date      IS NULL  OR g.startDate = :date)
            AND (:search    IS NULL  OR LOWER(g.title) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY g.startDate, g.startTime
    """)
    List<Game> findFiltered(
            @Param("sportId")  Long sportId,
            @Param("freeOnly") boolean freeOnly,
            @Param("date")     LocalDate date,
            @Param("search")   String search
    );

    /** Games still marked with the given state whose scheduled day is already in the past. */
    @Query("SELECT g FROM Game g WHERE g.state = :state AND g.startDate < :date")
    List<Game> findByStateAndStartDateBefore(@Param("state") State state,
                                             @Param("date") LocalDate date);
}
