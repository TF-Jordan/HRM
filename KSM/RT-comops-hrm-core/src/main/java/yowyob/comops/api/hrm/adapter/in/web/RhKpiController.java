package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateRhKpiSnapshotCommand;
import yowyob.comops.api.hrm.application.port.in.ManageRhKpiUseCase;
import yowyob.comops.api.hrm.domain.model.RhKpiSnapshot;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/kpi")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class RhKpiController {

    private final ManageRhKpiUseCase rhKpiUseCase;

    public RhKpiController(ManageRhKpiUseCase rhKpiUseCase) {
        this.rhKpiUseCase = rhKpiUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:kpi:create')")
    public Mono<ResponseEntity<ApiResponse<RhKpiSnapshotResponse>>> create(
            @Valid @RequestBody Mono<CreateRhKpiSnapshotRequest> requestMono) {
        return requestMono.map(CreateRhKpiSnapshotRequest::toCommand)
                .flatMap(rhKpiUseCase::createSnapshot)
                .map(RhKpiSnapshotResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "KPI snapshot created.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:kpi:read')")
    public Mono<ResponseEntity<ApiResponse<RhKpiSnapshotResponse>>> get(@PathVariable UUID id) {
        return rhKpiUseCase.getSnapshot(id).map(RhKpiSnapshotResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "KPI snapshot fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:kpi:read')")
    public Mono<ResponseEntity<ApiResponse<List<RhKpiSnapshotResponse>>>> list(@RequestParam UUID orgId) {
        return rhKpiUseCase.listSnapshots(orgId)
                .map(RhKpiSnapshotResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "KPI snapshots fetched.")));
    }

    public record CreateRhKpiSnapshotRequest(UUID organizationId, String periode, int effectifTotal,
            int effectifActif, BigDecimal tauxTurnover, BigDecimal tauxAbsenteisme,
            BigDecimal masseSalariale, BigDecimal couvertureCompetences) {
        CreateRhKpiSnapshotCommand toCommand() {
            return new CreateRhKpiSnapshotCommand(organizationId, periode, effectifTotal, effectifActif,
                    tauxTurnover, tauxAbsenteisme, masseSalariale, couvertureCompetences);
        }
    }

    public record RhKpiSnapshotResponse(UUID id, UUID organizationId, String periode, int effectifTotal,
            int effectifActif, BigDecimal tauxTurnover, BigDecimal tauxAbsenteisme,
            BigDecimal masseSalariale, BigDecimal couvertureCompetences) {
        static RhKpiSnapshotResponse from(RhKpiSnapshot s) {
            return new RhKpiSnapshotResponse(s.id(), s.organizationId(), s.periode(), s.effectifTotal(),
                    s.effectifActif(), s.tauxTurnover(), s.tauxAbsenteisme(),
                    s.masseSalariale(), s.couvertureCompetences());
        }
    }
}
