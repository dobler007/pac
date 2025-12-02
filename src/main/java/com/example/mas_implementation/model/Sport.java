package com.example.mas_implementation.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Map;
import java.util.HashMap;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "title")
    private Map<String, Game> games = new HashMap<>();
}
