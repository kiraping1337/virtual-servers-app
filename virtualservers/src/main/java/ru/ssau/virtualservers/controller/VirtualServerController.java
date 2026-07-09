package ru.ssau.virtualservers.controller;

import ru.ssau.virtualservers.dto.CreateServerRequestDTO;
import ru.ssau.virtualservers.dto.ServerResponseDTO;
import ru.ssau.virtualservers.entity.enums.ServerStatus;
import ru.ssau.virtualservers.service.VirtualServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class VirtualServerController {

    private final VirtualServerService serverService;

    public VirtualServerController(VirtualServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/user/my")
    public ResponseEntity<List<ServerResponseDTO>> getMyServers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal(); 
        return ResponseEntity.ok(serverService.getUserServers(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ServerResponseDTO>> getUserServers(@PathVariable Long userId) {
        return ResponseEntity.ok(serverService.getUserServers(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerResponseDTO> getServerById(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(serverService.getServerById(id, userId, authentication.getAuthorities()));
    }

    @PostMapping
    public ResponseEntity<ServerResponseDTO> createServer(@RequestBody CreateServerRequestDTO request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(serverService.createServer(userId, request.name(), request.cpu(), request.ram()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam ServerStatus status, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        serverService.changeServerStatus(id, status,userId, authentication.getAuthorities());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServer(@PathVariable Long id,  Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        serverService.deleteServer(id, userId, authentication.getAuthorities());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/config")
    public ResponseEntity<Void> updateConfig(
            @PathVariable Long id, 
            @RequestParam int cpu, 
            @RequestParam int ram,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        serverService.updateConfig(id, cpu, ram, userId, authentication.getAuthorities());
        return ResponseEntity.ok().build();
    }
    
}