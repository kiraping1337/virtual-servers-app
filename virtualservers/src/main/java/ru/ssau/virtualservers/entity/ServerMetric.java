package ru.ssau.virtualservers.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_metric")
public class ServerMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private VirtualServer server;

    @Column(name = "cpu_usage_percent", nullable = false)
    private Integer cpuUsagePercent;

    @Column(name = "ram_usage_mb", nullable = false)
    private Integer ramUsageMb;

    @Column(name = "network_load_percent", nullable = false)
    private Integer networkLoadPercent;

    @CreationTimestamp
    @Column(name = "metric_recorded_at", nullable = false, updatable = false)
    private LocalDateTime metricRecordedAt;

    public ServerMetric() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VirtualServer getServer() { return server; }
    public void setServer(VirtualServer server) { this.server = server; }

    public Integer getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(Integer cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }

    public Integer getRamUsageMb() { return ramUsageMb; }
    public void setRamUsageMb(Integer ramUsageMb) { this.ramUsageMb = ramUsageMb; }

    public Integer getNetworkLoadPercent() { return networkLoadPercent; }
    public void setNetworkLoadPercent(Integer networkLoadPercent) { this.networkLoadPercent = networkLoadPercent; }

    public LocalDateTime getMetricRecordedAt() { return metricRecordedAt; }
    public void setMetricRecordedAt(LocalDateTime metricRecordedAt) { this.metricRecordedAt = metricRecordedAt; }
}
