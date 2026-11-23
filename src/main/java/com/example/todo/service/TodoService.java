package com.example.todo.service;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {

    private static final Sort DEFAULT_SORT =
            Sort.by(Sort.Order.asc("completed"), Sort.Order.desc("createdAt"));

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> findAll(Boolean completed, String query) {
        boolean hasQuery = query != null && !query.isBlank();
        List<Todo> todos;
        if (completed != null && hasQuery) {
            todos = repository.findByCompletedAndTitleContainingIgnoreCase(completed, query.trim(), DEFAULT_SORT);
        } else if (completed != null) {
            todos = repository.findByCompleted(completed, DEFAULT_SORT);
        } else if (hasQuery) {
            todos = repository.findByTitleContainingIgnoreCase(query.trim(), DEFAULT_SORT);
        } else {
            todos = repository.findAll(DEFAULT_SORT);
        }
        return todos.stream().map(TodoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TodoResponse findById(Long id) {
        return TodoResponse.from(get(id));
    }

    public TodoResponse create(TodoRequest req) {
        Todo todo = new Todo();
        apply(todo, req);
        return TodoResponse.from(repository.save(todo));
    }

    public TodoResponse update(Long id, TodoRequest req) {
        Todo todo = get(id);
        apply(todo, req);
        return TodoResponse.from(repository.saveAndFlush(todo));
    }

    public TodoResponse toggle(Long id) {
        Todo todo = get(id);
        todo.setCompleted(!todo.isCompleted());
        return TodoResponse.from(repository.saveAndFlush(todo));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public int deleteCompleted() {
        return repository.deleteAllCompleted();
    }

    private Todo get(Long id) {
        return repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    private static void apply(Todo todo, TodoRequest req) {
        todo.setTitle(req.title().trim());
        todo.setDescription(req.description());
        todo.setCompleted(req.completed() != null && req.completed());
        todo.setPriority(req.priority() != null ? req.priority() : Priority.MEDIUM);
        todo.setDueDate(req.dueDate());
    }
}
