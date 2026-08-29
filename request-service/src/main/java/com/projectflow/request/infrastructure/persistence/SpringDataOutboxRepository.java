package com.projectflow.request.infrastructure.persistence;

import com.projectflow.request.infrastructure.persistence.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    List<OutboxEntity> findByPublishedAtIsNull();
}
