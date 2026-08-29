package com.projectflow.request.application.usecase;

import com.projectflow.request.application.dto.CreateRequestCommand;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.port.out.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRequestUseCaseImpl implements CreateRequestUseCase {

    private final RequestRepository requestRepository;

    public CreateRequestUseCaseImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public Request execute(CreateRequestCommand command) {
        Request request = Request.create(
                command.title(),
                command.requesterId(),
                command.category(),
                command.priority()
        );
        return requestRepository.save(request);
    }
}
