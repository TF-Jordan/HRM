package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.resource.domain.DuplicateResourceCodeException;
import yowyob.comops.api.resource.domain.InvalidResourceStateException;
import yowyob.comops.api.resource.domain.MaterialResourceNotFoundException;
import yowyob.comops.api.resource.domain.ResourceAssignmentNotFoundException;
import yowyob.comops.api.resource.domain.ResourceConsistencyException;
import yowyob.comops.api.resource.domain.ResourceSearchUnavailableException;
import yowyob.comops.api.resource.domain.ResourceReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ResourceController.class)
public class ResourceExceptionHandler {

    @ExceptionHandler(DuplicateResourceCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "RESOURCE_CODE_DUPLICATE"));
    }

    @ExceptionHandler({MaterialResourceNotFoundException.class, ResourceReservationNotFoundException.class,
            ResourceAssignmentNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException exception) {
        String errorCode = exception instanceof MaterialResourceNotFoundException ? "RESOURCE_NOT_FOUND"
                : exception instanceof ResourceReservationNotFoundException ? "RESOURCE_RESERVATION_NOT_FOUND"
                : "RESOURCE_ASSIGNMENT_NOT_FOUND";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler({InvalidResourceStateException.class, ResourceConsistencyException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(RuntimeException exception) {
        String errorCode = exception instanceof InvalidResourceStateException
                ? "RESOURCE_INVALID_STATE"
                : "RESOURCE_INCONSISTENT_REQUEST";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "RESOURCE_INVALID_REQUEST"));
    }

    @ExceptionHandler(ResourceSearchUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchUnavailable(ResourceSearchUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(exception.getMessage(), "RESOURCE_SEARCH_UNAVAILABLE"));
    }
}
