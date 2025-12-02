package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends CrudRepository<Review, Long> {
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "location"
    })
    Optional<Review> findById(Long id);


    @EntityGraph(attributePaths = {
            "user"
    })
    List<Review> findByLocationId(Long locationId);
}
