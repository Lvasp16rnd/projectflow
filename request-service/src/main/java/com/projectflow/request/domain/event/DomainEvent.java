package com.projectflow.request.domain.event;

import java.util.UUID;

public interface DomainEvent {

    String eventType();

    UUID aggregateId();
}
