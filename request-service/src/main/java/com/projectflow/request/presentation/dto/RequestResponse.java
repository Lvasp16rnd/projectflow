package com.projectflow.request.presentation.dto;

import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestResponse(
        UUID id,
        String title,
        String requesterId,
        Category category,
        Priority priority,
        RequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version
) {

    public static RequestResponse from(Request request) {
        return new RequestResponse(
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
}
