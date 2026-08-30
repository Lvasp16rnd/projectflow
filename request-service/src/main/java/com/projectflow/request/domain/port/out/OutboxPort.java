package com.projectflow.request.domain.port.out;

import com.projectflow.request.domain.event.DomainEvent;

public interface OutboxPort {

    void save(DomainEvent event);
}
