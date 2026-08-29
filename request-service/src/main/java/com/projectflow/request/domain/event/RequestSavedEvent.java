package com.projectflow.request.domain.event;

import com.projectflow.request.domain.model.RequestStatus;

import java.util.UUID;

public record RequestSavedEvent(UUID requestId, RequestStatus status) implements DomainEvent {

    @Override
    public String eventType() {
        return "REQUEST_SAVED";
    }

    @Override
    public UUID aggregateId() {
        return requestId;
    }
}
