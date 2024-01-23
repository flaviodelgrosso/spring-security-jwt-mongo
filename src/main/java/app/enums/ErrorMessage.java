package app.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {

    EMAIL_NOT_FOUND("Email: %s not found."),
    EMAIL_ALREADY_IN_USE("The email %s is already in use."),
    EMAIL_NOT_VALID("Email not valid."),
    PASSWORD_NOT_VALID("Password not valid."),
    PASSWORD_MUST_RESPECT_RULES("Password must respect rules."),
    ACCESS_DENIED("Access denied."),

    INVALID_JWT_SIGNATURE("Invalid JWT signature."),
    INVALID_JWT_TOKEN("Invalid JWT token."),
    EXPIRED_JWT_TOKEN("JWT token is expired."),
    UNSUPPORTED_JWT_TOKEN("JWT token is unsupported."),
    JWT_TOKEN_IS_NULL_OR_EMPTY("JWT claims string is null or empty.");

    private final String msg;
}