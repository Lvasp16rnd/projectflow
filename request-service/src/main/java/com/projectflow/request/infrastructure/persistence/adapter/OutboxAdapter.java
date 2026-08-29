package com.projectflow.request.infrastructure.persistence.adapter;

import com.projectflow.request.domain.event.DomainEvent;
import com.projectflow.request.domain.port.out.OutboxPort;
import com.projectflow.request.infrastructure.persistence.SpringDataOutboxRepository;
import com.projectflow.request.infrastructure.persistence.entity.OutboxEntity;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OutboxAdapter implements OutboxPort {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private final SpringDataOutboxRepository repository;

    public OutboxAdapter(SpringDataOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(DomainEvent event) {
        OutboxEntity entity = new OutboxEntity(
                UUID.randomUUID(),
                event.aggregateId().toString(),
                event.eventType(),
                serialize(event),
                LocalDateTime.now(),
                null
        );
        repository.save(entity);
    }

    private String serialize(DomainEvent event) {
        try {
            ObjectNode node = (ObjectNode) JSON_MAPPER.valueToTree(event);
            node.put("eventType", event.eventType());
            node.put("aggregateId", event.aggregateId().toString());
            return JSON_MAPPER.writeValueAsString(node);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize domain event " + event.eventType(), e);
        }
    }
}
