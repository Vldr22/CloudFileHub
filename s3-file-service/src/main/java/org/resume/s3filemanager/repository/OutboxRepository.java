package org.resume.s3filemanager.repository;

import org.resume.s3filemanager.entity.OutboxEvent;
import org.resume.s3filemanager.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status AND e.nextRetryAt <= :now ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents(
            @Param("status") OutboxStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

}
