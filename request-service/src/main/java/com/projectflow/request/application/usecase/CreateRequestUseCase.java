package com.projectflow.request.application.usecase;

import com.projectflow.request.application.dto.CreateRequestCommand;
import com.projectflow.request.domain.model.Request;

public interface CreateRequestUseCase {

    Request execute(CreateRequestCommand command);
}
