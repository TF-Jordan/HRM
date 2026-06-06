package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.Valid;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.adapter.in.web.EmployeeController.AddEmergencyContactRequest;
import yowyob.comops.api.hrm.adapter.in.web.EmployeeController.EmergencyContactResponse;
import yowyob.comops.api.hrm.adapter.in.web.EmployeeController.PersonalInfoResponse;
import yowyob.comops.api.hrm.adapter.in.web.EmployeeController.UpdateEmergencyContactRequest;
import yowyob.comops.api.hrm.adapter.in.web.EmployeeController.UpsertPersonalInfoRequest;
import yowyob.comops.api.hrm.application.port.in.ManageEmployeeUseCase;
import yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;

/**
 * Employee self-service: a worker maintains their OWN personal information and emergency contacts.
 * Guarded only by a valid user context — every operation is scoped to the employee record linked
 * to the caller's actor, so a worker can never touch another employee's data.
 */
@Profile("!test-memory")
@RestController("hrmEmployeeSelfServiceController")
@RequestMapping("/api/v1/hrm/employees/me")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class EmployeeSelfServiceController {

    private final ManageEmployeeUseCase useCase;

    public EmployeeSelfServiceController(ManageEmployeeUseCase useCase) {
        this.useCase = useCase;
    }

    @PutMapping("/personal-info")
    public Mono<ResponseEntity<ApiResponse<PersonalInfoResponse>>> upsertMyPersonalInfo(
            @Valid @RequestBody Mono<UpsertPersonalInfoRequest> requestMono) {
        return requestMono.map(UpsertPersonalInfoRequest::toCommand)
                .flatMap(useCase::upsertMyPersonalInfo)
                .map(PersonalInfoResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Personal info saved.")));
    }

    @PostMapping("/emergency-contacts")
    public Mono<ResponseEntity<ApiResponse<EmergencyContactResponse>>> addMyEmergencyContact(
            @Valid @RequestBody Mono<AddEmergencyContactRequest> requestMono) {
        return requestMono.map(AddEmergencyContactRequest::toCommand)
                .flatMap(useCase::addMyEmergencyContact)
                .map(EmergencyContactResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Emergency contact added.")));
    }

    @PatchMapping("/emergency-contacts/{contactId}")
    public Mono<ResponseEntity<ApiResponse<EmergencyContactResponse>>> updateMyEmergencyContact(
            @PathVariable UUID contactId,
            @Valid @RequestBody Mono<UpdateEmergencyContactRequest> requestMono) {
        return requestMono.map(UpdateEmergencyContactRequest::toCommand)
                .flatMap(cmd -> useCase.updateMyEmergencyContact(contactId, cmd))
                .map(EmergencyContactResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Emergency contact updated.")));
    }

    @DeleteMapping("/emergency-contacts/{contactId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteMyEmergencyContact(@PathVariable UUID contactId) {
        return useCase.deleteMyEmergencyContact(contactId)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success(null, "Emergency contact deleted."))));
    }

    @RestControllerAdvice(assignableTypes = EmployeeSelfServiceController.class)
    static class SelfServiceExceptionHandler {

        @ExceptionHandler(EmployeeNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ex.getMessage(), "EMPLOYEE_NOT_FOUND"));
        }

        @ExceptionHandler(EmergencyContactNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleContactNotFound(EmergencyContactNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ex.getMessage(), "EMERGENCY_CONTACT_NOT_FOUND"));
        }
    }
}
