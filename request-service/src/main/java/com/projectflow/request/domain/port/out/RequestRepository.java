package com.projectflow.request.domain.port.out;

import com.projectflow.request.domain.model.Request;

import java.util.Optional;
import java.util.UUID;

public interface RequestRepository {

    Request save(Request request);

    Optional<Request> findById(UUID id);
}
