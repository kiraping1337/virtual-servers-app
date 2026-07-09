package ru.ssau.virtualservers.service;

import ru.ssau.virtualservers.entity.ServerMetric;
import ru.ssau.virtualservers.entity.VirtualServer;
import ru.ssau.virtualservers.entity.enums.ServerStatus;
import ru.ssau.virtualservers.repository.ServerMetricRepository;
import ru.ssau.virtualservers.repository.VirtualServerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class ServerSimulationService {

    private final VirtualServerRepository serverRepository;
    private final ServerMetricRepository metricRepository;
    private final Random random = new Random();

    public ServerSimulationService(VirtualServerRepository serverRepository, 
                                   ServerMetricRepository metricRepository) {
        this.serverRepository = serverRepository;
        this.metricRepository = metricRepository;
    }

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void generateMetricsForRunningServers() {
        List<VirtualServer> runningServers = serverRepository.findAllByServerStatus(ServerStatus.RUNNING);

        for (VirtualServer server : runningServers) {
            ServerMetric metric = new ServerMetric();
            metric.setServer(server);
            
            metric.setCpuUsagePercent(random.nextInt(100));
            metric.setNetworkLoadPercent(random.nextInt(100));
            
            int maxRam = server.getConfiguration().getRam();
            metric.setRamUsageMb(500 + random.nextInt(maxRam > 500 ? maxRam - 500 : 100));

            metricRepository.save(metric);
        }
    }
    @Scheduled(fixedRate = 600000)
    public void cleanupOldMetrics() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);
        
        System.out.println("Запуск очистки старых метрик. Удаляем записи до: " + cutoffTime);
        
        metricRepository.deleteByMetricRecordedAtBefore(cutoffTime);
    }
    
}