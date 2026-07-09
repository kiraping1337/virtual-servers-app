package ru.ssau.virtualservers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.ssau.virtualservers.dto.ServerResponseDTO;
import ru.ssau.virtualservers.entity.AppUser;
import ru.ssau.virtualservers.entity.ServerConfiguration;
import ru.ssau.virtualservers.entity.VirtualServer;
import ru.ssau.virtualservers.entity.enums.ServerStatus;
import ru.ssau.virtualservers.entity.enums.UserRole;
import ru.ssau.virtualservers.repository.AppUserRepository;
import ru.ssau.virtualservers.repository.ServerConfigurationRepository;
import ru.ssau.virtualservers.repository.ServerLogRepository;
import ru.ssau.virtualservers.repository.VirtualServerRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//модульные тесты для VirtualServerService.

class VirtualServerServiceTest {

    private VirtualServerRepository serverRepository;
    private ServerConfigurationRepository configRepository;
    private ServerLogRepository logRepository;
    private AppUserRepository userRepository;
    private VirtualServerService serverService;

    private final Collection<GrantedAuthority> userAuthorities =
        List.of(new SimpleGrantedAuthority("ROLE_USER"));
    private final Collection<GrantedAuthority> adminAuthorities =
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

    @BeforeEach
    void setUp() {
        serverRepository = mock(VirtualServerRepository.class);
        configRepository = mock(ServerConfigurationRepository.class);
        logRepository    = mock(ServerLogRepository.class);
        userRepository   = mock(AppUserRepository.class);
        serverService = new VirtualServerService(
            serverRepository, configRepository, logRepository, userRepository
        );
    }

    @Test
    void getUserServers_returnsUserServers() {
        VirtualServer server = buildServer(1L, "my-server", ServerStatus.RUNNING, buildUser(1L), 2, 1024);
        when(serverRepository.findAllByUserId(1L)).thenReturn(List.of(server));

        List<ServerResponseDTO> result = serverService.getUserServers(1L);

        assertEquals(1, result.size());
        assertEquals("my-server", result.get(0).name());
        assertEquals(ServerStatus.RUNNING, result.get(0).status());
        assertEquals(2, result.get(0).cpu());
        assertEquals(1024, result.get(0).ram());
    }

    @Test
    void getUserServers_noServers_returnsAnEmptyList() {
        when(serverRepository.findAllByUserId(99L)).thenReturn(List.of());

        List<ServerResponseDTO> result = serverService.getUserServers(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void changeServerStatus_ownerChangesStatus_successfully() {
        AppUser owner = buildUser(1L);
        VirtualServer server = buildServer(10L, "srv", ServerStatus.RUNNING, owner, 1, 512);
        when(serverRepository.findById(10L)).thenReturn(Optional.of(server));
        when(serverRepository.save(any())).thenReturn(server);

        serverService.changeServerStatus(10L, ServerStatus.STOPPED, 1L, userAuthorities);

        assertEquals(ServerStatus.STOPPED, server.getServerStatus());
        verify(serverRepository).save(server);
        verify(logRepository).save(any());
    }

    @Test
    void changeServerStatus_otherUser_throwsException() {
        AppUser owner = buildUser(1L);
        VirtualServer server = buildServer(10L, "srv", ServerStatus.RUNNING, owner, 1, 512);
        when(serverRepository.findById(10L)).thenReturn(Optional.of(server));

        assertThrows(RuntimeException.class, () ->
            serverService.changeServerStatus(10L, ServerStatus.STOPPED, 2L, userAuthorities)
        );
        verify(serverRepository, never()).save(any());
    }

    @Test
    void changeServerStatus_adminCanChangeOtherServer() {
        AppUser owner = buildUser(1L);
        VirtualServer server = buildServer(10L, "srv", ServerStatus.RUNNING, owner, 1, 512);
        when(serverRepository.findById(10L)).thenReturn(Optional.of(server));
        when(serverRepository.save(any())).thenReturn(server);

        serverService.changeServerStatus(10L, ServerStatus.MAINTENANCE, 99L, adminAuthorities);

        assertEquals(ServerStatus.MAINTENANCE, server.getServerStatus());
    }

    @Test
    void deleteServer_ownerDeletes_successfully() {
        AppUser owner = buildUser(1L);
        VirtualServer server = buildServer(5L, "to-delete", ServerStatus.STOPPED, owner, 1, 256);
        when(serverRepository.findById(5L)).thenReturn(Optional.of(server));

        serverService.deleteServer(5L, 1L, userAuthorities);

        verify(serverRepository).delete(server);
    }

    @Test
    void deleteServer_serverNotFound_throwsException() {
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            serverService.deleteServer(999L, 1L, userAuthorities)
        );
    }

    //вспомогательные методы

    private AppUser buildUser(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setUserRole(UserRole.USER);
        return user;
    }

    private VirtualServer buildServer(Long id, String name, ServerStatus status,
                                       AppUser owner, int cpu, int ram) {
        VirtualServer server = new VirtualServer();
        server.setId(id);
        server.setServerName(name);
        server.setServerStatus(status);
        server.setServerIpAddress("192.168.1.1");
        server.setUser(owner);

        ServerConfiguration config = new ServerConfiguration();
        config.setCpu(cpu);
        config.setRam(ram);
        config.setServer(server);
        server.setConfiguration(config);

        return server;
    }
}