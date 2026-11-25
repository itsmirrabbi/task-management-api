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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

    @Test
    void getMyTasks_shouldReturnCompletedTasks_whenCompletedIsTrue() {

        // Arrange
        String username = "testuser";
        int page = 0;
        int size = 10;

        authenticateAs(username);

        Task task = createTask(
                1L,
                "Completed Task",
                "Task description",
                true
        );

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                PageRequest.of(page, size),
                1
        );

        when(taskRepository.findByUser_UsernameAndCompleted(
                eq(username),
                eq(true),
                any(Pageable.class)
        )).thenReturn(taskPage);

        // Act
        Page<TaskResponse> response =
                taskService.getMyTasks(page, size, true);

        // Assert
        assertEquals(1, response.getTotalElements());
        assertEquals("Completed Task", response.getContent().get(0).getTitle());
        assertTrue(response.getContent().get(0).isCompleted());

        verify(taskRepository).findByUser_UsernameAndCompleted(
                eq(username),
                eq(true),
                any(Pageable.class)
        );
    }

    @Test
    void getMyTasks_shouldReturnIncompleteTasks_whenCompletedIsFalse() {

        // Arrange
        String username = "testuser";
        int page = 0;
        int size = 10;

        authenticateAs(username);

        Task task = createTask(
                2L,
                "Incomplete Task",
                "Task description",
                false
        );

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                PageRequest.of(page, size),
                1
        );

        when(taskRepository.findByUser_UsernameAndCompleted(
                eq(username),
                eq(false),
                any(Pageable.class)
        )).thenReturn(taskPage);

        // Act
        Page<TaskResponse> response =
                taskService.getMyTasks(page, size, false);

        // Assert
        assertEquals(1, response.getTotalElements());
        assertEquals("Incomplete Task", response.getContent().get(0).getTitle());
        assertFalse(response.getContent().get(0).isCompleted());

        verify(taskRepository).findByUser_UsernameAndCompleted(
                eq(username),
                eq(false),
                any(Pageable.class)
        );
    }


    @Test
    void getMyTasks_shouldUseDefaultSorting_whenSortParametersAreNotProvided() {
        // Arrange
        String username = "testuser";
        int page = 0;
        int size = 10;

        authenticateAs(username);

        Page<Task> taskPage = new PageImpl<>(
                List.of(),
                PageRequest.of(page, size),
                0
        );

        when(taskRepository.findByUser_Username(
                eq(username),
                any(Pageable.class)
        )).thenReturn(taskPage);

        // Act
        taskService.getMyTasks(page, size);

        // Assert
        verify(taskRepository).findByUser_Username(
                eq(username),
                argThat(pageable -> {
                    Sort.Order order = pageable.getSort().getOrderFor("createdAt");
                    return order != null
                            && order.getDirection() == Sort.Direction.DESC;
                })
        );
    }

    @Test
    void getMyTasks_shouldUseCustomSorting_whenSortParametersAreProvided() {
        // Arrange
        String username = "testuser";

        authenticateAs(username);

        Page<Task> taskPage = new PageImpl<>(List.of());

        when(taskRepository.findByUser_Username(
                eq(username),
                any(Pageable.class)
        )).thenReturn(taskPage);

        // Act
        taskService.getMyTasks(
                0,
                10,
                null,
                "title",
                "asc"
        );

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(taskRepository).findByUser_Username(
                eq(username),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("title").getDirection()
        );
    }

    @Test
    void getMyTasks_shouldThrowBadRequest_whenSortFieldIsInvalid() {
        // Arrange
        authenticateAs("testuser");

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getMyTasks(
                        0,
                        10,
                        null,
                        "invalidField",
                        "asc"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid sort field", exception.getReason());

        verifyNoInteractions(taskRepository);
    }

    @Test
    void getMyTasks_shouldThrowBadRequest_whenSortDirectionIsInvalid() {
        authenticateAs("testuser");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getMyTasks(
                        0, 10, null, "title", "sideways"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid sort direction", exception.getReason());

        verifyNoInteractions(taskRepository);
    }

    @Test
    void getMyTasks_shouldFilterAndSort_whenCompletedAndSortByAreProvided() {
        authenticateAs("testuser");

        Page<Task> taskPage = new PageImpl<>(List.of());

        when(taskRepository.findByUser_UsernameAndCompleted(
                eq("testuser"),
                eq(true),
                any(Pageable.class)
        )).thenReturn(taskPage);

        taskService.getMyTasks(0, 10, true, "title", "asc");

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(taskRepository).findByUser_UsernameAndCompleted(
                eq("testuser"),
                eq(true),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals("title", pageable.getSort().getOrderFor("title").getProperty());
        assertEquals(Sort.Direction.ASC,
                pageable.getSort().getOrderFor("title").getDirection());
    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
