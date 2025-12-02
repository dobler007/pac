package com.example.mas_implementation.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private int maxCharacters;
    @ManyToMany
    @JoinTable(
            name = "user_notification",
            joinColumns = @JoinColumn(name = "notification_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> recipients;
    @OneToOne(mappedBy = "notification", cascade = CascadeType.ALL)
    private Email emailNot;
    @OneToOne(mappedBy = "notification", cascade = CascadeType.ALL)
    private Text text;
    public void send() {}
}
