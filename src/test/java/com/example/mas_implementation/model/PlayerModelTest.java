package com.example.mas_implementation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerModelTest {

    private Player newPlayer() {
        Player p = new Player();
        p.setLogin("testplayer");
        p.setName("Test Player");
        p.setEmail("test@test.com");
        p.setPassword("pass");
        p.setBirthdate(LocalDate.of(1995, 3, 20));
        return p;
    }

    @Test
    void getAverageSkill_returnsCorrectAverage() {
        Player p = newPlayer();
        p.setSkillRatings(Map.of(1L, 4, 2L, 2, 3L, 3));  // average = 3.0

        assertThat(p.getAverageSkill()).isEqualTo(3.0);
    }

    @Test
    void getAverageBehavior_returnsCorrectAverage() {
        Player p = newPlayer();
        p.setBehaviorRatings(Map.of(1L, 5, 2L, 5));  // average = 5.0

        assertThat(p.getAverageBehavior()).isEqualTo(5.0);
    }

    @Test
    void getAverageSkill_returnsZero_whenNoRatings() {
        Player p = newPlayer();
        // no ratings set

        assertThat(p.getAverageSkill()).isEqualTo(0.0);
    }

    @Test
    void getAge_returnsCorrectAge() {
        // Born in 1995, current year from LocalDate.now()
        Player p = newPlayer();
        int expected = LocalDate.now().getYear() - 1995;

        assertThat(p.getAge()).isEqualTo(expected);
    }
}
