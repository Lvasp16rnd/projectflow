package com.projectflow.request.presentation.exception;

import java.util.List;

public record ValidationErrorResponse(String message, List<FieldErrorResponse> errors) {
}
