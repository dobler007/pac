package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends CrudRepository<Event, Long> {
    @Override
    @EntityGraph(attributePaths = {
            "creators",
            "eventSponsors"
    })
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {
            "creators"
    })
    List<Event> findAll();
}
