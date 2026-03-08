# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Setup

Source `.env` before running any Maven command — it sets `JAVA_HOME` (Java 21) and the app secrets:

```bash
set -a && source .env && set +a
```

## Commands

```bash
# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=SessionServiceTest

# Build the JAR
./mvnw package -DskipTests

# Run the production JAR
java -jar target/presence-backend.jar --spring.profiles.active=prod
```

## Configuration

Config is in `src/main/resources/application.yml`. Local secrets come from `.env` (git-ignored):

```
JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.9/libexec/openjdk.jdk/Contents/Home
DB_URL=...
CLERK_SECRET_KEY=sk_test_...
```

The prod profile (`application-prod.yml`) reads `DB_URL`, `CLERK_SECRET_KEY`, and `ALLOWED_ORIGINS` from environment variables.

## Architecture

**Stack:** Spring Boot 3.2, MongoDB (Spring Data), Spring Security, Clerk Java SDK (v1.4.0), Lombok.

**Auth flow:** Every request goes through `ClerkAuthFilter` (a `OncePerRequestFilter`) which verifies the `Authorization: Bearer <token>` JWT using Clerk's JWKS endpoint via the Clerk SDK. On success, it populates the `SecurityContext` with a `ClerkPrincipal` (containing `userId` and `email`). Controllers extract the principal via `@AuthenticationPrincipal ClerkPrincipal`. All `/api/sessions/**` routes require authentication.

**Data isolation:** The `userId` (from the Clerk JWT subject claim) is stored on every `Session` document and used as a filter on all repository queries. Service methods accept `userId` as a parameter and scope every query and mutation to it — users can never access each other's data.

**Key classes:**
- `ClerkAuthFilter` — JWT verification, SecurityContext population
- `SecurityConfig` — filter chain, CORS (origins from `presence.cors.allowed-origins`)
- `SessionService` — all business logic; `StatsResponse` record (streak, totals, top mood) is defined as an inner record
- `SessionRepository` — Spring Data MongoDB repository; queries are named-method conventions
- `Session` model — uses `@CreatedDate`/`@LastModifiedDate` (requires `@EnableMongoAuditing` in `AppConfig`); `userId` and `date` fields are indexed
- `SessionDtos` — request/response DTOs including `SaveSessionRequest` with a nested `SenseAnswerDto`

**Domain concept:** A `Session` is a mindfulness grounding exercise with `SenseAnswer` entries (one per sense: sound, sight, etc.), an optional `moodTag`, and an optional `aiReflection` string. Sessions can be partial (`isPartial=true`) if the user didn't complete all questions.
