package com.projectflow.request.infrastructure.persistence.mapper;

import com.projectflow.request.domain.model.Request;
import com.projectflow.request.infrastructure.persistence.entity.RequestEntity;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestEntity toEntity(Request request) {
        return new RequestEntity(
                request.getId(),
                request.getTitle(),
                request.getRequesterId(),
                request.getCategory(),
                request.getPriority(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getVersion()
        );
    }

    public Request toDomain(RequestEntity entity) {
        return Request.reconstitute(
                entity.getId(),
                entity.getTitle(),
                entity.getRequesterId(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
