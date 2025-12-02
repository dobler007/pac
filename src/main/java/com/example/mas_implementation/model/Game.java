package com.example.mas_implementation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @ManyToOne(optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;
    @Column(length = 500)
    private String description;
    private int capacity;
    private Integer pricePerPerson;
    private LocalDate startDate;
    private LocalDateTime startTime;
    @ManyToMany(mappedBy = "games")
    private Set<Player> players = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "game_waitlist",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> waitList = new HashSet<>();
    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Player owner;
    @Enumerated(EnumType.STRING)
    private State state;
}
