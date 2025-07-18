package org.balczukiewicz.notekeeperservice.service;

import org.balczukiewicz.notekeeperservice.dto.AuthRequest;
import org.balczukiewicz.notekeeperservice.dto.AuthResponse;

public interface AuthService {

    AuthResponse authenticate(AuthRequest request);

    boolean validateCredentials(String username, String password);

}
