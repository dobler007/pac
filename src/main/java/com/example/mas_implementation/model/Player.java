// src/main/java/com/example/mas_implementation/model/Player.java
package com.example.mas_implementation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
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

    @ManyToMany
    @JoinTable(
            name = "player_game",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    @Builder.Default
    private Set<Game> games = new HashSet<>();

    public double getAverageSkill() {
        return average(skillMerits);
    }

    public double getAverageBehavior() {
        return average(behaviorMerits);
    }

    private double average(List<Integer> list) {
        return list == null || list.isEmpty()
                ? 0
                : DoubleStream.of(list.stream().mapToDouble(i -> i).toArray()).average().orElse(0);
    }
}
