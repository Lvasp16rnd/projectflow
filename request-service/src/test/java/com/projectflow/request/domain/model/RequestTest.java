package com.projectflow.request.domain.model;

import com.projectflow.request.domain.exception.DomainRuleException;
import com.projectflow.request.domain.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.projectflow.request.domain.model.RequestStatus.ANALYSIS;
import static com.projectflow.request.domain.model.RequestStatus.APPROVAL;
import static com.projectflow.request.domain.model.RequestStatus.CANCELLED;
import static com.projectflow.request.domain.model.RequestStatus.COMPLETED;
import static com.projectflow.request.domain.model.RequestStatus.CREATED;
import static com.projectflow.request.domain.model.RequestStatus.PROCESSING;
import static com.projectflow.request.domain.model.RequestStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestTest {

    @Test
    void shouldCreateRequestInCreatedStatus() {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);

        assertThat(request.getId()).isNotNull();
        assertThat(request.getTitle()).isEqualTo("Setup laptop");
        assertThat(request.getRequesterId()).isEqualTo("user-1");
        assertThat(request.getCategory()).isEqualTo(Category.TI);
        assertThat(request.getPriority()).isEqualTo(Priority.LOW);
        assertThat(request.getStatus()).isEqualTo(CREATED);
        assertThat(request.getCreatedAt()).isNotNull();
        assertThat(request.getUpdatedAt()).isEqualTo(request.getCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldAllowTransition(RequestStatus from, RequestStatus to) {
        Request request = requestIn(from);

        request.transitionTo(to);

        assertThat(request.getStatus()).isEqualTo(to);
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectIllegalTransition(RequestStatus from, RequestStatus to) {
        Request request = requestIn(from);

        assertThatThrownBy(() -> request.transitionTo(to))
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining(from.name())
                .hasMessageContaining(to.name());
    }

    @Test
    void shouldRejectAnalysisToProcessingWhenCritical() {
        Request request = Request.create("Critical incident", "user-1", Category.TI, Priority.CRITICAL);
        request.transitionTo(ANALYSIS);

        assertThatThrownBy(() -> request.transitionTo(PROCESSING))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage(DomainRuleException.CRITICAL_REQUIRES_APPROVAL);
    }

    @Test
    void shouldAllowAnalysisToApprovalWhenCritical() {
        Request request = Request.create("Critical incident", "user-1", Category.TI, Priority.CRITICAL);
        request.transitionTo(ANALYSIS);

        request.transitionTo(APPROVAL);

        assertThat(request.getStatus()).isEqualTo(APPROVAL);
    }

    @ParameterizedTest
    @EnumSource(value = Priority.class, names = {"LOW", "MEDIUM", "HIGH"})
    void shouldAllowAnalysisToProcessingForNonCriticalPriority(Priority priority) {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, priority);
        request.transitionTo(ANALYSIS);

        request.transitionTo(PROCESSING);

        assertThat(request.getStatus()).isEqualTo(PROCESSING);
    }

    @Test
    void shouldAllowCriticalRequestToCompleteViaApproval() {
        Request request = Request.create("Critical incident", "user-1", Category.TI, Priority.CRITICAL);

        request.transitionTo(ANALYSIS);
        request.transitionTo(APPROVAL);
        request.transitionTo(PROCESSING);
        request.transitionTo(COMPLETED);

        assertThat(request.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    void shouldIgnoreTransitionToSameStatus() {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);

        request.transitionTo(CREATED);

        assertThat(request.getStatus()).isEqualTo(CREATED);
    }

    @Test
    void shouldRejectNullTargetStatus() {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);

        assertThatThrownBy(() -> request.transitionTo(null))
                .isInstanceOf(NullPointerException.class);
    }

    private Request requestIn(RequestStatus status) {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);
        return switch (status) {
            case CREATED -> request;
            case ANALYSIS -> {
                request.transitionTo(ANALYSIS);
                yield request;
            }
            case APPROVAL -> {
                request.transitionTo(ANALYSIS);
                request.transitionTo(APPROVAL);
                yield request;
            }
            case PROCESSING -> {
                request.transitionTo(ANALYSIS);
                request.transitionTo(PROCESSING);
                yield request;
            }
            case COMPLETED -> {
                request.transitionTo(ANALYSIS);
                request.transitionTo(PROCESSING);
                request.transitionTo(COMPLETED);
                yield request;
            }
            case REJECTED -> {
                request.transitionTo(ANALYSIS);
                request.transitionTo(APPROVAL);
                request.transitionTo(REJECTED);
                yield request;
            }
            case CANCELLED -> {
                request.transitionTo(CANCELLED);
                yield request;
            }
        };
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(CREATED, ANALYSIS),
                Arguments.of(CREATED, CANCELLED),
                Arguments.of(ANALYSIS, APPROVAL),
                Arguments.of(ANALYSIS, PROCESSING),
                Arguments.of(ANALYSIS, CANCELLED),
                Arguments.of(APPROVAL, PROCESSING),
                Arguments.of(APPROVAL, REJECTED),
                Arguments.of(APPROVAL, CANCELLED),
                Arguments.of(PROCESSING, COMPLETED)
        );
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(CREATED, PROCESSING),
                Arguments.of(CREATED, APPROVAL),
                Arguments.of(CREATED, COMPLETED),
                Arguments.of(CREATED, REJECTED),
                Arguments.of(ANALYSIS, COMPLETED),
                Arguments.of(ANALYSIS, REJECTED),
                Arguments.of(APPROVAL, ANALYSIS),
                Arguments.of(APPROVAL, COMPLETED),
                Arguments.of(PROCESSING, CANCELLED),
                Arguments.of(PROCESSING, ANALYSIS),
                Arguments.of(PROCESSING, APPROVAL),
                Arguments.of(PROCESSING, REJECTED),
                Arguments.of(COMPLETED, ANALYSIS),
                Arguments.of(REJECTED, PROCESSING),
                Arguments.of(CANCELLED, ANALYSIS)
        );
    }
}
