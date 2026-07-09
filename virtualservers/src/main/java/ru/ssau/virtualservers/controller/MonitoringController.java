package ru.ssau.virtualservers.controller;

import ru.ssau.virtualservers.dto.LogResponseDTO;
import ru.ssau.virtualservers.dto.MetricResponseDTO;
import ru.ssau.virtualservers.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/servers/{serverId}/metrics")
    public ResponseEntity<List<MetricResponseDTO>> getMetrics(@PathVariable Long serverId) {
        return ResponseEntity.ok(monitoringService.getLastMetrics(serverId));
    }

    @GetMapping("/servers/{serverId}/logs")
    public ResponseEntity<List<LogResponseDTO>> getLogs(@PathVariable Long serverId) {
        return ResponseEntity.ok(monitoringService.getServerLogs(serverId));
    }
}