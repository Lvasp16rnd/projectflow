package com.projectflow.request.application.usecase;

import com.projectflow.request.application.dto.ChangeStatusCommand;
import com.projectflow.request.domain.model.Request;

public interface ChangeRequestStatusUseCase {

    Request execute(ChangeStatusCommand command);
}
