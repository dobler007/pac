package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT r FROM Review r
        WHERE r.location.id = :locationId AND r.user.id = :userId
    """)
    Optional<Review> findByLocationIdAndUserId(@Param("locationId") Long locationId, @Param("userId") Long userId);
}
