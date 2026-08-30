package com.projectflow.notification.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditRepository extends MongoRepository<AuditEventEntity, String> {
}
