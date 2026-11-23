package com.example.todo.dto;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;

import java.time.Instant;
import java.time.LocalDate;

public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        Priority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static TodoResponse from(Todo t) {
        return new TodoResponse(t.getId(), t.getTitle(), t.getDescription(), t.isCompleted(),
                t.getPriority(), t.getDueDate(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
