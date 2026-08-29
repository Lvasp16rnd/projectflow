package com.projectflow.request.application.usecase;

import com.projectflow.request.application.dto.CreateRequestCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRequestUseCaseImplTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private CreateRequestUseCaseImpl useCase;

    @Test
    void shouldCreateAndSaveRequest() {
        CreateRequestCommand command = new CreateRequestCommand("Setup laptop", "user-1", Category.TI, Priority.HIGH);

        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Setup laptop");
        assertThat(result.getRequesterId()).isEqualTo("user-1");
        assertThat(result.getCategory()).isEqualTo(Category.TI);
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getStatus()).isEqualTo(RequestStatus.CREATED);

        verify(requestRepository).save(any(Request.class));
    }
}
