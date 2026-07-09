package ru.ssau.virtualservers.repository;

import ru.ssau.virtualservers.entity.VirtualServer;
import ru.ssau.virtualservers.entity.enums.ServerStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualServerRepository extends JpaRepository<VirtualServer, Long> {
    
    List<VirtualServer> findAllByUserId(Long userId);

    List<VirtualServer> findAllByServerStatus(ServerStatus serverStatus);

    Optional<VirtualServer> findByServerIpAddress(String serverIpAddress);
}