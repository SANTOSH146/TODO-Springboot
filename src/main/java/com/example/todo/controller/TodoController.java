package com.example.todo.controller;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    /** GET /api/todos?completed=true|false&q=text */
    @GetMapping
    public List<TodoResponse> list(@RequestParam(required = false) Boolean completed,
                                   @RequestParam(name = "q", required = false) String query) {
        return service.findAll(completed, query);
    }

    @GetMapping("/{id}")
    public TodoResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoRequest request) {
        TodoResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/toggle")
    public TodoResponse toggle(@PathVariable Long id) {
        return service.toggle(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @DeleteMapping("/completed")
    public Map<String, Integer> deleteCompleted() {
        return Map.of("deleted", service.deleteCompleted());
    }
}
