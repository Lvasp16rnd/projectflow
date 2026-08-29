package com.projectflow.request.domain.model;

import com.projectflow.request.domain.event.DomainEvent;
import com.projectflow.request.domain.event.RequestSavedEvent;
import com.projectflow.request.domain.exception.DomainRuleException;
import com.projectflow.request.domain.exception.InvalidTransitionException;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.projectflow.request.domain.model.Priority.CRITICAL;
import static com.projectflow.request.domain.model.RequestStatus.ANALYSIS;
import static com.projectflow.request.domain.model.RequestStatus.APPROVAL;
import static com.projectflow.request.domain.model.RequestStatus.CANCELLED;
import static com.projectflow.request.domain.model.RequestStatus.COMPLETED;
import static com.projectflow.request.domain.model.RequestStatus.CREATED;
import static com.projectflow.request.domain.model.RequestStatus.PROCESSING;
import static com.projectflow.request.domain.model.RequestStatus.REJECTED;

@Getter
public class Request {

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_TRANSITIONS = Map.of(
            CREATED, Set.of(ANALYSIS, CANCELLED),
            ANALYSIS, Set.of(APPROVAL, PROCESSING, CANCELLED),
            APPROVAL, Set.of(PROCESSING, REJECTED, CANCELLED),
            PROCESSING, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            REJECTED, Set.of(),
            CANCELLED, Set.of()
    );

    private final UUID id;
    private final String title;
    private final String requesterId;
    private final Category category;
    private final Priority priority;
    private final LocalDateTime createdAt;
    private final Long version;

    private RequestStatus status;
    private LocalDateTime updatedAt;

    @Getter(AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    private Request(UUID id, String title, String requesterId, Category category, Priority priority,
                    RequestStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = requireNotBlank(title, "title");
        this.requesterId = requireNotBlank(requesterId, "requesterId");
        this.category = Objects.requireNonNull(category, "category");
        this.priority = Objects.requireNonNull(priority, "priority");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.version = version;
    }

    public static Request create(String title, String requesterId, Category category, Priority priority) {
        LocalDateTime now = LocalDateTime.now();
        Request request = new Request(UUID.randomUUID(), title, requesterId, category, priority, CREATED, now, now, null);
        request.registerEvent(new RequestSavedEvent(request.getId(), CREATED));
        return request;
    }

    public static Request reconstitute(UUID id, String title, String requesterId, Category category, Priority priority,
                                       RequestStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        return new Request(id, title, requesterId, category, priority, status, createdAt, updatedAt, version);
    }

    public void transitionTo(RequestStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "targetStatus");

        if (this.status == targetStatus) {
            return;
        }

        Set<RequestStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new InvalidTransitionException(this.status, targetStatus);
        }

        validateBusinessRules(targetStatus);

        this.status = targetStatus;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new RequestSavedEvent(this.id, targetStatus));
    }

    public List<DomainEvent> domainEvents() {
        return List.copyOf(events);
    }

    public void clearEvents() {
        events.clear();
    }

    private void validateBusinessRules(RequestStatus targetStatus) {
        if (this.status == ANALYSIS && targetStatus == PROCESSING && this.priority == CRITICAL) {
            throw new DomainRuleException(DomainRuleException.CRITICAL_REQUIRES_APPROVAL);
        }
    }

    private void registerEvent(DomainEvent event) {
        events.add(event);
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
