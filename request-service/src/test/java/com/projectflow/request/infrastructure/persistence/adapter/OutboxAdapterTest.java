package com.projectflow.request.infrastructure.persistence.adapter;

import com.projectflow.request.domain.event.RequestSavedEvent;
import com.projectflow.request.domain.model.RequestStatus;
import com.projectflow.request.infrastructure.persistence.SpringDataOutboxRepository;
import com.projectflow.request.infrastructure.persistence.entity.OutboxEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxAdapterTest {

    @Mock
    private SpringDataOutboxRepository repository;

    @InjectMocks
    private OutboxAdapter adapter;

    @Test
    void shouldSaveOutboxEntityWithSerializedPayload() {
        RequestSavedEvent event = new RequestSavedEvent(UUID.randomUUID(), RequestStatus.CREATED);

        adapter.save(event);

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(repository).save(captor.capture());

        OutboxEntity entity = captor.getValue();
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getAggregateId()).isEqualTo(event.aggregateId().toString());
        assertThat(entity.getEventType()).isEqualTo("REQUEST_SAVED");
        assertThat(entity.getPayload()).contains(event.aggregateId().toString());
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getPublishedAt()).isNull();
    }
}
