# SportRadar

A web platform for organising and joining amateur sports games and events. Built with Spring Boot and Thymeleaf.

---

## Running the app

```bash
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080). The H2 database is created automatically on first run.

To run tests:
```bash
./gradlew test
```

---

## Features

- **Games** – Browse upcoming games, filter by sport, date, price or title. Join a game or add yourself to the waitlist when it's full. When a player resigns, the first person on the waitlist is promoted automatically.
- **Events** – Multi-game events with goal tracking, per-game scoreboards, and a top-scorers table.
- **Locations** – Admin-managed venues displayed on an interactive Leaflet map. Players can leave star ratings and written reviews.
- **Profiles** – Public player profiles showing skill/behaviour ratings from other players, game history, and contact info. Users can edit their own profile.
- **Admin panel** – Admins can add locations and delete inappropriate reviews.

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3, Spring Data JPA, Hibernate 6 |
| Templates | Thymeleaf |
| Database | H2 (file-based, auto-migrated via `ddl-auto=update`) |
| Maps | Leaflet.js + OpenStreetMap / Nominatim geocoding |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| Build | Gradle |

---

## Architecture

The application follows a standard three-layer MVC structure:

```
Controller  →  Service  →  Repository  →  Database
     ↕
  Thymeleaf templates
```

- **Controllers** (`web/`) handle HTTP requests, validate input, and populate the model.
- **Services** (`service/`) contain business logic (join/resign game, promote from waitlist, etc.).
- **Repositories** (`repository/`) extend Spring Data's `CrudRepository` and declare JPQL queries.
- **`@ControllerAdvice`** (`CurrentUserAdvice`) injects the logged-in user into every model automatically.

---

## Key design decisions

### JPA JOINED inheritance
`User` is an abstract JPA entity using `InheritanceType.JOINED`. `Player` and `Admin` are concrete subclasses stored in separate tables joined on the primary key. This reflects the real-world distinction between regular players and administrators without duplicating shared fields (name, email, login, etc.).

### Waitlist as an ordered List
`Game.waitList` uses `@OrderColumn` so the list preserves insertion order. When a player resigns, `GameServiceImpl` removes the first entry and adds them to the game — a simple FIFO queue backed directly by JPA.

### Session-based authentication
User identity is stored in `HttpSession` as `currentUserId`. This is intentionally simple; a production version would use Spring Security with BCrypt password hashing and CSRF protection.

### Manual pagination
The games list paginates by fetching all matching games and slicing with `List.subList()`. For the current H2/development scale this is straightforward to understand and test; a production version would use Spring Data's `Pageable` to push the `LIMIT/OFFSET` to the database.

---

## Project structure

```
src/main/java/.../
├── model/          Entity classes (User, Player, Admin, Game, Event, Location, …)
├── repository/     Spring Data repositories with custom JPQL queries
├── service/        Business logic (GameService, GameServiceImpl)
└── web/            Controllers + form DTOs + CurrentUserAdvice

src/main/resources/
├── templates/      Thymeleaf HTML templates
└── static/css/     Single design-system stylesheet (Style.css)

src/test/java/.../
├── model/          Unit tests for domain logic (Player ratings, age)
├── service/        Unit tests for GameServiceImpl (join, resign, waitlist)
└── web/dto/        Bean Validation tests for RegisterForm
```
