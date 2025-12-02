package com.example.mas_implementation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Size(min = 2, max = 20)
    private String login;
    @NotBlank
    private String email;
    @Size(min = 3)
    private String phoneNumber;
    @NotBlank
    @Size(min = 2)
    private String password;
    @NotNull
    @Past
    private LocalDate birthdate;
    @ManyToMany(mappedBy = "recipients")
    private Set<Notification> notifications;
    @OneToMany(mappedBy = "user")
    private Set<Review> reviews;

    public Object getAge() {
        return LocalDate.now().getYear() - birthdate.getYear();
    }
}
