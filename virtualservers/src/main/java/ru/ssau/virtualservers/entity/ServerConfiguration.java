package ru.ssau.virtualservers.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_configuration")
public class ServerConfiguration {

    @Id
    @Column(name = "server_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "server_id")
    private VirtualServer server;

    @Column(nullable = false)
    private Integer cpu;

    @Column(nullable = false)
    private Integer ram;

    @UpdateTimestamp
    @Column(name = "configuration_updated_at", nullable = false)
    private LocalDateTime configurationUpdatedAt;

    public ServerConfiguration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VirtualServer getServer() { return server; }
    public void setServer(VirtualServer server) { this.server = server; }

    public Integer getCpu() { return cpu; }
    public void setCpu(Integer cpu) { this.cpu = cpu; }

    public Integer getRam() { return ram; }
    public void setRam(Integer ram) { this.ram = ram; }

    public LocalDateTime getConfigurationUpdatedAt() { return configurationUpdatedAt; }
    public void setConfigurationUpdatedAt(LocalDateTime configurationUpdatedAt) { this.configurationUpdatedAt = configurationUpdatedAt; }
}
