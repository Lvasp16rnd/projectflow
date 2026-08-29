package com.projectflow.request;

import com.projectflow.request.application.dto.CreateRequestCommand;
import com.projectflow.request.application.usecase.CreateRequestUseCase;
import com.projectflow.request.domain.event.DomainEvent;
import com.projectflow.request.domain.model.Category;
import com.projectflow.request.domain.model.Priority;
import com.projectflow.request.domain.port.out.OutboxPort;
import com.projectflow.request.infrastructure.persistence.SpringDataRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.aws.region.static=us-east-1",
        "spring.cloud.aws.credentials.access-key=test",
        "spring.cloud.aws.credentials.secret-key=test"
})
class RequestOutboxIntegrationTest {

    @Autowired
    private CreateRequestUseCase createRequestUseCase;

    @Autowired
    private SpringDataRequestRepository requestRepository;

    @MockitoBean
    private OutboxPort outboxPort;

    @Test
    void shouldRollbackRequestWhenOutboxSaveFails() {
        doThrow(new RuntimeException("Outbox unavailable"))
                .when(outboxPort).save(any(DomainEvent.class));

        assertThatThrownBy(() -> createRequestUseCase.execute(
                new CreateRequestCommand("Setup laptop", "user-1", Category.TI, Priority.LOW)))
                .isInstanceOf(RuntimeException.class);

        assertThat(requestRepository.count()).isZero();
    }
}
