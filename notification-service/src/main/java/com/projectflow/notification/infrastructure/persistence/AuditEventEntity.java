package com.projectflow.notification.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "audit_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventEntity {

    @Id
    private String eventId;

    private String aggregateId;
    private String eventType;
    private String payload;
    private LocalDateTime processedAt;
}
