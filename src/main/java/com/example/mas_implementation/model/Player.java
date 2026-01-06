package com.example.mas_implementation.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "id")
public class Player extends User {

    @ElementCollection
    @Size(max = 50)
    private List<@Min(1) @Max(5) Integer> skillMerits;

    @ElementCollection
    @Size(max = 50)
    private List<@Min(1) @Max(5) Integer> behaviorMerits;

    @ElementCollection
    @CollectionTable(
            name = "player_skill_ratings",
            joinColumns = @JoinColumn(name = "rated_player_id")
    )
    @MapKeyColumn(name = "rater_id")
    @Column(name = "skill", nullable = false)
    @Builder.Default
    private Map<Long, Integer> skillRatings = new HashMap<>();

    @ElementCollection
    @CollectionTable(
            name = "player_behavior_ratings",
            joinColumns = @JoinColumn(name = "rated_player_id")
    )
    @MapKeyColumn(name = "rater_id")
    @Column(name = "behavior", nullable = false)
    @Builder.Default
    private Map<Long, Integer> behaviorRatings = new HashMap<>();

    @ManyToMany
    @JoinTable(
            name = "player_game",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    @Builder.Default
    private Set<Game> games = new HashSet<>();

    public double getAverageSkill() {
        if (skillRatings != null && !skillRatings.isEmpty()) {
            return skillRatings.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        }
        return average(skillMerits);
    }

    public double getAverageBehavior() {
        if (behaviorRatings != null && !behaviorRatings.isEmpty()) {
            return behaviorRatings.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        }
        return average(behaviorMerits);
    }

    private double average(List<Integer> list) {
        return list == null || list.isEmpty()
                ? 0
                : DoubleStream.of(list.stream().mapToDouble(i -> i).toArray()).average().orElse(0);
    }
}
