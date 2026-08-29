package com.projectflow.request.presentation.dto;

import com.projectflow.request.domain.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull RequestStatus targetStatus) {
}
