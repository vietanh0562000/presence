package com.presence.repository;

import com.presence.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {

    // ── Scoped to a single user ──────────────────────────────

    List<Session> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Session> findByUserIdAndDateOrderByCreatedAtDesc(String userId, LocalDate date);

    Optional<Session> findFirstByUserIdAndDateOrderByCreatedAtDesc(String userId, LocalDate date);

    List<Session> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate from, LocalDate to);

    List<Session> findByUserIdAndIsPartialFalseOrderByCreatedAtDesc(String userId);

    long countByUserIdAndDateBetween(String userId, LocalDate from, LocalDate to);

    @Query("{ 'userId': ?0, 'moodTag': { $regex: ?1, $options: 'i' } }")
    List<Session> findByUserIdAndMoodTagContaining(String userId, String mood);

    Optional<Session> findFirstByUserIdAndIsPartialTrueAndAiReflectionIsNullOrderByUpdatedAtDesc(String userId);
}

