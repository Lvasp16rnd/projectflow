package com.projectflow.request.infrastructure.persistence.adapter;

import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.port.out.OutboxPort;
import com.projectflow.request.domain.port.out.RequestRepository;
import com.projectflow.request.infrastructure.persistence.SpringDataRequestRepository;
import com.projectflow.request.infrastructure.persistence.entity.RequestEntity;
import com.projectflow.request.infrastructure.persistence.mapper.RequestMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RequestPersistenceAdapter implements RequestRepository {

    private final SpringDataRequestRepository repository;
    private final RequestMapper mapper;
    private final OutboxPort outboxPort;

    public RequestPersistenceAdapter(SpringDataRequestRepository repository, RequestMapper mapper, OutboxPort outboxPort) {
        this.repository = repository;
        this.mapper = mapper;
        this.outboxPort = outboxPort;
    }

    @Override
    public Request save(Request request) {
        RequestEntity entity = mapper.toEntity(request);
        RequestEntity saved = repository.save(entity);

        request.domainEvents().forEach(outboxPort::save);
        request.clearEvents();

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Request> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
