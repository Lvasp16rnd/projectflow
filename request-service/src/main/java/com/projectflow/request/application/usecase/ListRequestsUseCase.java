package com.projectflow.request.application.usecase;

import com.projectflow.request.domain.model.Request;

import java.util.List;

public interface ListRequestsUseCase {

    List<Request> execute();
}
