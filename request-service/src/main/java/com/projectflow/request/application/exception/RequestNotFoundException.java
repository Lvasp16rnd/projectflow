package com.projectflow.request.application.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RequestNotFoundException extends RuntimeException {

    private final UUID requestId;

    public RequestNotFoundException(UUID requestId) {
        super("Request not found: " + requestId);
        this.requestId = requestId;
    }
}
