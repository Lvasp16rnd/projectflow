package com.projectflow.request.application.dto;

import com.projectflow.request.domain.model.RequestStatus;

import java.util.UUID;

public record ChangeStatusCommand(UUID requestId, RequestStatus targetStatus) {
}
