package ru.ssau.virtualservers.service;

import ru.ssau.virtualservers.dto.LogResponseDTO;
import ru.ssau.virtualservers.dto.MetricResponseDTO;
import ru.ssau.virtualservers.repository.ServerLogRepository;
import ru.ssau.virtualservers.repository.ServerMetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    private final ServerMetricRepository metricRepository;
    private final ServerLogRepository logRepository;

    public MonitoringService(ServerMetricRepository metricRepository, ServerLogRepository logRepository) {
        this.metricRepository = metricRepository;
        this.logRepository = logRepository;
    }

    public List<MetricResponseDTO> getLastMetrics(Long serverId) {
        return metricRepository.findTop30ByServerIdOrderByMetricRecordedAtDesc(serverId).stream()
                .map(m -> new MetricResponseDTO(
                    m.getCpuUsagePercent(),
                    m.getRamUsageMb(),
                    m.getNetworkLoadPercent(),
                    m.getMetricRecordedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<LogResponseDTO> getServerLogs(Long serverId) {
        return logRepository.findAllByServerIdOrderByLogCreatedAtDesc(serverId).stream()
                .map(log -> new LogResponseDTO(
                    log.getId(),
                    log.getLogCreatedAt(),
                    log.getLogLevel(),
                    log.getLogMessage()
                ))
                .collect(Collectors.toList());
    }
}