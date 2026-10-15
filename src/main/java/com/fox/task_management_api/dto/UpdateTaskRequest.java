package com.fox.task_management_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateTaskRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(min = 3, max = 100,
            message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000,
            message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Completed status must be provided")
    private Boolean completed;

    public UpdateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}