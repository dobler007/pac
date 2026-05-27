package com.example.mas_implementation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @Column(length = 500)
    private String description;
    private int capacity;
    private Integer pricePerPerson;
    private LocalDate startDate;
    private LocalDateTime startTime;
    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    @ManyToMany
    @JoinTable(
            name = "player_event",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> creators;
    @OneToMany(mappedBy = "event")
    private Set<EventSponsor> eventSponsors;
    @OneToMany(mappedBy = "event")
    @Builder.Default
    private List<Game> games = new ArrayList<>();
}
