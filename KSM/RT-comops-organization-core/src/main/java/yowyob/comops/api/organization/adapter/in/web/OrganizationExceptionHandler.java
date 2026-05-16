package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.AgencySelfServiceCreationDisabledException;
import yowyob.comops.api.organization.domain.DuplicateAgencyCodeException;
import yowyob.comops.api.organization.domain.DuplicateEmployeeMembershipException;
import yowyob.comops.api.organization.domain.DuplicatePhysicalSpaceCodeException;
import yowyob.comops.api.organization.domain.DuplicateOrganizationCodeException;
import yowyob.comops.api.organization.domain.DuplicatePointOfInterestException;
import yowyob.comops.api.organization.domain.EmployeeMembershipNotFoundException;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.OrganizationSearchUnavailableException;
import yowyob.comops.api.organization.domain.OrganizationSelfServiceCreationDisabledException;
import yowyob.comops.api.organization.domain.PhysicalSpaceNotFoundException;
import yowyob.comops.api.organization.domain.UserAccountNotFoundInOrganizationContextException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        AgencyController.class,
        WarehouseController.class,
        EmployeeController.class,
        OrganizationController.class,
        OrganizationServiceController.class,
        OpeningHoursController.class,
        PointOfInterestController.class,
        PhysicalSpaceController.class,
        AgencyScheduleController.class,
        LegacyPointOfInterestController.class
})
public class OrganizationExceptionHandler {

    @ExceptionHandler(DuplicateOrganizationCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateCode(DuplicateOrganizationCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "ORGANIZATION_CODE_DUPLICATE"));
    }

    @ExceptionHandler(DuplicateAgencyCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateAgencyCode(DuplicateAgencyCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "AGENCY_CODE_DUPLICATE"));
    }

    @ExceptionHandler(DuplicatePointOfInterestException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicatePointOfInterest(DuplicatePointOfInterestException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "POINT_OF_INTEREST_DUPLICATE"));
    }

    @ExceptionHandler(DuplicatePhysicalSpaceCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicatePhysicalSpace(
            DuplicatePhysicalSpaceCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PHYSICAL_SPACE_CODE_DUPLICATE"));
    }

    @ExceptionHandler(DuplicateEmployeeMembershipException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmployee(DuplicateEmployeeMembershipException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "EMPLOYEE_MEMBERSHIP_DUPLICATE"));
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrganizationNotFound(OrganizationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "ORGANIZATION_NOT_FOUND"));
    }

    @ExceptionHandler(AgencyNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAgencyNotFound(AgencyNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "AGENCY_NOT_FOUND"));
    }

    @ExceptionHandler(PhysicalSpaceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePhysicalSpaceNotFound(PhysicalSpaceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "PHYSICAL_SPACE_NOT_FOUND"));
    }

    @ExceptionHandler({EmployeeMembershipNotFoundException.class, UserAccountNotFoundInOrganizationContextException.class})
    public ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "EMPLOYEE_NOT_FOUND"));
    }

    @ExceptionHandler(OrganizationSearchUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchUnavailable(OrganizationSearchUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(exception.getMessage(), "ORGANIZATION_SEARCH_UNAVAILABLE"));
    }

    @ExceptionHandler(OrganizationSelfServiceCreationDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrganizationSelfServiceDisabled(
            OrganizationSelfServiceCreationDisabledException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(exception.getMessage(), "ORGANIZATION_SELF_SERVICE_DISABLED"));
    }

    @ExceptionHandler(AgencySelfServiceCreationDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleAgencySelfServiceDisabled(
            AgencySelfServiceCreationDisabledException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(exception.getMessage(), "AGENCY_SELF_SERVICE_DISABLED"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "ORGANIZATION_INVALID_REQUEST"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(exception.getMessage(), "ORGANIZATION_FORBIDDEN"));
    }
}
