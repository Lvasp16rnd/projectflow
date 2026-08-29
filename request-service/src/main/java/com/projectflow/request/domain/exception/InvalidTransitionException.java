package com.projectflow.request.domain.exception;

import com.projectflow.request.domain.model.RequestStatus;
import lombok.Getter;

@Getter
public class InvalidTransitionException extends RuntimeException {

    private final RequestStatus from;
    private final RequestStatus to;

    public InvalidTransitionException(RequestStatus from, RequestStatus to) {
        super(String.format("Invalid status transition: %s -> %s", from, to));
        this.from = from;
        this.to = to;
    }
}
