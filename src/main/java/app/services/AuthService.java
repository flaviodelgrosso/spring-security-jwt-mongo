package app.services;

import app.dto.auth.AuthRequest;
import app.dto.auth.AuthResponse;
import app.dto.auth.RegisterRequest;
import app.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final MongoService mongoService;

    public AuthResponse authenticate(AuthRequest req) {
        User user = userService.signInValidationHandler(req);

        var authenticationToken = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
        var authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return this.authResponseHandler(user);
    }

    // TODO: RegisterRequest as User interface projection?
    public AuthResponse register(RegisterRequest req) {
        var user = Optional.of(req)
                .map(userService::signupValidationHandler)
                .map(mongoService::saveUser)
                .orElseThrow(() -> new RuntimeException("User registration failed."));

        return this.authResponseHandler(user);
    }

    private AuthResponse authResponseHandler(User user) {
        var jwtToken = jwtService.generateToken(user);

        jwtService.revokeAllUserTokens(user);
        jwtService.saveUserToken(user, jwtToken);

        return AuthResponse.builder()
                .jwt(jwtToken)
                .user(user)
                .build();
    }

}
