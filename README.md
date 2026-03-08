# 🌿 Presence Backend

Spring Boot 3 + MongoDB. Auth is handled by Clerk — no passwords stored here.

## How auth works

```
React app (Clerk)
  └─ useAuth().getToken()  →  Clerk JWT
       └─ Authorization: Bearer <token>  →  Spring Boot
            └─ ClerkAuthFilter verifies token via Clerk SDK (JWKS)
                 └─ ClerkPrincipal { userId, email } → SecurityContext
                      └─ SessionController scopes all queries to userId
```

## Requirements

- Java 21+
- Maven 3.9+
- MongoDB running locally or MongoDB Atlas
- A Clerk account (free tier works): https://clerk.com

## Setup

**1. Get your Clerk Secret Key**
Dashboard → API Keys → copy `sk_test_...`

**2. Configure `application.properties`**
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/presence_db
clerk.secret-key=sk_test_YOUR_SECRET_KEY
presence.cors.allowed-origins=http://localhost:5173
```

**3. Run**
```bash
./mvnw spring-boot:run
# → http://localhost:8080
```

## API Reference

All endpoints require `Authorization: Bearer <clerk-jwt>`.
The `userId` is extracted from the token automatically — never passed in the body.

| Method | Endpoint                        | Description                     |
|--------|---------------------------------|---------------------------------|
| POST   | `/api/sessions`                 | Save a session                  |
| GET    | `/api/sessions`                 | All my sessions                 |
| GET    | `/api/sessions/today`           | Today's sessions                |
| GET    | `/api/sessions/stats`           | Streak, totals, top mood        |
| GET    | `/api/sessions/{id}`            | One session by ID               |
| GET    | `/api/sessions/date/2026-03-08` | Sessions on a specific date     |
| GET    | `/api/sessions/range?from=&to=` | Sessions in a date range        |
| PATCH  | `/api/sessions/{id}/mood`       | `{ moodTag }` — update mood     |
| DELETE | `/api/sessions/{id}`            | Delete a session                |

## Project structure

```
src/main/java/com/presence/
├── model/
│   ├── Session.java            # userId field ties sessions to Clerk users
│   └── SenseAnswer.java
├── repository/
│   └── SessionRepository.java  # All queries scoped by userId
├── service/
│   └── SessionService.java
├── controller/
│   └── SessionController.java  # @AuthenticationPrincipal ClerkPrincipal
├── security/
│   ├── ClerkAuthFilter.java    # Verifies Clerk JWT on every request
│   └── ClerkPrincipal.java     # userId + email in SecurityContext
├── dto/
│   └── SessionDtos.java
└── config/
    ├── SecurityConfig.java     # Clerk bean, filter chain, CORS
    ├── AppConfig.java          # MongoDB auditing
    └── GlobalExceptionHandler.java
```

## Production deploy

```bash
export MONGO_URI=mongodb+srv://...
export CLERK_SECRET_KEY=sk_live_...
export ALLOWED_ORIGINS=https://yourapp.com
java -jar target/presence-backend.jar --spring.profiles.active=prod
```
