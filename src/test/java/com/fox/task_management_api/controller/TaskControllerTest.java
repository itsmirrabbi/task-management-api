package com.fox.task_management_api.controller;

import com.fox.task_management_api.dto.CreateTaskRequest;
import com.fox.task_management_api.dto.TaskResponse;
import com.fox.task_management_api.dto.UpdateTaskRequest;
import com.fox.task_management_api.security.CustomUserDetailsService;
import com.fox.task_management_api.security.JwtService;
import com.fox.task_management_api.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
@Import(TaskControllerTestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void createTask_shouldReturnCreated_whenRequestIsValid() throws Exception {

        LocalDateTime now = LocalDateTime.of(2026, 11, 3, 12, 0);

        TaskResponse taskResponse = new TaskResponse(
                1L,
                "Learn Spring Boot",
                "Practice controller testing",
                false,
                now,
                now
        );

        when(taskService.createTask(any(CreateTaskRequest.class)))
                .thenReturn(taskResponse);

        mockMvc.perform(post("/tasks")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Learn Spring Boot",
                                    "description": "Practice controller testing"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.description")
                        .value("Practice controller testing"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-11-03T12:00:00"))
                .andExpect(jsonPath("$.updatedAt")
                        .value("2026-11-03T12:00:00"));

        verify(taskService, times(1))
                .createTask(any(CreateTaskRequest.class));
    }

    @Test
    void createTask_shouldReturnBadRequest_whenTitleIsBlank()
            throws Exception {

        mockMvc.perform(post("/tasks")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "description": "Practice controller testing"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "Title must not be blank"
                        )));
    }

    @Test
    void getTaskById_shouldReturnTask_whenTaskExists()
            throws Exception {

        LocalDateTime now = LocalDateTime.of(2026, 11, 3, 12, 0);

        TaskResponse taskResponse = new TaskResponse(
                1L,
                "Learn Spring Boot",
                "Practice API testing",
                false,
                now,
                now
        );

        when(taskService.getTaskById(1L))
                .thenReturn(taskResponse);

        mockMvc.perform(get("/tasks/1")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.description")
                        .value("Practice API testing"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-11-03T12:00:00"))
                .andExpect(jsonPath("$.updatedAt")
                        .value("2026-11-03T12:00:00"));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void getTaskById_shouldReturnNotFound_whenTaskDoesNotExist()
            throws Exception {

        when(taskService.getTaskById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        mockMvc.perform(get("/tasks/999")
                        .with(user("testuser")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/999"));

        verify(taskService, times(1)).getTaskById(999L);
    }

    @Test
    void updateTask_shouldReturnUpdatedTask_whenTaskExists()
            throws Exception {

        LocalDateTime now = LocalDateTime.of(2026, 11, 3, 12, 0);

        TaskResponse taskResponse = new TaskResponse(
                1L,
                "Master Spring Boot",
                "Build REST APIs",
                true,
                now,
                now
        );

        when(taskService.updateTask(
                eq(1L),
                any(UpdateTaskRequest.class)
        )).thenReturn(taskResponse);

        mockMvc.perform(put("/tasks/1")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Master Spring Boot",
                                    "description": "Build REST APIs",
                                    "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Master Spring Boot"))
                .andExpect(jsonPath("$.description")
                        .value("Build REST APIs"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-11-03T12:00:00"))
                .andExpect(jsonPath("$.updatedAt")
                        .value("2026-11-03T12:00:00"));

        verify(taskService).updateTask(
                eq(1L),
                any(UpdateTaskRequest.class)
        );
    }

    @Test
    void updateTask_shouldReturnBadRequest_whenTitleIsBlank()
            throws Exception {

        mockMvc.perform(put("/tasks/1")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "description": "Build REST APIs",
                                    "completed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "Title must not be blank"
                        )));

        verifyNoInteractions(taskService);
    }

    @Test
    void deleteTask_shouldReturnSuccess_whenTaskExists()
            throws Exception {

        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/1")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Task deleted successfully"
                ));

        verify(taskService).deleteTask(1L);
    }

    @Test
    void getMyTasks_shouldReturnPaginatedTasks()
            throws Exception {

        LocalDateTime now = LocalDateTime.of(2026, 11, 3, 12, 0);

        TaskResponse task = new TaskResponse(
                1L,
                "Learn Spring Boot",
                "Practice API testing",
                false,
                now,
                now
        );

        Page<TaskResponse> taskPage = new PageImpl<>(
                List.of(task),
                PageRequest.of(1, 5),
                11
        );

        when(taskService.getMyTasks(1, 5))
                .thenReturn(taskPage);

        mockMvc.perform(get("/tasks")
                        .param("page", "1")
                        .param("size", "5")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Learn Spring Boot"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(taskService).getMyTasks(1, 5);
    }

    @Test
    void getMyTasks_shouldUseDefaultPagination_whenParametersAreNotProvided()
            throws Exception {

        Page<TaskResponse> taskPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(taskService.getMyTasks(0, 10))
                .thenReturn(taskPage);

        mockMvc.perform(get("/tasks")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(taskService).getMyTasks(0, 10);
    }

    @Test
    void deleteTask_shouldReturnNotFound_whenTaskDoesNotExist()
            throws Exception {

        doThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found"
        )).when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/tasks/999")
                        .with(user("testuser")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/999"));

        verify(taskService).deleteTask(999L);
    }

    @Test
    void updateTask_shouldReturnNotFound_whenTaskDoesNotExist()
            throws Exception {

        when(taskService.updateTask(
                eq(999L),
                any(UpdateTaskRequest.class)
        )).thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found"
        ));

        mockMvc.perform(put("/tasks/999")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated task",
                                    "description": "Updated description",
                                    "completed": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/999"));

        verify(taskService).updateTask(
                eq(999L),
                any(UpdateTaskRequest.class)
        );
    }

    @Test
    void getTaskById_shouldReturnUnauthorized_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }
}
