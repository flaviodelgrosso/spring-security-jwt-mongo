package app.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.api.AuthApi;
import app.dto.auth.AuthRequest;
import app.dto.auth.AuthResponse;
import app.dto.auth.RegisterRequest;
import app.services.AuthService;

@RequiredArgsConstructor
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> signin(AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Override
    public ResponseEntity<AuthResponse> signup(RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Override
    public String logout() {
        return "User has been successfully logged out.";
    }

}