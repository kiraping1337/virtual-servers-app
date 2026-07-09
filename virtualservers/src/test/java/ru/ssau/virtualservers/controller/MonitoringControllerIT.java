package ru.ssau.virtualservers.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.virtualservers.dto.LogResponseDTO;
import ru.ssau.virtualservers.dto.MetricResponseDTO;
import ru.ssau.virtualservers.entity.*;
import ru.ssau.virtualservers.entity.enums.LogLevel;
import ru.ssau.virtualservers.entity.enums.ServerStatus;
import ru.ssau.virtualservers.entity.enums.UserRole;
import ru.ssau.virtualservers.repository.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//интеграционные тесты для MonitoringController
@SpringBootTest
class MonitoringControllerIT {

    @Autowired
    private MonitoringController monitoringController;

    @Autowired
    private ServerMetricRepository metricRepository;

    @Autowired
    private ServerLogRepository logRepository;

    @Autowired
    private VirtualServerRepository serverRepository;

    @Autowired
    private ServerConfigurationRepository configRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private VirtualServer testServer;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setUsername("monitoring_test_user");
        user.setPasswordHash(passwordEncoder.encode("pass"));
        user.setUserRole(UserRole.USER);
        user = userRepository.save(user);

        testServer = new VirtualServer();
        testServer.setUser(user);
        testServer.setServerName("monitored-server");
        testServer.setServerIpAddress("192.168.10.1");
        testServer.setServerStatus(ServerStatus.RUNNING);
        testServer = serverRepository.save(testServer);

        ServerConfiguration config = new ServerConfiguration();
        config.setServer(testServer);
        config.setCpu(4);
        config.setRam(2048);
        configRepository.save(config);
    }

    @AfterEach
    void tearDown() {
        metricRepository.deleteAllInBatch();
        logRepository.deleteAllInBatch();
        configRepository.deleteAllInBatch();
        serverRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void getMetrics_returnsServerMetrics() {
        saveMetric(testServer, 60, 1024, 20);
        saveMetric(testServer, 80, 1500, 45);

        ResponseEntity<List<MetricResponseDTO>> response =
            monitoringController.getMetrics(testServer.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MetricResponseDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getMetrics_noMetrics_returnsEmptyList() {
        ResponseEntity<List<MetricResponseDTO>> response =
            monitoringController.getMetrics(testServer.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getMetrics_returnsOnlyMetricsOfRequestedServer() {
        AppUser user = userRepository.findByUsername("monitoring_test_user").orElseThrow();

        VirtualServer otherServer = new VirtualServer();
        otherServer.setUser(user);
        otherServer.setServerName("other-server");
        otherServer.setServerIpAddress("192.168.10.2");
        otherServer.setServerStatus(ServerStatus.RUNNING);
        otherServer = serverRepository.save(otherServer);

        ServerConfiguration otherConfig = new ServerConfiguration();
        otherConfig.setServer(otherServer);
        otherConfig.setCpu(2);
        otherConfig.setRam(1024);
        configRepository.save(otherConfig);

        saveMetric(testServer, 50, 800, 10);
        saveMetric(otherServer, 90, 900, 70);

        ResponseEntity<List<MetricResponseDTO>> response =
            monitoringController.getMetrics(testServer.getId());

        List<MetricResponseDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).cpuUsage());
    }

    @Test
    void getMetrics_returnsNoMoreThan30Records() {
        for (int i = 0; i < 35; i++) {
            saveMetric(testServer, i % 100, 512 + i, i % 100);
        }

        ResponseEntity<List<MetricResponseDTO>> response =
            monitoringController.getMetrics(testServer.getId());

        assertTrue(response.getBody().size() <= 30);
    }

    @Test
    void getMetrics_returnsCorrectValues() {
        saveMetric(testServer, 75, 2000, 33);

        ResponseEntity<List<MetricResponseDTO>> response =
            monitoringController.getMetrics(testServer.getId());

        List<MetricResponseDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(75, result.get(0).cpuUsage());
        assertEquals(2000, result.get(0).ramUsage());
        assertEquals(33, result.get(0).networkLoad());
        assertNotNull(result.get(0).timestamp());
    }

    @Test
    void getLogs_returnsServerLogs() {
        saveLog(testServer, LogLevel.INFO, "Сервер запущен");
        saveLog(testServer, LogLevel.WARN, "Высокая нагрузка на CPU");

        ResponseEntity<List<LogResponseDTO>> response =
            monitoringController.getLogs(testServer.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<LogResponseDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getLogs_noLogs_returnsEmptyList() {
        ResponseEntity<List<LogResponseDTO>> response =
            monitoringController.getLogs(testServer.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getLogs_returnsCorrectFields() {
        saveLog(testServer, LogLevel.ERROR, "Критическая ошибка диска");

        ResponseEntity<List<LogResponseDTO>> response =
            monitoringController.getLogs(testServer.getId());

        List<LogResponseDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).id());
        assertEquals(LogLevel.ERROR, result.get(0).level());
        assertEquals("Критическая ошибка диска", result.get(0).message());
        assertNotNull(result.get(0).createdAt());
    }

    @Test
    void getLogs_sortedByDateDescending() {
        saveLog(testServer, LogLevel.INFO, "первый лог");
        saveLog(testServer, LogLevel.WARN, "второй лог");
        saveLog(testServer, LogLevel.ERROR, "третий лог");

        ResponseEntity<List<LogResponseDTO>> response =
            monitoringController.getLogs(testServer.getId());

        List<LogResponseDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(3, result.size());
        for (int i = 0; i < result.size() - 1; i++) {
            assertFalse(result.get(i).createdAt().isBefore(result.get(i + 1).createdAt()));
        }
    }


    private void saveMetric(VirtualServer server, int cpu, int ram, int network) {
        ServerMetric metric = new ServerMetric();
        metric.setServer(server);
        metric.setCpuUsagePercent(cpu);
        metric.setRamUsageMb(ram);
        metric.setNetworkLoadPercent(network);
        metricRepository.save(metric);
    }

    private void saveLog(VirtualServer server, LogLevel level, String message) {
        ServerLog log = new ServerLog();
        log.setServer(server);
        log.setLogLevel(level);
        log.setLogMessage(message);
        logRepository.save(log);
    }
}

