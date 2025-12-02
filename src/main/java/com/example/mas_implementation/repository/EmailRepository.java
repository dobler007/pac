package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Email;
import org.springframework.data.repository.CrudRepository;

public interface EmailRepository extends CrudRepository<Email, Long> {
}
