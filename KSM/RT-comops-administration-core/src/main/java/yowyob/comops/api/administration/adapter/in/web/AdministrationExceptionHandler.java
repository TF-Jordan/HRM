package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.domain.PermissionCatalogValidationException;
import yowyob.comops.api.administration.domain.ProtectedRoleMutationException;
import yowyob.comops.api.administration.domain.RoleStillAssignedException;
import yowyob.comops.api.administration.domain.UserRoleAssignmentNotFoundException;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.roles.domain.DuplicateRoleCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdministrationController.class)
public class AdministrationExceptionHandler {

    @ExceptionHandler(DuplicateRoleCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateRoleCode(DuplicateRoleCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_ROLE_CODE_DUPLICATE"));
    }

    @ExceptionHandler(PermissionCatalogValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionCatalogValidation(PermissionCatalogValidationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_PERMISSION_INVALID"));
    }

    @ExceptionHandler(ProtectedRoleMutationException.class)
    public ResponseEntity<ApiResponse<Void>> handleProtectedRoleMutation(ProtectedRoleMutationException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_ROLE_PROTECTED"));
    }

    @ExceptionHandler(RoleStillAssignedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleStillAssigned(RoleStillAssignedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_ROLE_STILL_ASSIGNED"));
    }

    @ExceptionHandler(UserRoleAssignmentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAssignmentNotFound(UserRoleAssignmentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_ASSIGNMENT_NOT_FOUND"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_INVALID_REQUEST"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "ADMIN_STATE_CONFLICT"));
    }
}
