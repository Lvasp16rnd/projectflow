package com.projectflow.notification;

import com.projectflow.notification.infrastructure.persistence.AuditRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(TestcontainersConfiguration.class)
@Testcontainers
@SpringBootTest
class RequestEventConsumerIntegrationTest {

    @Container
    static GenericContainer<?> localstack = new GenericContainer<>(DockerImageName.parse("localstack/localstack:3.7.2"))
            .withExposedPorts(4566)
            .withEnv("SERVICES", "sqs")
            .waitingFor(Wait.forHttp("/_localstack/health").forPort(4566));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint",
                () -> "http://" + localstack.getHost() + ":" + localstack.getMappedPort(4566));
        registry.add("spring.cloud.aws.region.static", () -> "us-east-1");
        registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");
        registry.add("spring.cloud.aws.sqs.listener.max-concurrent-messages", () -> "1");
        registry.add("spring.cloud.aws.sqs.listener.max-messages-per-poll", () -> "1");
        registry.add("spring.cloud.aws.sqs.queue-not-found-strategy", () -> "CREATE");
    }

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Test
    void shouldProcessOnlyFirstOfTwoIdenticalMessages() {
        String message = """
                {"eventId":"evt-001","eventType":"REQUEST_SAVED","aggregateId":"req-001","requestId":"req-001","status":"CREATED"}
                """;

        sqsTemplate.send(to -> to.queue("request-events").payload(message));
        sqsTemplate.send(to -> to.queue("request-events").payload(message));

        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(auditRepository.count()).isEqualTo(1));

        assertThat(auditRepository.findById("evt-001")).isPresent();
    }
}
