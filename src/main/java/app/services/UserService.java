package app.services;

import app.dto.auth.AuthRequest;
import app.dto.auth.RegisterRequest;
import app.enums.ErrorMessage;
import app.enums.Role;
import app.exceptions.PreconditionFailedException;
import app.exceptions.ResourceNotFoundException;
import app.exceptions.UnauthorizedException;
import app.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final MongoService mongoService;

    public User signInValidationHandler(AuthRequest req) {
        return mongoService.findUserByEmail(req.getEmail())
                .filter(u -> this.isEmailPresent(req.getEmail()))
                .filter(u -> this.isPasswordMatching(req.getPassword(), u))
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessage.EMAIL_NOT_FOUND.getMsg(), req.getEmail())));
    }

    public User signupValidationHandler(RegisterRequest req) {
        var user = User.builder()
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .password(this.encodeUserNewPassword(req.getPassword()))
                .role(this.setUserRole())
                .teamName(req.getTeamName())
                .createdAt(new Date())
                .build();

        return Optional.of(user)
                .filter(u -> this.isEmailAlreadyInUse(req.getEmail()))
                .filter(u -> UserService.isValidPassword(req.getPassword()) && UserService.isValidEmail(req.getEmail()))
                .orElseThrow(() -> new UnauthorizedException(ErrorMessage.ACCESS_DENIED.getMsg()));
    }

    public User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return mongoService.findUserByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessage.EMAIL_NOT_FOUND.getMsg(), currentUsername)));
    }

    private boolean isEmailPresent(String email) {
        if (mongoService.findUserByEmail(email).isPresent()) {
            return true;
        }

        throw new ResourceNotFoundException(String.format(ErrorMessage.EMAIL_NOT_FOUND.getMsg(), email));
    }

    private boolean isPasswordMatching(String password, User u) {
        if (passwordEncoder.matches(password, u.getPassword())) {
            return true;
        }

        throw new UnauthorizedException(ErrorMessage.PASSWORD_NOT_VALID.getMsg());
    }

    private boolean isEmailAlreadyInUse(String email) {
        if (mongoService.findUserByEmail(email).isPresent()) {
            throw new PreconditionFailedException(String.format(ErrorMessage.EMAIL_ALREADY_IN_USE.getMsg(), email));
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        if (email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return true;
        }

        throw new PreconditionFailedException(ErrorMessage.EMAIL_NOT_VALID.getMsg());
    }

    private static boolean isValidPassword(String password) {
        // regex: Must be from 8 to 32 characters long, at least 1 special character
        // (only [!, #, %, @]), at least 1 upper-case letter, at least 1 lower-case
        // letter, at least 1 number
        if (password.matches("^(?=[^a-z]*[a-z])(?=[^A-Z]*[A-Z])(?=\\D*\\d)(?=[^!#%@]*[!#%@])[A-Za-z0-9!#%@]{8,32}$")) {
            return true;
        }

        throw new PreconditionFailedException(ErrorMessage.PASSWORD_MUST_RESPECT_RULES.getMsg());
    }

    private Role setUserRole() {
        // TODO: setup roles according to teamId and teamName
        return Role.ADMIN;
    }

    private String encodeUserNewPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
