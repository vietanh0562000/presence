package com.presence.controller;

import com.presence.dto.SaveSessionRequest;
import com.presence.model.Session;
import com.presence.security.ClerkPrincipal;
import com.presence.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * All endpoints require a valid JWT (Authorization: Bearer <token>).
 * Every query is automatically scoped to the authenticated user.
 *
 *   POST   /api/sessions              — save a session
 *   GET    /api/sessions              — get all my sessions
 *   GET    /api/sessions/today        — get today's sessions
 *   GET    /api/sessions/stats        — streak, totals, top mood
 *   GET    /api/sessions/{id}         — get one session
 *   GET    /api/sessions/date/{date}  — sessions on a specific date
 *   GET    /api/sessions/range        — sessions in a range (?from=&to=)
 *   PATCH  /api/sessions/{id}/mood    — update mood tag
 *   DELETE /api/sessions/{id}         — delete a session
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;

    @PostMapping
    public ResponseEntity<Session> save(
        @AuthenticationPrincipal ClerkPrincipal principal,
        @Valid @RequestBody SaveSessionRequest req
    ) {
        Session saved = service.saveSession(principal.getUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Session>> getAll(
        @AuthenticationPrincipal ClerkPrincipal principal
    ) {
        return ResponseEntity.ok(service.getAll(principal.getUserId()));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Session>> getToday(
        @AuthenticationPrincipal ClerkPrincipal principal
    ) {
        return ResponseEntity.ok(service.getToday(principal.getUserId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<SessionService.StatsResponse> getStats(
        @AuthenticationPrincipal ClerkPrincipal principal
    ) {
        return ResponseEntity.ok(service.getStats(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getById(
        @AuthenticationPrincipal ClerkPrincipal principal,
        @PathVariable String id
    ) {
        return service.getById(principal.getUserId(), id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Session>> getByDate(
        @AuthenticationPrincipal ClerkPrincipal principal,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(service.getByDate(principal.getUserId(), date));
    }

    @GetMapping("/range")
    public ResponseEntity<List<Session>> getRange(
        @AuthenticationPrincipal ClerkPrincipal principal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(service.getRange(principal.getUserId(), from, to));
    }

    @PatchMapping("/{id}/mood")
    public ResponseEntity<Session> updateMood(
        @AuthenticationPrincipal ClerkPrincipal principal,
        @PathVariable String id,
        @RequestBody Map<String, String> body
    ) {
        String moodTag = body.get("moodTag");
        if (moodTag == null || moodTag.isBlank()) return ResponseEntity.badRequest().build();

        return service.updateMood(principal.getUserId(), id, moodTag)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal ClerkPrincipal principal,
        @PathVariable String id
    ) {
        return service.deleteSession(principal.getUserId(), id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
