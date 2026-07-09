package ru.ssau.virtualservers.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.virtualservers.dto.CreateServerRequestDTO;
import ru.ssau.virtualservers.dto.ServerResponseDTO;
import ru.ssau.virtualservers.entity.AppUser;
import ru.ssau.virtualservers.entity.enums.ServerStatus;
import ru.ssau.virtualservers.entity.enums.UserRole;
import ru.ssau.virtualservers.repository.AppUserRepository;
import ru.ssau.virtualservers.repository.ServerConfigurationRepository;
import ru.ssau.virtualservers.repository.ServerLogRepository;
import ru.ssau.virtualservers.repository.VirtualServerRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//интеграционные тесты для VirtualServerController
@SpringBootTest
class VirtualServerControllerIT {

    @Autowired
    private VirtualServerController serverController;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private VirtualServerRepository serverRepository;

    @Autowired
    private ServerConfigurationRepository configRepository;

    @Autowired
    private ServerLogRepository logRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser testUser;
    private Authentication userAuth;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setUsername("server_test_user");
        testUser.setPasswordHash(passwordEncoder.encode("pass"));
        testUser.setUserRole(UserRole.USER);
        testUser = userRepository.save(testUser);

        userAuth = new UsernamePasswordAuthenticationToken(
            testUser.getId(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @AfterEach
    void tearDown() {
        logRepository.deleteAllInBatch();
        configRepository.deleteAllInBatch();
        serverRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void createServer_returnsCreatedServerWithStatusSTARTING() {
        CreateServerRequestDTO request = new CreateServerRequestDTO("web-server", null, 2, 1024);

        ResponseEntity<ServerResponseDTO> response = serverController.createServer(request, userAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServerResponseDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("web-server", result.name());
        assertEquals(ServerStatus.STARTING, result.status());
        assertEquals(2, result.cpu());
        assertEquals(1024, result.ram());
    }

    @Test
    void createServer_automaticallyCreatesConfiguration() {
        CreateServerRequestDTO request = new CreateServerRequestDTO("db-server", null, 4, 2048);

        ResponseEntity<ServerResponseDTO> response = serverController.createServer(request, userAuth);

        Long serverId = response.getBody().id();
        assertTrue(configRepository.findById(serverId).isPresent());
    }

    @Test
    void getMyServers_returnsOnlyCurrentUserServers() {
        serverController.createServer(new CreateServerRequestDTO("server-1", null, 1, 512), userAuth);
        serverController.createServer(new CreateServerRequestDTO("server-2", null, 2, 1024), userAuth);

        AppUser otherUser = new AppUser();
        otherUser.setUsername("other_user");
        otherUser.setPasswordHash(passwordEncoder.encode("pass"));
        otherUser.setUserRole(UserRole.USER);
        otherUser = userRepository.save(otherUser);
        Authentication otherAuth = new UsernamePasswordAuthenticationToken(
            otherUser.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        serverController.createServer(new CreateServerRequestDTO("other-server", null, 1, 256), otherAuth);

        ResponseEntity<List<ServerResponseDTO>> response = serverController.getMyServers(userAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ServerResponseDTO> servers = response.getBody();
        assertNotNull(servers);
        assertEquals(2, servers.size());
        assertTrue(servers.stream().anyMatch(s -> s.name().equals("server-1")));
        assertFalse(servers.stream().anyMatch(s -> s.name().equals("other-server")));
    }

    @Test
    void updateStatus_changesServerStatusInDB() {
        CreateServerRequestDTO request = new CreateServerRequestDTO("status-test", null, 1, 512);
        Long serverId = serverController.createServer(request, userAuth).getBody().id();

        ResponseEntity<Void> response = serverController.updateStatus(serverId, ServerStatus.RUNNING, userAuth);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        ServerResponseDTO updated = serverController.getServerById(serverId, userAuth).getBody();
        assertEquals(ServerStatus.RUNNING, updated.status());
    }

    @Test
    void updateStatus_createsLogEntry() {
        Long serverId = serverController.createServer(
            new CreateServerRequestDTO("log-test", null, 1, 512), userAuth
        ).getBody().id();
        long logsBefore = logRepository.count();

        serverController.updateStatus(serverId, ServerStatus.STOPPED, userAuth);

        assertTrue(logRepository.count() > logsBefore);
    }

    @Test
    void updateConfig_updatesCpuAndRamInDB() {
        Long serverId = serverController.createServer(
            new CreateServerRequestDTO("config-test", null, 1, 512), userAuth
        ).getBody().id();

        serverController.updateConfig(serverId, 8, 4096, userAuth);

        ServerResponseDTO updated = serverController.getServerById(serverId, userAuth).getBody();
        assertEquals(8, updated.cpu());
        assertEquals(4096, updated.ram());
    }

    @Test
    void deleteServer_removesServerFromDB() {
        Long serverId = serverController.createServer(
            new CreateServerRequestDTO("to-delete", null, 1, 512), userAuth
        ).getBody().id();

        ResponseEntity<Void> response = serverController.deleteServer(serverId, userAuth);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(serverRepository.existsById(serverId));
    }

    @Test
    void deleteServer_cascadesRemovesConfiguration() {
        Long serverId = serverController.createServer(
            new CreateServerRequestDTO("cascade-test", null, 1, 512), userAuth
        ).getBody().id();

        serverController.deleteServer(serverId, userAuth);

        assertFalse(configRepository.existsById(serverId));
    }
}

