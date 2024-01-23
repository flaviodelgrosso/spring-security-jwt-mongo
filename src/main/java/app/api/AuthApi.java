package app.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.dto.auth.AuthRequest;
import app.dto.auth.AuthResponse;
import app.dto.auth.RegisterRequest;

@RequestMapping("/auth")
public interface AuthApi {

    @PostMapping(value = "/login")
    ResponseEntity<AuthResponse> signin(@RequestBody AuthRequest request);

    @PostMapping(value = "/register")
    ResponseEntity<AuthResponse> signup(@RequestBody RegisterRequest request);

    @PostMapping(value = "/logout")
    String logout();

}