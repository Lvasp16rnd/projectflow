package com.projectflow.request.domain.exception;

public class DomainRuleException extends RuntimeException {

    public static final String CRITICAL_REQUIRES_APPROVAL = "Critical requests require approval";

    public DomainRuleException(String message) {
        super(message);
    }
}
