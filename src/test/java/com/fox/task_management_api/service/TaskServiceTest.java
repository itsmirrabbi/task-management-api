package com.fox.task_management_api.service;


import com.fox.task_management_api.dto.CreateTaskRequest;
import com.fox.task_management_api.dto.TaskResponse;
import com.fox.task_management_api.dto.UpdateTaskRequest;
import com.fox.task_management_api.model.Task;
import com.fox.task_management_api.model.User;
import com.fox.task_management_api.repository.TaskRepository;
import com.fox.task_management_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private void authenticateAs(String username) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        java.util.Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private Task createTask(Long id, String title, String description, boolean completed) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);

        return task;
    }

    @Test
    void getTaskById_shouldReturnTask_whenTaskExists() {
        // Arrange
        String username = "testuser";
        Long taskId = 1L;

        Task task = createTask(
                taskId,
                "Test Task",
                "Test Description",
                false
        );

        authenticateAs(username);

        when(taskRepository.findByIdAndUser_Username(taskId, username))
                .thenReturn(Optional.of(task));
        // Act
        TaskResponse response = taskService.getTaskById(taskId);

        // Assert
        assertEquals(taskId, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertFalse(response.isCompleted());


    }

    @Test
    void getTaskById_shouldThrowNotFound_whenTaskDoesNotExist() {
        // Arrange
        String username = "testuser";
        Long taskId = 999L;

        authenticateAs(username);
        when(taskRepository.findByIdAndUser_Username(taskId, username))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTaskById(taskId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Task not found", exception.getReason());

    }

    @Test
    void createTask_shouldReturnTaskResponse_whenUserExists() {


        String username = "testuser";

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Task Description");

        authenticateAs(username);
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        TaskResponse response = taskService.createTask(request);

        assertEquals("New Task", response.getTitle());
        assertEquals("New Task Description", response.getDescription());
        assertFalse(response.isCompleted());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrowNotFound_whenUserDoesNotExist() {

        // Arrange
        String username = "testuser";

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Task Description");
        authenticateAs(username);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());

        verify(taskRepository, never()).save(any(Task.class));
    }


    @Test
    void updateTask_shouldReturnUpdatedTask_whenTaskExists() {

        // Arrange
        String username = "testuser";
        Long taskId = 1L;

        authenticateAs(username);


        Task existingTask = createTask(
                taskId,
                "Old Task",
                "Old Description",
                false
        );

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setCompleted(true);

        when(taskRepository.findByIdAndUser_Username(taskId, username))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.updateTask(taskId, request);

        // Assert
        assertEquals(taskId, response.getId());
        assertEquals("Updated Task", response.getTitle());
        assertEquals("Updated Description", response.getDescription());
        assertTrue(response.isCompleted());

        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void updateTask_shouldThrowNotFound_whenTaskDoesNotExist() {

        // Arrange
        String username = "testuser";
        Long taskId = 999L;

        authenticateAs(username);
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setCompleted(true);

        when(taskRepository.findByIdAndUser_Username(taskId, username))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(taskId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Task not found", exception.getReason());

        verify(taskRepository, never()).save(any(Task.class));
    }


    @Test
    void deleteTask_shouldDeleteTask_whenTaskExists() {

        // Arrange
        String username = "testuser";
        Long taskId = 1L;

        authenticateAs(username);

        Task task = createTask(
                taskId,
                "Test Task",
                "Test Description",
                false
        );
        when(taskRepository.findByIdAndUser_Username(taskId, username))
                .thenReturn(Optional.of(task));

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_shouldThrowNotFound_whenTaskDoesNotExist() {
        // Arrange
        String username = "testuser";
        Long taskId = 1L;

        authenticateAs(username);


        when(taskRepository.findByIdAndUser_Username(taskId, username))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                taskService.deleteTask(taskId)
        );

        // Assert
        verify(taskRepository, never()).delete(any(Task.class));

    }


    @Test
    void getMyTasks_shouldReturnPaginatedTasks_forCurrentUser() {

        // Arrange
        String username = "testuser";
        int page = 0;
        int size = 10;

        authenticateAs(username);

        Task task = createTask(
                1L,
                "Test Task",
                "Test Description",
                false
        );

        Page<Task> taskPage = new PageImpl<>(List.of(task),
                PageRequest.of(page, size), 1
        );

        when(taskRepository.findByUser_Username(
                eq(username),
                any(Pageable.class)
        )).thenReturn(taskPage);

        // Act
        Page<TaskResponse> response = taskService.getMyTasks(page, size);

        // Assert
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("Test Task", response.getContent().get(0).getTitle());
        assertEquals(0, response.getNumber());
        assertEquals(10, response.getSize());

        verify(taskRepository).findByUser_Username(
                eq(username),
                any(Pageable.class)
        );
    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
