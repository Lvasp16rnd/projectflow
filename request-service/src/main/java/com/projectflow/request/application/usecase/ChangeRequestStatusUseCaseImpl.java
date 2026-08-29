package com.projectflow.request.application.usecase;

import com.projectflow.request.application.dto.ChangeStatusCommand;
import com.projectflow.request.application.exception.RequestNotFoundException;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.port.out.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeRequestStatusUseCaseImpl implements ChangeRequestStatusUseCase {

    private final RequestRepository requestRepository;

    public ChangeRequestStatusUseCaseImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public Request execute(ChangeStatusCommand command) {
        Request request = requestRepository.findById(command.requestId())
                .orElseThrow(() -> new RequestNotFoundException(command.requestId()));

        request.transitionTo(command.targetStatus());

        return requestRepository.save(request);
    }
}
