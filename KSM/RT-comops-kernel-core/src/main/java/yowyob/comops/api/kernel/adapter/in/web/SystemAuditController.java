package yowyob.comops.api.kernel.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.port.in.ListSystemAuditUseCase;
import yowyob.comops.api.kernel.domain.model.SystemAuditEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/system-audits")
public class SystemAuditController {

    private final ListSystemAuditUseCase listSystemAuditUseCase;

    public SystemAuditController(ListSystemAuditUseCase listSystemAuditUseCase) {
        this.listSystemAuditUseCase = listSystemAuditUseCase;
    }

    @GetMapping("/me")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<SystemAuditResponse>>>> getMyActivity(
            @RequestParam(defaultValue = "50") int limit) {
        return listSystemAuditUseCase.listCurrentUserActivity(limit)
                .map(SystemAuditResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "User system audit retrieved.")));
    }

    @GetMapping("/organization")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<SystemAuditResponse>>>> getOrganizationActivity(
            @RequestParam(defaultValue = "50") int limit) {
        return listSystemAuditUseCase.listCurrentOrganizationActivity(limit)
                .map(SystemAuditResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                        "Organization system audit retrieved.")));
    }

    public record SystemAuditResponse(UUID id, UUID tenantId, UUID organizationId, UUID actorUserId, String action,
            String targetType, String targetId, String payloadSummary, Instant createdAt) {
        public static SystemAuditResponse from(SystemAuditEntry entry) {
            return new SystemAuditResponse(entry.id(), entry.tenantId(), entry.organizationId(), entry.actorUserId(),
                    entry.action(), entry.targetType(), entry.targetId(), entry.payloadSummary(), entry.createdAt());
        }
    }
}
