
package com.queue.queuemanagement.repository;

import com.queue.queuemanagement.entity.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    QueueEntry findTopByOrderByTokenNumberDesc();

    QueueEntry findTopByJoinedAtGreaterThanEqualAndJoinedAtLessThanOrderByTokenNumberDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    List<QueueEntry> findByStatus(String status);

    QueueEntry findFirstByStatus(String status);

    List<QueueEntry> findByJoinedAtGreaterThanEqualAndJoinedAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );
}
