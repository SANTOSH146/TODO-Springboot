# Todo App (Spring Boot)

A todo application with a REST API, an H2 database and a built-in web UI.

**Stack:** Java 21 · Spring Boot 3.5 · Spring Web · Spring Data JPA · Bean Validation · H2

## Run

```bash
mvn spring-boot:run
```

Open http://localhost:8080. The UI lets you add todos with a priority and due date, tick them off, search, filter and clear completed ones.

Run the tests with `mvn test`. To build a runnable jar, run `mvn package`, then `java -jar target/todo-app-1.0.0.jar`.

Data is stored in `./data/tododb.mv.db`, so it survives restarts. For an in-memory database, set
`spring.datasource.url=jdbc:h2:mem:tododb` in `application.properties`.
The H2 console is at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/tododb`, user `sa`, no password).

## Using MySQL or PostgreSQL

H2 is the default. Two extra profiles switch to a real database server. No code changes are needed, and the `todos` table is created automatically.

| Database | Run command (PowerShell) | Default user / password | Setup |
|---|---|---|---|
| MySQL | `mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"` | `root` / `root` | none, `tododb` is created automatically |
| PostgreSQL | `mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"` | `postgres` / `postgres` | run `CREATE DATABASE tododb;` once |

To use your own credentials, set environment variables before running:

```powershell
$env:DB_USERNAME="root"; $env:DB_PASSWORD="yourpassword"
```

Or edit `application-mysql.properties` / `application-postgres.properties`.
For a jar: `java -jar target/todo-app-1.0.0.jar --spring.profiles.active=mysql`.

Tests always use an in-memory H2 database, so `mvn test` needs no database server.

## REST API

| Method | Path | Description |
|---|---|---|
| GET | `/api/todos` | List todos. Optional `?completed=true\|false` and `?q=text` (title search) |
| GET | `/api/todos/{id}` | Get one todo |
| POST | `/api/todos` | Create (201 + `Location` header) |
| PUT | `/api/todos/{id}` | Replace a todo |
| PATCH | `/api/todos/{id}/toggle` | Flip completed |
| DELETE | `/api/todos/{id}` | Delete (204) |
| DELETE | `/api/todos/completed` | Delete all completed; returns `{"deleted": n}` |

Request body:

```json
{
  "title": "Write report",
  "description": "Q4 numbers",
  "completed": false,
  "priority": "HIGH",
  "dueDate": "2026-12-01"
}
```

Only `title` is required (max 200 characters). `priority` is `LOW`, `MEDIUM` (the default) or `HIGH`.

Errors are returned as RFC 7807 problem details, for example:

```json
{ "status": 400, "title": "Bad Request", "detail": "Validation failed",
  "errors": { "title": "Title is required" } }
```

### curl examples

```bash
curl -X POST localhost:8080/api/todos -H 'Content-Type: application/json' \
     -d '{"title":"Buy milk","priority":"HIGH"}'
curl localhost:8080/api/todos?completed=false
curl -X PATCH localhost:8080/api/todos/1/toggle
curl -X DELETE localhost:8080/api/todos/1
```

## Project layout

```
k8s/
src/main/java/com/example/todo/
├── TodoApplication.java
├── controller/TodoController.java      REST endpoints
├── service/TodoService.java            business logic, transactions
├── repository/TodoRepository.java      Spring Data JPA
├── model/Todo.java, Priority.java      JPA entity
├── dto/TodoRequest.java, TodoResponse.java
└── exception/                          404 + validation error handling
src/main/resources/
├── application.properties
└── static/index.html                   web UI (plain HTML/JS)
src/test/java/.../TodoControllerTest.java   integration tests (MockMvc)
```
