package ru.ssau.virtualservers.repository;

import ru.ssau.virtualservers.entity.ServerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServerLogRepository extends JpaRepository<ServerLog, Long> {
    List<ServerLog> findAllByServerIdOrderByLogCreatedAtDesc(Long serverId);
}