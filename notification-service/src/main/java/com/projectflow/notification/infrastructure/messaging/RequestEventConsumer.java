package com.projectflow.notification.infrastructure.messaging;

import com.projectflow.notification.infrastructure.persistence.AuditEventEntity;
import com.projectflow.notification.infrastructure.persistence.AuditRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

@Slf4j
@Component
public class RequestEventConsumer {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private final AuditRepository auditRepository;

    public RequestEventConsumer(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @SqsListener(value = "request-events", acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    public void onMessage(String message) {
        try {
            JsonNode node = JSON_MAPPER.readTree(message);
            String eventId = node.get("eventId").asText();
            String eventType = node.get("eventType").asText();
            String aggregateId = node.get("aggregateId").asText();

            if (auditRepository.findById(eventId).isPresent()) {
                log.warn("Duplicate event ignored: {}", eventId);
                return;
            }

            log.info("Sending notification for event {} (type {}, aggregate {})", eventId, eventType, aggregateId);

            auditRepository.save(new AuditEventEntity(eventId, aggregateId, eventType, message, LocalDateTime.now()));
        } catch (JacksonException e) {
            log.error("Failed to parse SQS message: {}", message, e);
        }
    }
}
