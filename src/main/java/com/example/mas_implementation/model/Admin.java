package com.example.mas_implementation.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "id")
public class Admin extends User implements IAdmin {
    private int experience;
    private String responsibilities;

    @Override
    public void deleteReview() {}

    @Override
    public void createNotification() {}

    @Override
    public void deleteNotification() {}
}
