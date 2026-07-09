package ru.ssau.virtualservers.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ssau.virtualservers.dto.AuthResponseDTO;
import ru.ssau.virtualservers.dto.LoginRequestDTO;
import ru.ssau.virtualservers.dto.RefreshRequestDTO;
import ru.ssau.virtualservers.entity.AppUser;
import ru.ssau.virtualservers.service.AppUserService;
import ru.ssau.virtualservers.service.TokenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AppUserService userService, TokenService tokenService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            AppUser user = userService.findEntityByUsername(username); 
            return ResponseEntity.ok(generateTokens(user));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ошибка: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequestDTO request) {
        try {
            String token = request.refreshToken();

            if (!tokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидный или просроченный Refresh Token");
            }

            Map<String, Object> payload = tokenService.decodePayload(token.split("\\.")[0]);
            Long userId = ((Number) payload.get("userId")).longValue();

            AppUser user = userService.findEntityById(userId);
            return ResponseEntity.ok(generateTokens(user));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ошибка обработки токена");
        }
    }

    private AuthResponseDTO generateTokens(AppUser user) {
        long currentTime = System.currentTimeMillis() / 1000;

        //Access Token (15 минут)
        Map<String, Object> accessPayload = new HashMap<>();
        accessPayload.put("userId", user.getId());
        accessPayload.put("roles", List.of(user.getUserRole().name()));
        accessPayload.put("iat", currentTime);
        accessPayload.put("exp", currentTime + (15 * 60));
        String accessToken = tokenService.generateToken(accessPayload);

        //Refresh Token (7 дней)
        Map<String, Object> refreshPayload = new HashMap<>();
        refreshPayload.put("userId", user.getId());
        refreshPayload.put("iat", currentTime);
        refreshPayload.put("exp", currentTime + (7 * 24 * 60 * 60));
        String refreshToken = tokenService.generateToken(refreshPayload);

        return new AuthResponseDTO(accessToken, refreshToken);
    }
}