package com.projectflow.request.presentation.controller;

import com.projectflow.request.application.dto.ChangeStatusCommand;
import com.projectflow.request.application.dto.CreateRequestCommand;
import com.projectflow.request.application.usecase.ChangeRequestStatusUseCase;
import com.projectflow.request.application.usecase.CreateRequestUseCase;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.presentation.dto.ChangeStatusRequest;
import com.projectflow.request.presentation.dto.CreateRequestRequest;
import com.projectflow.request.presentation.dto.RequestResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final CreateRequestUseCase createRequestUseCase;
    private final ChangeRequestStatusUseCase changeRequestStatusUseCase;

    public RequestController(CreateRequestUseCase createRequestUseCase, ChangeRequestStatusUseCase changeRequestStatusUseCase) {
        this.createRequestUseCase = createRequestUseCase;
        this.changeRequestStatusUseCase = changeRequestStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<RequestResponse> create(@Valid @RequestBody CreateRequestRequest request) {
        CreateRequestCommand command = new CreateRequestCommand(
                request.title(),
                request.requesterId(),
                request.category(),
                request.priority()
        );

        Request created = createRequestUseCase.execute(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(RequestResponse.from(created));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RequestResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        ChangeStatusCommand command = new ChangeStatusCommand(id, request.targetStatus());

        Request updated = changeRequestStatusUseCase.execute(command);

        return ResponseEntity.ok(RequestResponse.from(updated));
    }
}
