package com.fox.task_management_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(min = 3, max = 100,
            message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000,
            message = "Description must not exceed 1000 characters")
    private String description;

    public CreateTaskRequest() {
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
}