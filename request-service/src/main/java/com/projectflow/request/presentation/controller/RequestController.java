package com.projectflow.request.presentation.controller;

import com.projectflow.request.application.dto.ChangeStatusCommand;
import com.projectflow.request.application.dto.CreateRequestCommand;
import com.projectflow.request.application.usecase.ChangeRequestStatusUseCase;
import com.projectflow.request.application.usecase.CreateRequestUseCase;
import com.projectflow.request.application.usecase.ListRequestsUseCase;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.presentation.dto.ChangeStatusRequest;
import com.projectflow.request.presentation.dto.CreateRequestRequest;
import com.projectflow.request.presentation.dto.RequestResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final CreateRequestUseCase createRequestUseCase;
    private final ChangeRequestStatusUseCase changeRequestStatusUseCase;
    private final ListRequestsUseCase listRequestsUseCase;

    public RequestController(CreateRequestUseCase createRequestUseCase,
                             ChangeRequestStatusUseCase changeRequestStatusUseCase,
                             ListRequestsUseCase listRequestsUseCase) {
        this.createRequestUseCase = createRequestUseCase;
        this.changeRequestStatusUseCase = changeRequestStatusUseCase;
        this.listRequestsUseCase = listRequestsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RequestResponse>> list() {
        List<RequestResponse> responses = listRequestsUseCase.execute().stream()
                .map(RequestResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
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
