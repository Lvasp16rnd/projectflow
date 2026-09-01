package com.projectflow.request.application.usecase;

import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.port.out.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListRequestsUseCaseImpl implements ListRequestsUseCase {

    private final RequestRepository requestRepository;

    public ListRequestsUseCaseImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Request> execute() {
        return requestRepository.findAll();
    }
}
