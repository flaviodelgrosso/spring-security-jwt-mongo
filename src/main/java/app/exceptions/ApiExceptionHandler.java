package app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public HttpError resourceNotFoundException(ResourceNotFoundException ex) {
        return new HttpError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public HttpError unauthorizedException(UnauthorizedException ex) {
        return new HttpError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    }

    @ExceptionHandler(value = ForbiddenException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public HttpError forbiddenException(ForbiddenException ex) {
        return new HttpError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ExceptionHandler(value = PreconditionFailedException.class)
    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    public HttpError preconditionFailedException(PreconditionFailedException ex) {
        return new HttpError(HttpStatus.PRECONDITION_FAILED.value(), ex.getMessage());
    }
}
