
package com.fox.task_management_api.service;

import com.fox.task_management_api.model.Task;
import com.fox.task_management_api.model.User;
import com.fox.task_management_api.repository.TaskRepository;
import com.fox.task_management_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }

    public Task createTask(Task task) {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        task.setUser(user);
        taskRepository.save(task);

        return task;
    }

    public List<Task> getMyTasks() {
        String username = getCurrentUsername();

        return taskRepository.findByUser_Username(username);
    }
}