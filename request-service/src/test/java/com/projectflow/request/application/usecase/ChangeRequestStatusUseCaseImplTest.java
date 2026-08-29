package com.projectflow.request.application.usecase;

import com.projectflow.request.application.dto.ChangeStatusCommand;
import com.projectflow.request.application.exception.RequestNotFoundException;
import com.projectflow.request.domain.exception.DomainRuleException;
import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;
import com.projectflow.request.domain.model.Request;
import com.projectflow.request.domain.model.RequestStatus;
import com.projectflow.request.domain.port.out.RequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeRequestStatusUseCaseImplTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private ChangeRequestStatusUseCaseImpl useCase;

    @Test
    void shouldTransitionAndSaveRequest() {
        UUID id = UUID.randomUUID();
        Request request = Request.create("Setup laptop", "user-1", Category.TI, Priority.LOW);
        ChangeStatusCommand command = new ChangeStatusCommand(id, RequestStatus.ANALYSIS);

        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = useCase.execute(command);

        assertThat(result.getStatus()).isEqualTo(RequestStatus.ANALYSIS);
        verify(requestRepository).findById(id);
        verify(requestRepository).save(request);
    }

    @Test
    void shouldThrowWhenRequestNotFound() {
        UUID id = UUID.randomUUID();
        ChangeStatusCommand command = new ChangeStatusCommand(id, RequestStatus.ANALYSIS);

        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void shouldNotSaveWhenTransitionViolatesDomainRule() {
        UUID id = UUID.randomUUID();
        Request critical = Request.create("Critical incident", "user-1", Category.TI, Priority.CRITICAL);
        critical.transitionTo(RequestStatus.ANALYSIS);
        ChangeStatusCommand command = new ChangeStatusCommand(id, RequestStatus.PROCESSING);

        when(requestRepository.findById(id)).thenReturn(Optional.of(critical));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainRuleException.class);

        verify(requestRepository, never()).save(any(Request.class));
    }
}
