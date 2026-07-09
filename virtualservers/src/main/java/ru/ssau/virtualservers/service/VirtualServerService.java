package ru.ssau.virtualservers.service;

import ru.ssau.virtualservers.dto.ServerResponseDTO;
import ru.ssau.virtualservers.entity.*;
import ru.ssau.virtualservers.entity.enums.LogLevel;
import ru.ssau.virtualservers.entity.enums.ServerStatus;
import ru.ssau.virtualservers.repository.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VirtualServerService {

    private final VirtualServerRepository serverRepository;
    private final ServerConfigurationRepository configRepository;
    private final ServerLogRepository logRepository;
    private final AppUserRepository userRepository;

    public VirtualServerService(VirtualServerRepository serverRepository, 
                                ServerConfigurationRepository configRepository, 
                                ServerLogRepository logRepository,
                                AppUserRepository userRepository) {
        this.serverRepository = serverRepository;
        this.configRepository = configRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ServerResponseDTO createServer(Long userId, String serverName, int cpu, int ram) {
        AppUser owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Владелец не найден"));

        VirtualServer server = new VirtualServer();
        server.setUser(owner);
        server.setServerName(serverName);
        server.setServerIpAddress(generateRandomIp());
        server.setServerStatus(ServerStatus.STARTING); 

        VirtualServer savedServer = serverRepository.save(server);

        ServerConfiguration config = new ServerConfiguration();
        config.setServer(savedServer);
        config.setCpu(cpu);
        config.setRam(ram);
        configRepository.save(config);

        savedServer.setConfiguration(config); 

        saveLog(savedServer, LogLevel.INFO, "Сервер успешно создан и инициализирован");

        return convertToDTO(savedServer);
    }

    public List<ServerResponseDTO> getUserServers(Long userId) {
        return serverRepository.findAllByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeServerStatus(Long serverId, ServerStatus newStatus, Long userId, Collection<? extends GrantedAuthority> authorities) {
        VirtualServer server = findAndVerifyOwner(serverId, userId, authorities);
        
        server.setServerStatus(newStatus);
        serverRepository.save(server);

        saveLog(server, LogLevel.INFO, "Статус сервера изменен на: " + newStatus);
    }
    
    @Transactional
    public void deleteServer(Long serverId, Long userId, Collection<? extends GrantedAuthority> authorities) {
        VirtualServer server = findAndVerifyOwner(serverId, userId, authorities);
        serverRepository.delete(server);
    }

    @Transactional
    public void updateConfig(Long serverId, int cpu, int ram, Long userId, Collection<? extends GrantedAuthority> authorities) {
        VirtualServer server = findAndVerifyOwner(serverId, userId, authorities);
        
        ServerConfiguration config = server.getConfiguration();
        config.setCpu(cpu);
        config.setRam(ram);
        configRepository.save(config);
        
        saveLog(server, LogLevel.INFO, "Конфигурация обновлена: CPU=" + cpu + ", RAM=" + ram);
    }

    private ServerResponseDTO convertToDTO(VirtualServer server) {
        return new ServerResponseDTO(
                server.getId(),
                server.getServerName(),
                server.getServerIpAddress(),
                server.getServerStatus(),
                server.getConfiguration().getCpu(),
                server.getConfiguration().getRam()
        );
    }

    private void saveLog(VirtualServer server, LogLevel level, String message) {
        ServerLog log = new ServerLog();
        log.setServer(server);
        log.setLogLevel(level);
        log.setLogMessage(message);
        logRepository.save(log);
    }

    private String generateRandomIp() {
        return "192.168." + (int)(Math.random() * 255) + "." + (int)(Math.random() * 254 + 1);
    }

    public ServerResponseDTO getServerById(Long serverId, Long userId, Collection<? extends GrantedAuthority> authorities) {
        VirtualServer server = findAndVerifyOwner(serverId, userId, authorities);
        return convertToDTO(server);
    }

    private VirtualServer findAndVerifyOwner(Long serverId, Long userId, Collection<? extends GrantedAuthority> authorities) {
        VirtualServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Сервер не найден"));
               
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!server.getUser().getId().equals(userId) && !isAdmin) {
            throw new RuntimeException("Доступ запрещен: недостаточно прав");
        }
        return server;
    }
    
}