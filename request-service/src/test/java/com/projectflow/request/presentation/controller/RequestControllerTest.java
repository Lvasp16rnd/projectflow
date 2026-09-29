package com.projectflow.request.presentation.controller;

import com.projectflow.request.application.dto.ChangeStatusCommand;
import com.projectflow.request.application.dto.CreateRequestCommand;
import com.projectflow.request.application.exception.RequestNotFoundException;
import com.projectflow.request.application.usecase.ChangeRequestStatusUseCase;
import com.projectflow.request.application.usecase.CreateRequestUseCase;
import com.projectflow.request.application.usecase.ListRequestsUseCase;
import com.projectflow.request.domain.exception.DomainRuleException;
import com.projectflow.request.domain.exception.InvalidTransitionException;
import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.model.RequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    private static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRequestUseCase createRequestUseCase;

    @MockitoBean
    private ChangeRequestStatusUseCase changeRequestStatusUseCase;

    @MockitoBean
    private ListRequestsUseCase listRequestsUseCase;

    @Test
    void shouldCreateRequestAndReturn201WithLocation() throws Exception {
        Request request = Request.reconstitute(
                REQUEST_ID, "Setup laptop", "user-1", Category.TI, Priority.HIGH,
                RequestStatus.CREATED, LocalDateTime.now(), LocalDateTime.now(), 0L);

        when(createRequestUseCase.execute(any(CreateRequestCommand.class))).thenReturn(request);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Setup laptop",
                                  "requesterId": "user-1",
                                  "category": "TI",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/requests/" + REQUEST_ID))
                .andExpect(jsonPath("$.id").value(REQUEST_ID.toString()))
                .andExpect(jsonPath("$.title").value("Setup laptop"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(createRequestUseCase).execute(any(CreateRequestCommand.class));
    }

    @Test
    void shouldChangeStatusAndReturn200() throws Exception {
        Request request = Request.reconstitute(
                REQUEST_ID, "Setup laptop", "user-1", Category.TI, Priority.LOW,
                RequestStatus.APPROVAL, LocalDateTime.now(), LocalDateTime.now(), 1L);

        when(changeRequestStatusUseCase.execute(any(ChangeStatusCommand.class))).thenReturn(request);

        mockMvc.perform(patch("/requests/{id}/status", REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"APPROVAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID.toString()))
                .andExpect(jsonPath("$.status").value("APPROVAL"));

        verify(changeRequestStatusUseCase).execute(any(ChangeStatusCommand.class));
    }

    @Test
    void shouldReturn404WhenRequestNotFound() throws Exception {
        when(changeRequestStatusUseCase.execute(any(ChangeStatusCommand.class)))
                .thenThrow(new RequestNotFoundException(REQUEST_ID));

        mockMvc.perform(patch("/requests/{id}/status", REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"ANALYSIS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Request not found: " + REQUEST_ID));
    }

    @Test
    void shouldReturn422WhenTransitionIsInvalid() throws Exception {
        when(changeRequestStatusUseCase.execute(any(ChangeStatusCommand.class)))
                .thenThrow(new InvalidTransitionException(RequestStatus.CREATED, RequestStatus.PROCESSING));

        mockMvc.perform(patch("/requests/{id}/status", REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"PROCESSING\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn422WhenDomainRuleViolated() throws Exception {
        when(changeRequestStatusUseCase.execute(any(ChangeStatusCommand.class)))
                .thenThrow(new DomainRuleException(DomainRuleException.CRITICAL_REQUIRES_APPROVAL));

        mockMvc.perform(patch("/requests/{id}/status", REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"PROCESSING\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(DomainRuleException.CRITICAL_REQUIRES_APPROVAL));
    }

    @Test
    void shouldReturn409OnOptimisticLockingFailure() throws Exception {
        when(changeRequestStatusUseCase.execute(any(ChangeStatusCommand.class)))
                .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        mockMvc.perform(patch("/requests/{id}/status", REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"PROCESSING\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "requesterId": "user-1",
                                  "category": "TI",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));

        verify(createRequestUseCase, never()).execute(any(CreateRequestCommand.class));
    }

    @Test
    void shouldReturn400WhenCategoryIsNull() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Setup laptop",
                                  "requesterId": "user-1",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("category"));
    }

    @Test
    void shouldReturn400WhenTargetStatusIsNull() throws Exception {
        mockMvc.perform(patch("/requests/{id}/status", REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("targetStatus"));
    }

    @Test
    void shouldListRequestsAndReturn200() throws Exception {
        when(listRequestsUseCase.execute()).thenReturn(java.util.List.of());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(listRequestsUseCase).execute();
    }
}
