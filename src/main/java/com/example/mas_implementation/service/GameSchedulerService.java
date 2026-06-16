package com.example.mas_implementation.service;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.State;
import com.example.mas_implementation.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Moves games from {@link State#UPCOMING} to {@link State#FINISHED} once their scheduled day
 * has passed. The transition is driven by the calendar, not by a user action, so it runs as a
 * scheduled task rather than from a controller.
 *
 * <p>The job runs every day just after midnight. Scheduling must be enabled at the application
 * level with {@code @EnableScheduling}.
 */
@Service
public class GameSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(GameSchedulerService.class);

    private final GameRepository gameRepository;

    public GameSchedulerService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /** Runs daily at 00:05 server time. */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void markPastGamesFinished() {
        List<Game> finished = gameRepository.findByStateAndStartDateBefore(State.UPCOMING, LocalDate.now());
        if (finished.isEmpty()) {
            return;
        }
        finished.forEach(game -> game.setState(State.FINISHED));
        gameRepository.saveAll(finished);
        log.info("Game scheduler marked {} game(s) as FINISHED.", finished.size());
    }
}
