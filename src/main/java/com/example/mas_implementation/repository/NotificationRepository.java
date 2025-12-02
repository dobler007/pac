package com.example.mas_implementation.repository;

import com.example.mas_implementation.model.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
}
