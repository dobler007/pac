package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
