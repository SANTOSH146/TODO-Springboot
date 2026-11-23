package com.example.todo.dto;

import com.example.todo.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Payload for creating or fully replacing a todo. */
public record TodoRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        Boolean completed,

        Priority priority,

        LocalDate dueDate
) {}
