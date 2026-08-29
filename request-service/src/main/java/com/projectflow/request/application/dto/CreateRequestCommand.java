package com.projectflow.request.application.dto;

import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;

public record CreateRequestCommand(String title, String requesterId, Category category, Priority priority) {
}
