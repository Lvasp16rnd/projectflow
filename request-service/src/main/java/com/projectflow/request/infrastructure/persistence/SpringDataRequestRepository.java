package com.projectflow.request.infrastructure.persistence;

import com.projectflow.request.infrastructure.persistence.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataRequestRepository extends JpaRepository<RequestEntity, UUID> {
}
