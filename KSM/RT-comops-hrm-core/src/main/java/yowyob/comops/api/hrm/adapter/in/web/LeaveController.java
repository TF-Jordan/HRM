package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.ManageLeaveUseCase;
import yowyob.comops.api.hrm.application.port.in.RunLeaveAccrualUseCase;
import yowyob.comops.api.hrm.application.port.in.SubmitLeaveCommand;
import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/leaves")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class LeaveController {

    private final ManageLeaveUseCase manageLeaveUseCase;
    private final RunLeaveAccrualUseCase runLeaveAccrualUseCase;

    public LeaveController(ManageLeaveUseCase manageLeaveUseCase,
                          RunLeaveAccrualUseCase runLeaveAccrualUseCase) {
        this.manageLeaveUseCase = manageLeaveUseCase;
        this.runLeaveAccrualUseCase = runLeaveAccrualUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:create')")
    public Mono<ResponseEntity<ApiResponse<LeaveResponse>>> submitLeave(
            @Valid @RequestBody Mono<SubmitLeaveRequest> requestMono) {
        return requestMono.map(SubmitLeaveRequest::toCommand)
                .flatMap(manageLeaveUseCase::submitLeave)
                .map(LeaveResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Leave request submitted.")));
    }

    @PutMapping("/{leaveRequestId}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:approve')")
    public Mono<ResponseEntity<ApiResponse<LeaveResponse>>> approveLeave(@PathVariable UUID leaveRequestId) {
        return manageLeaveUseCase.approveLeave(leaveRequestId)
                .map(LeaveResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request approved.")));
    }

    @PutMapping("/{leaveRequestId}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:approve')")
    public Mono<ResponseEntity<ApiResponse<LeaveResponse>>> rejectLeave(
            @PathVariable UUID leaveRequestId,
            @RequestBody Mono<RejectLeaveRequest> requestMono) {
        return requestMono.flatMap(req -> manageLeaveUseCase.rejectLeave(leaveRequestId, req.commentaire()))
                .map(LeaveResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request rejected.")));
    }

    @PutMapping("/{leaveRequestId}/cancel")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:create')")
    public Mono<ResponseEntity<ApiResponse<LeaveResponse>>> cancelLeave(@PathVariable UUID leaveRequestId) {
        return manageLeaveUseCase.cancelLeave(leaveRequestId)
                .map(LeaveResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request cancelled.")));
    }

    @GetMapping("/{leaveRequestId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:read')")
    public Mono<ResponseEntity<ApiResponse<LeaveResponse>>> getLeaveRequest(@PathVariable UUID leaveRequestId) {
        return manageLeaveUseCase.getLeaveRequest(leaveRequestId)
                .map(LeaveResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:read')")
    public Mono<ResponseEntity<ApiResponse<List<LeaveResponse>>>> listLeavesByEmployee(
            @PathVariable UUID employeeId) {
        return manageLeaveUseCase.listLeavesByEmployee(employeeId)
                .map(LeaveResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Leave requests fetched.")));
    }

    @GetMapping("/pending")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:approve')")
    public Mono<ResponseEntity<ApiResponse<List<LeaveResponse>>>> listPendingLeaves(
            @RequestParam UUID organizationId,
            @RequestParam(required = false) UUID agencyId) {
        return manageLeaveUseCase.listPendingLeaves(organizationId, agencyId)
                .map(LeaveResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Pending leave requests fetched.")));
    }

    /**
     * Manually triggers the monthly leave accrual for the caller's organization. Idempotent per
     * calendar month (the scheduler runs this automatically; this endpoint is for ops/demo).
     */
    @PostMapping("/accrual/run")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:approve')")
    public Mono<ResponseEntity<ApiResponse<Integer>>> runAccrual() {
        return runLeaveAccrualUseCase.runForCurrentContext()
                .map(credited -> ResponseEntity.ok(ApiResponse.success(credited,
                        "Leave accrual executed for " + credited + " employee(s).")));
    }

    // --- Request/Response DTOs ---

    public record SubmitLeaveRequest(UUID employeeId, String type, LocalDate dateDebut, LocalDate dateFin,
            String motif, UUID justificatifFileId) {
        SubmitLeaveCommand toCommand() {
            return new SubmitLeaveCommand(employeeId, type, dateDebut, dateFin, motif, justificatifFileId);
        }
    }

    public record RejectLeaveRequest(String commentaire) {}

    public record LeaveResponse(UUID id, UUID employeeId, String type, LocalDate dateDebut, LocalDate dateFin,
            BigDecimal nbJours, String status, String motif, UUID valideurPartyId, String valideurDisplayName,
            Instant dateValidation, String commentaireValideur, UUID justificatifFileId) {
        static LeaveResponse from(LeaveRequest r) {
            return new LeaveResponse(r.id(), r.employeeId(), r.type().name(), r.dateDebut(), r.dateFin(),
                    r.nbJours(), r.status().name(), r.motif(), r.valideurPartyId(), r.valideurDisplayName(),
                    r.dateValidation(), r.commentaireValideur(), r.justificatifFileId());
        }
    }
}
