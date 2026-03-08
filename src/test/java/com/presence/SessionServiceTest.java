package com.presence;

import com.presence.dto.SaveSessionRequest;
import com.presence.model.Session;
import com.presence.repository.SessionRepository;
import com.presence.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock  SessionRepository repository;
    @InjectMocks SessionService service;

    private static final String USER_ID = "user_abc123";

    private SaveSessionRequest sampleRequest() {
        SaveSessionRequest req = new SaveSessionRequest();
        req.setDate(LocalDate.now());
        req.setPartial(false);
        req.setTotalQuestions(7);
        req.setMoodTag("🌱 Grounded");

        SaveSessionRequest.SenseAnswerDto ans = new SaveSessionRequest.SenseAnswerDto();
        ans.setSense("Sound");
        ans.setIcon("👂");
        ans.setQuestion("What do you hear?");
        ans.setAnswer("Birds outside");
        req.setAnswers(List.of(ans));

        return req;
    }

    @Test
    void saveSession_shouldPersistAndReturn() {
        Session fakeStored = Session.builder().id("abc123").date(LocalDate.now()).build();
        when(repository.save(any(Session.class))).thenReturn(fakeStored);

        Session result = service.saveSession(USER_ID, sampleRequest());

        assertThat(result.getId()).isEqualTo("abc123");
        verify(repository, times(1)).save(any(Session.class));
    }

    @Test
    void updateMood_shouldSetMoodTagAndSave() {
        Session existing = Session.builder().id("xyz").userId(USER_ID).moodTag(null).build();
        when(repository.findById("xyz")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Session> result = service.updateMood(USER_ID, "xyz", "🌊 Calm");

        assertThat(result).isPresent();
        assertThat(result.get().getMoodTag()).isEqualTo("🌊 Calm");
    }

    @Test
    void getCurrentStreak_shouldReturnZeroWhenEmpty() {
        when(repository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of());
        assertThat(service.getCurrentStreak(USER_ID)).isZero();
    }

    @Test
    void deleteSession_shouldReturnFalseWhenNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThat(service.deleteSession(USER_ID, "missing")).isFalse();
    }
}
