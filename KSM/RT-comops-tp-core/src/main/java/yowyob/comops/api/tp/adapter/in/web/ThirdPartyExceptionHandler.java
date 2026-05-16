package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.tp.domain.DuplicateThirdPartyReferenceException;
import yowyob.comops.api.tp.domain.ProspectConversionNotAllowedException;
import yowyob.comops.api.tp.domain.ThirdPartyAccountingAccountAlreadyExistsException;
import yowyob.comops.api.tp.domain.ThirdPartyBankAccountAlreadyExistsException;
import yowyob.comops.api.tp.domain.ThirdPartyBankAccountNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartyLookupNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartyNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartySearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        ThirdPartyController.class,
        ClientController.class,
        CustomerController.class,
        SupplierController.class,
        ProspectController.class,
        SalesAgentController.class
})
public class ThirdPartyExceptionHandler {

    @ExceptionHandler(DuplicateThirdPartyReferenceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateReference(DuplicateThirdPartyReferenceException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "THIRD_PARTY_REFERENCE_DUPLICATE"));
    }

    @ExceptionHandler({ThirdPartyAccountingAccountAlreadyExistsException.class,
            ThirdPartyBankAccountAlreadyExistsException.class})
    public ResponseEntity<ApiResponse<Void>> handleDuplicateDetails(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "THIRD_PARTY_DETAIL_DUPLICATE"));
    }

    @ExceptionHandler({ThirdPartyNotFoundException.class, ThirdPartyLookupNotFoundException.class,
            ThirdPartyBankAccountNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "THIRD_PARTY_NOT_FOUND"));
    }

    @ExceptionHandler(ProspectConversionNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handleConversionNotAllowed(ProspectConversionNotAllowedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PROSPECT_CONVERSION_NOT_ALLOWED"));
    }

    @ExceptionHandler(ThirdPartySearchUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchUnavailable(ThirdPartySearchUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(exception.getMessage(), "THIRD_PARTY_SEARCH_UNAVAILABLE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "THIRD_PARTY_INVALID_REQUEST"));
    }
}
