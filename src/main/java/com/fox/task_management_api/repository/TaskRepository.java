package com.fox.task_management_api.repository;

import com.fox.task_management_api.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByUser_Username(
            String username,
            Pageable pageable
    );

    Page<Task> findByUser_UsernameAndCompleted(
            String username,
            boolean completed,
            Pageable pageable
    );

    Optional<Task> findByIdAndUser_Username(
            Long id,
            String username
    );
}
