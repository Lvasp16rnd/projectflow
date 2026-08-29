package com.projectflow.request.presentation.dto;

import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRequestRequest(
        @NotBlank String title,
        @NotBlank String requesterId,
        @NotNull Category category,
        @NotNull Priority priority
) {
}
