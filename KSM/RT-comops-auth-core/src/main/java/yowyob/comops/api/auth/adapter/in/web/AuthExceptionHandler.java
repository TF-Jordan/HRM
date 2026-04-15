package yowyob.comops.api.auth.adapter.in.web;

import yowyob.comops.api.auth.domain.DuplicateUsernameException;
import yowyob.comops.api.auth.domain.InvalidLoginCredentialsException;
import yowyob.comops.api.common.domain.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUsername(DuplicateUsernameException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "USERNAME_DUPLICATE"));
    }

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidLoginCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(exception.getMessage(), "AUTH_INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "AUTH_INVALID_REQUEST"));
    }
}
