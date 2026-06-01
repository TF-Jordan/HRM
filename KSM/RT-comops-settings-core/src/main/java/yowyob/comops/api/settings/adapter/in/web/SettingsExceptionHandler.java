package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.settings.domain.DocumentSequenceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {SettingsController.class, GeneralOptionsController.class})
public class SettingsExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "SETTINGS_INVALID_REQUEST"));
    }

    @ExceptionHandler(DocumentSequenceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSequenceNotFound(DocumentSequenceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "DOCUMENT_SEQUENCE_NOT_FOUND"));
    }
}
