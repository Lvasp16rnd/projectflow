package com.projectflow.request.infrastructure.persistence.adapter;

import com.projectflow.request.domain.event.DomainEvent;
import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.model.RequestStatus;
import com.projectflow.request.domain.port.out.OutboxPort;
import com.projectflow.request.infrastructure.persistence.SpringDataRequestRepository;
import com.projectflow.request.infrastructure.persistence.entity.RequestEntity;
import com.projectflow.request.infrastructure.persistence.mapper.RequestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPersistenceAdapterTest {

    @Mock
    private SpringDataRequestRepository repository;

    @Mock
    private RequestMapper mapper;

    @Mock
    private OutboxPort outboxPort;

    @InjectMocks
    private RequestPersistenceAdapter adapter;

    @Test
    void shouldSaveRequestThenPublishOutboxEvent() {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);
        RequestEntity entity = mock(RequestEntity.class);
        RequestEntity savedEntity = mock(RequestEntity.class);
        Request savedRequest = Request.reconstitute(
                request.getId(), "Setup laptop", "user-1", Category.TI, Priority.LOW,
                RequestStatus.CREATED, LocalDateTime.now(), LocalDateTime.now(), 0L);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedRequest);

        Request result = adapter.save(request);

        assertThat(result).isSameAs(savedRequest);

        InOrder inOrder = inOrder(repository, outboxPort);
        inOrder.verify(repository).save(entity);
        inOrder.verify(outboxPort).save(any(DomainEvent.class));

        assertThat(request.domainEvents()).isEmpty();
    }

    @Test
    void shouldNotPublishOutboxWhenRequestSaveFails() {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);
        RequestEntity entity = mock(RequestEntity.class);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("DB is down"));

        assertThatThrownBy(() -> adapter.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(outboxPort, never()).save(any(DomainEvent.class));
    }

    @Test
    void shouldPropagateExceptionWhenOutboxSaveFails() {
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);
        RequestEntity entity = mock(RequestEntity.class);
        RequestEntity savedEntity = mock(RequestEntity.class);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        doThrow(new RuntimeException("Outbox write failed")).when(outboxPort).save(any(DomainEvent.class));

        assertThatThrownBy(() -> adapter.save(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Outbox write failed");
    }
}
