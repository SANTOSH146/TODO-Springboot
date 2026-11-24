package com.example.todo;

import com.example.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
class TodoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired TodoRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private long create(String json) throws Exception {
        String body = mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void createsTodoWithDefaults() throws Exception {
        mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  Buy milk  \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/todos/")))
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title is required"));
    }

    @Test
    void rejectsInvalidPriority() throws Exception {
        mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getsUpdatesTogglesAndDeletes() throws Exception {
        long id = create("{\"title\":\"Write report\",\"priority\":\"HIGH\",\"dueDate\":\"2026-12-01\"}");

        mvc.perform(get("/api/todos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-01"));

        mvc.perform(put("/api/todos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Write final report\",\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write final report"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.dueDate").value(nullValue()));

        mvc.perform(patch("/api/todos/" + id + "/toggle"))
                .andExpect(jsonPath("$.completed").value(true));
        mvc.perform(patch("/api/todos/" + id + "/toggle"))
                .andExpect(jsonPath("$.completed").value(false));

        mvc.perform(delete("/api/todos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/todos/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Todo not found with id " + id));
    }

    @Test
    void filtersAndSearches() throws Exception {
        long a = create("{\"title\":\"Buy groceries\"}");
        create("{\"title\":\"Call mom\"}");
        create("{\"title\":\"Buy gift\",\"completed\":true}");

        mvc.perform(get("/api/todos")).andExpect(jsonPath("$", hasSize(3)))
                // incomplete items sort first
                .andExpect(jsonPath("$[2].title").value("Buy gift"));
        mvc.perform(get("/api/todos?completed=true")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/todos?q=buy")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/todos?q=buy&completed=false"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value((int) a));
    }

    @Test
    void clearsCompleted() throws Exception {
        create("{\"title\":\"a\",\"completed\":true}");
        create("{\"title\":\"b\",\"completed\":true}");
        create("{\"title\":\"c\"}");

        mvc.perform(delete("/api/todos/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(2));
        mvc.perform(get("/api/todos")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void missingTodoReturns404() throws Exception {
        mvc.perform(put("/api/todos/9999").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/todos/9999")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/todos/9999/toggle")).andExpect(status().isNotFound());
    }
}
