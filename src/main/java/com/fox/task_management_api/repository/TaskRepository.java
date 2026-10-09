package com.fox.task_management_api.repository;

import com.fox.task_management_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task>findByUser_Username(String username);
}
