package com.projectflow.request.infrastructure.messaging;

import com.projectflow.request.infrastructure.persistence.SpringDataOutboxRepository;
import com.projectflow.request.infrastructure.persistence.entity.OutboxEntity;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OutboxPoller {

    private final SpringDataOutboxRepository outboxRepository;
    private final SqsTemplate sqsTemplate;

    public OutboxPoller(SpringDataOutboxRepository outboxRepository, SqsTemplate sqsTemplate) {
        this.outboxRepository = outboxRepository;
        this.sqsTemplate = sqsTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        outboxRepository.findByPublishedAtIsNull().forEach(this::publish);
    }

    private void publish(OutboxEntity event) {
        try {
            sqsTemplate.send(to -> to.queue("request-events").payload(event.getPayload()));
            event.markPublished(LocalDateTime.now());
            outboxRepository.save(event);
        } catch (Exception e) {
            log.warn("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
        }
    }
}
