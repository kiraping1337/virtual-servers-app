package ru.ssau.virtualservers.entity;

import jakarta.persistence.*;
import ru.ssau.virtualservers.entity.enums.LogLevel;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_log")
public class ServerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private VirtualServer server;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false, length = 20)
    private LogLevel logLevel;

    @Column(name = "log_message", nullable = false, columnDefinition = "TEXT")
    private String logMessage;

    @CreationTimestamp
    @Column(name = "log_created_at", nullable = false, updatable = false)
    private LocalDateTime logCreatedAt;

    public ServerLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VirtualServer getServer() { return server; }
    public void setServer(VirtualServer server) { this.server = server; }

    public LogLevel getLogLevel() { return logLevel; }
    public void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }

    public String getLogMessage() { return logMessage; }
    public void setLogMessage(String logMessage) { this.logMessage = logMessage; }

    public LocalDateTime getLogCreatedAt() { return logCreatedAt; }
    public void setLogCreatedAt(LocalDateTime logCreatedAt) { this.logCreatedAt = logCreatedAt; }
}
