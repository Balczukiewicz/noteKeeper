package org.balczukiewicz.notekeeperservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balczukiewicz.notekeeperservice.api.AuthApi;
import org.balczukiewicz.notekeeperservice.dto.AuthRequest;
import org.balczukiewicz.notekeeperservice.dto.AuthResponse;
import org.balczukiewicz.notekeeperservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> authenticate(AuthRequest request) {
        log.info("Authentication request received for user: {}", request.getUsername());
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
