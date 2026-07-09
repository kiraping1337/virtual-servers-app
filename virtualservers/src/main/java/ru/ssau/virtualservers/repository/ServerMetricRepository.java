package ru.ssau.virtualservers.repository;

import ru.ssau.virtualservers.entity.ServerMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetric, Long> {
    List<ServerMetric> findTop30ByServerIdOrderByMetricRecordedAtDesc(Long serverId);

    @Modifying
    @Transactional
    void deleteByMetricRecordedAtBefore(LocalDateTime cutoffTime);
}