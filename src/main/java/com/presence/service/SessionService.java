package com.presence.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.presence.dto.SaveSessionRequest;
import com.presence.model.SenseAnswer;
import com.presence.model.Session;
import com.presence.repository.SessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository repository;

    // ── Save ─────────────────────────────────────────────────

    public Session saveSession(String userId, SaveSessionRequest req) {
        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        List<SenseAnswer> answers = req.getAnswers().stream()
            .map(dto -> SenseAnswer.builder()
                .sense(dto.getSense())
                .icon(dto.getIcon())
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .build())
            .collect(Collectors.toList());

        Session session = Session.builder()
            .userId(userId)
            .date(date)
            .answers(answers)
            .moodTag(req.getMoodTag())
            .isPartial(req.isPartial())
            .totalQuestions(req.getTotalQuestions() > 0 ? req.getTotalQuestions() : 7)
            .aiReflection(req.getAiReflection())
            .build();

        Session saved = repository.save(session);
        log.info("Session saved id={} userId={} date={} partial={}", saved.getId(), userId, date, saved.isPartial());
        return saved;
    }

    // ── Draft ────────────────────────────────────────────────

    public Optional<Session> getLatestUnfinishedSession(String userId) {
        return repository.findFirstByUserIdAndIsPartialTrueAndAiReflectionIsNullOrderByUpdatedAtDesc(userId);
    }

    // ── Update draft answers ─────────────────────────────────

    /**
     * Replaces the answers of an existing partial session (draft).
     * Only updates if the session belongs to the requesting user.
     */
    public Optional<Session> update(String userId, String sessionId, SaveSessionRequest req) {
        return repository.findById(sessionId)
            .filter(s -> userId.equals(s.getUserId()))
            .map(s -> {
                List<SenseAnswer> answers = req.getAnswers().stream()
                    .map(dto -> SenseAnswer.builder()
                        .sense(dto.getSense())
                        .icon(dto.getIcon())
                        .question(dto.getQuestion())
                        .answer(dto.getAnswer())
                        .build())
                    .collect(Collectors.toList());
                s.setAnswers(answers);
                s.setPartial(req.isPartial());
                s.setAiReflection(req.getAiReflection());
                Session saved = repository.save(s);
                log.info("Draft updated id={} userId={} answers={}", saved.getId(), userId, answers.size());
                return saved;
            });
    }

    // ── Update mood ──────────────────────────────────────────

    /**
     * Only updates the session if it belongs to the requesting user.
     */
    public Optional<Session> updateMood(String userId, String sessionId, String moodTag) {
        return repository.findById(sessionId)
            .filter(s -> userId.equals(s.getUserId()))
            .map(s -> {
                s.setMoodTag(moodTag);
                return repository.save(s);
            });
    }

    // ── Queries (all scoped to userId) ───────────────────────

    public List<Session> getAll(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Session> getById(String userId, String sessionId) {
        return repository.findById(sessionId)
            .filter(s -> userId.equals(s.getUserId())); // prevent accessing other users' data
    }

    public List<Session> getByDate(String userId, LocalDate date) {
        return repository.findByUserIdAndDateOrderByCreatedAtDesc(userId, date);
    }

    public List<Session> getToday(String userId) {
        return repository.findByUserIdAndDateOrderByCreatedAtDesc(userId, LocalDate.now());
    }

    public List<Session> getRange(String userId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end   = to   != null ? to   : LocalDate.now();
        return repository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
    }

    // ── Stats ────────────────────────────────────────────────

    public int getCurrentStreak(String userId) {
        List<Session> all = repository.findByUserIdOrderByCreatedAtDesc(userId);
        if (all.isEmpty()) return 0;

        List<LocalDate> days = all.stream()
            .map(Session::getDate)
            .distinct()
            .sorted((a, b) -> b.compareTo(a))
            .collect(Collectors.toList());

        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (!days.get(0).isEqual(today) && !days.get(0).isEqual(yesterday)) return 0;

        int streak   = 0;
        LocalDate expected = days.get(0);
        for (LocalDate day : days) {
            if (day.isEqual(expected)) { streak++; expected = expected.minusDays(1); }
            else break;
        }
        return streak;
    }

    public StatsResponse getStats(String userId) {
        List<Session> all  = repository.findByUserIdOrderByCreatedAtDesc(userId);
        long total         = all.size();
        long completed     = all.stream().filter(s -> !s.isPartial()).count();
        int  streak        = getCurrentStreak(userId);

        String topMood = all.stream()
            .map(Session::getMoodTag)
            .filter(m -> m != null && !m.isBlank())
            .collect(Collectors.groupingBy(m -> m, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        return new StatsResponse(total, completed, streak, topMood);
    }

    public record StatsResponse(
        long   totalSessions,
        long   completedSessions,
        int    currentStreak,
        String mostFrequentMood
    ) {}

    // ── Delete ───────────────────────────────────────────────

    public boolean deleteSession(String userId, String sessionId) {
        return repository.findById(sessionId)
            .filter(s -> userId.equals(s.getUserId()))
            .map(s -> { repository.delete(s); return true; })
            .orElse(false);
    }
}
