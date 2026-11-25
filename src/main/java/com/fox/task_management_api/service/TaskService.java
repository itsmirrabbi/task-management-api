package com.fox.task_management_api.service;

import com.fox.task_management_api.dto.CreateTaskRequest;
import com.fox.task_management_api.dto.TaskResponse;
import com.fox.task_management_api.dto.UpdateTaskRequest;
import com.fox.task_management_api.model.Task;
import com.fox.task_management_api.model.User;
import com.fox.task_management_api.repository.TaskRepository;
import com.fox.task_management_api.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return authentication.getName();
    }


    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    @Transactional
    public TaskResponse createTask(@NonNull CreateTaskRequest request) {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        return toTaskResponse(savedTask);
    }


    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyTasks(int page, int size) {
        return getMyTasks(page, size, null, "createdAt", "desc");
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyTasks(
            int page,
            int size,
            Boolean completed) {

        return getMyTasks(page, size, completed, "createdAt", "desc");
    }


    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyTasks(
            int page,
            int size,
            Boolean completed,
            String sortBy,
            String direction) {

        String username = getCurrentUsername();

        // Allow only valid task fields for sorting
        if (!List.of("id", "title", "completed", "createdAt", "updatedAt")
                .contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort direction"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Task> tasks;

        if (completed == null) {
            tasks = taskRepository.findByUser_Username(
                    username,
                    pageable
            );
        } else {
            tasks = taskRepository.findByUser_UsernameAndCompleted(
                    username,
                    completed,
                    pageable
            );
        }

        return tasks.map(this::toTaskResponse);
    }

    @Transactional
    public TaskResponse updateTask(
            Long id,
            UpdateTaskRequest request) {
        String username = getCurrentUsername();

        Task existingTask = taskRepository
                .findByIdAndUser_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        existingTask.setCompleted(request.getCompleted());

        Task updatedTask = taskRepository.save(existingTask);

        return toTaskResponse(updatedTask);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        String username = getCurrentUsername();

        Task task = taskRepository
                .findByIdAndUser_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        return toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        String username = getCurrentUsername();

        Task existingTask = taskRepository
                .findByIdAndUser_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        taskRepository.delete(existingTask);
    }
}
