package com.example.todo.repository;

import com.example.todo.model.Todo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByCompleted(boolean completed, Sort sort);

    List<Todo> findByTitleContainingIgnoreCase(String query, Sort sort);

    List<Todo> findByCompletedAndTitleContainingIgnoreCase(boolean completed, String query, Sort sort);

    @Modifying
    @Query("delete from Todo t where t.completed = true")
    int deleteAllCompleted();
}
