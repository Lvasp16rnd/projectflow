package com.projectflow.request.infrastructure.messaging;

import com.projectflow.request.infrastructure.persistence.SpringDataOutboxRepository;
import com.projectflow.request.infrastructure.persistence.entity.OutboxEntity;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock
    private SpringDataOutboxRepository outboxRepository;

    @Mock
    private SqsTemplate sqsTemplate;

    @InjectMocks
    private OutboxPoller poller;

    @Test
    void shouldPublishUnpublishedEventsAndMarkPublished() {
        OutboxEntity event = new OutboxEntity(
                UUID.randomUUID(), "agg-1", "REQUEST_SAVED", "{\"requestId\":\"agg-1\"}",
                LocalDateTime.now(), null);

        when(outboxRepository.findByPublishedAtIsNull()).thenReturn(List.of(event));

        poller.publishPendingEvents();

        verify(sqsTemplate).send(any());
        verify(outboxRepository).save(event);
        assertThat(event.getPublishedAt()).isNotNull();
    }
}
