package ru.ssau.virtualservers.entity;

import jakarta.persistence.*;
import ru.ssau.virtualservers.entity.enums.ServerStatus;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "virtual_server")
public class VirtualServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "server_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "server_name", nullable = false, length = 150)
    private String serverName;

    @Column(name = "server_ip_address", nullable = false, length = 50)
    private String serverIpAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "server_status", nullable = false, length = 30)
    private ServerStatus serverStatus;

    @CreationTimestamp
    @Column(name = "server_created_at", nullable = false, updatable = false)
    private LocalDateTime serverCreatedAt;

    @OneToOne(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private ServerConfiguration configuration;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServerMetric> metrics = new ArrayList<>();

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServerLog> logs = new ArrayList<>();

    public VirtualServer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getServerIpAddress() { return serverIpAddress; }
    public void setServerIpAddress(String serverIpAddress) { this.serverIpAddress = serverIpAddress; }

    public ServerStatus getServerStatus() { return serverStatus; }
    public void setServerStatus(ServerStatus serverStatus) { this.serverStatus = serverStatus; }

    public LocalDateTime getServerCreatedAt() { return serverCreatedAt; }
    public void getServerCreatedAt(LocalDateTime serverCreatedAt) { this.serverCreatedAt = serverCreatedAt; }

    public ServerConfiguration getConfiguration() { return configuration; }
    public void setConfiguration(ServerConfiguration configuration) { this.configuration = configuration; }

    public List<ServerMetric> getMetrics() { return metrics; }
    public void setMetrics(List<ServerMetric> metrics) { this.metrics = metrics; }

    public List<ServerLog> getLogs() { return logs; }
    public void setLogs(List<ServerLog> logs) { this.logs = logs; }
}