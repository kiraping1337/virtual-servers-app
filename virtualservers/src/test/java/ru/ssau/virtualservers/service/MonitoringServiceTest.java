package ru.ssau.virtualservers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.ssau.virtualservers.dto.LogResponseDTO;
import ru.ssau.virtualservers.dto.MetricResponseDTO;
import ru.ssau.virtualservers.entity.ServerLog;
import ru.ssau.virtualservers.entity.ServerMetric;
import ru.ssau.virtualservers.entity.VirtualServer;
import ru.ssau.virtualservers.entity.enums.LogLevel;
import ru.ssau.virtualservers.repository.ServerLogRepository;
import ru.ssau.virtualservers.repository.ServerMetricRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//модульные тесты для MonitoringService.

class MonitoringServiceTest {

    private ServerMetricRepository metricRepository;
    private ServerLogRepository logRepository;
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        metricRepository = mock(ServerMetricRepository.class);
        logRepository = mock(ServerLogRepository.class);
        monitoringService = new MonitoringService(metricRepository, logRepository);
    }


    @Test
    void getLastMetrics_returnsMappedDTO() {
        VirtualServer server = new VirtualServer();
        server.setId(1L);

        ServerMetric metric = new ServerMetric();
        metric.setServer(server);
        metric.setCpuUsagePercent(75);
        metric.setRamUsageMb(2048);
        metric.setNetworkLoadPercent(30);
        metric.setMetricRecordedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        when(metricRepository.findTop30ByServerIdOrderByMetricRecordedAtDesc(1L))
            .thenReturn(List.of(metric));

        List<MetricResponseDTO> result = monitoringService.getLastMetrics(1L);

        assertEquals(1, result.size());
        assertEquals(75, result.get(0).cpuUsage());
        assertEquals(2048, result.get(0).ramUsage());
        assertEquals(30, result.get(0).networkLoad());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), result.get(0).timestamp());
    }

    @Test
    void getLastMetrics_noMetrics_returnsEmptyList() {
        when(metricRepository.findTop30ByServerIdOrderByMetricRecordedAtDesc(99L))
            .thenReturn(List.of());

        List<MetricResponseDTO> result = monitoringService.getLastMetrics(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLastMetrics_returnsNoMoreThan30Records() {
        List<ServerMetric> thirtyMetrics = buildMetrics(30, 1L);
        when(metricRepository.findTop30ByServerIdOrderByMetricRecordedAtDesc(1L))
            .thenReturn(thirtyMetrics);

        List<MetricResponseDTO> result = monitoringService.getLastMetrics(1L);

        assertEquals(30, result.size());
        verify(metricRepository, times(1))
            .findTop30ByServerIdOrderByMetricRecordedAtDesc(1L);
    }

    @Test
    void getServerLogs_returnsMappedDTO() {
        VirtualServer server = new VirtualServer();
        server.setId(1L);

        ServerLog log = new ServerLog();
        log.setId(10L);
        log.setServer(server);
        log.setLogLevel(LogLevel.INFO);
        log.setLogMessage("Сервер запущен");
        log.setLogCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        when(logRepository.findAllByServerIdOrderByLogCreatedAtDesc(1L))
            .thenReturn(List.of(log));

        List<LogResponseDTO> result = monitoringService.getServerLogs(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals(LogLevel.INFO, result.get(0).level());
        assertEquals("Сервер запущен", result.get(0).message());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), result.get(0).createdAt());
    }

    @Test
    void getServerLogs_noLogs_returnsEmptyList() {
        when(logRepository.findAllByServerIdOrderByLogCreatedAtDesc(99L))
            .thenReturn(List.of());

        List<LogResponseDTO> result = monitoringService.getServerLogs(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getServerLogs_differentLogLevels_mappingPreservesLevel() {
        VirtualServer server = new VirtualServer();
        server.setId(1L);

        ServerLog errorLog = new ServerLog();
        errorLog.setId(1L);
        errorLog.setServer(server);
        errorLog.setLogLevel(LogLevel.ERROR);
        errorLog.setLogMessage("Критическая ошибка");
        errorLog.setLogCreatedAt(LocalDateTime.now());

        ServerLog warnLog = new ServerLog();
        warnLog.setId(2L);
        warnLog.setServer(server);
        warnLog.setLogLevel(LogLevel.WARN);
        warnLog.setLogMessage("Предупреждение");
        warnLog.setLogCreatedAt(LocalDateTime.now());

        when(logRepository.findAllByServerIdOrderByLogCreatedAtDesc(1L))
            .thenReturn(List.of(errorLog, warnLog));

        List<LogResponseDTO> result = monitoringService.getServerLogs(1L);

        assertEquals(2, result.size());
        assertEquals(LogLevel.ERROR, result.get(0).level());
        assertEquals(LogLevel.WARN, result.get(1).level());
    }

    //вспомогательные методы

    private List<ServerMetric> buildMetrics(int count, Long serverId) {
        VirtualServer server = new VirtualServer();
        server.setId(serverId);

        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                ServerMetric m = new ServerMetric();
                m.setServer(server);
                m.setCpuUsagePercent(i % 100);
                m.setRamUsageMb(512 + i * 10);
                m.setNetworkLoadPercent(i % 100);
                m.setMetricRecordedAt(LocalDateTime.now().minusSeconds(i * 15L));
                return m;
            })
            .toList();
    }
}