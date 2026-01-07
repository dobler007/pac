package com.example.mas_implementation.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlayerAdmin extends Player implements IAdmin {
    private double discount;

    private int experience;

    private String responsibilities;
    @Override
    public void deleteReview() {}

    @Override
    public void createNotification() {}

    @Override
    public void deleteNotification() {}
}
