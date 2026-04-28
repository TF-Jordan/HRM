package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateMissionOrderCommand;
import yowyob.comops.api.hrm.application.port.in.ManageMissionOrderUseCase;
import yowyob.comops.api.hrm.domain.model.MissionOrder;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/hrm/mission-orders")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class MissionOrderController {

    private final ManageMissionOrderUseCase missionOrderUseCase;

    public MissionOrderController(ManageMissionOrderUseCase missionOrderUseCase) {
        this.missionOrderUseCase = missionOrderUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:create')")
    public Mono<ResponseEntity<ApiResponse<MissionOrderResponse>>> createMissionOrder(
            @Valid @RequestBody Mono<CreateMissionOrderRequest> requestMono) {
        return requestMono.map(CreateMissionOrderRequest::toCommand)
                .flatMap(missionOrderUseCase::createMissionOrder)
                .map(MissionOrderResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Mission order created.")));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:manage')")
    public Mono<ResponseEntity<ApiResponse<MissionOrderResponse>>> approveMissionOrder(@PathVariable UUID id) {
        return missionOrderUseCase.approveMissionOrder(id).map(MissionOrderResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Mission order approved.")));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:manage')")
    public Mono<ResponseEntity<ApiResponse<MissionOrderResponse>>> startMissionOrder(@PathVariable UUID id) {
        return missionOrderUseCase.startMissionOrder(id).map(MissionOrderResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Mission order started.")));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:manage')")
    public Mono<ResponseEntity<ApiResponse<MissionOrderResponse>>> completeMissionOrder(@PathVariable UUID id) {
        return missionOrderUseCase.completeMissionOrder(id).map(MissionOrderResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Mission order completed.")));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:manage')")
    public Mono<ResponseEntity<ApiResponse<MissionOrderResponse>>> cancelMissionOrder(@PathVariable UUID id) {
        return missionOrderUseCase.cancelMissionOrder(id).map(MissionOrderResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Mission order cancelled.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:read')")
    public Mono<ResponseEntity<ApiResponse<MissionOrderResponse>>> getMissionOrder(@PathVariable UUID id) {
        return missionOrderUseCase.getMissionOrder(id).map(MissionOrderResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Mission order fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:mission:read')")
    public Mono<ResponseEntity<ApiResponse<List<MissionOrderResponse>>>> listMissionOrders(@RequestParam UUID employeeId) {
        return missionOrderUseCase.listMissionOrdersByEmployee(employeeId)
                .map(MissionOrderResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Mission orders fetched.")));
    }

    public record CreateMissionOrderRequest(UUID employeeId, String destination, String objet,
            LocalDate dateDebut, LocalDate dateFin, BigDecimal montantAvance, String centreCout) {
        CreateMissionOrderCommand toCommand() {
            return new CreateMissionOrderCommand(employeeId, destination, objet, dateDebut,
                    dateFin, montantAvance, centreCout);
        }
    }

    public record MissionOrderResponse(UUID id, UUID employeeId, String destination, String objet,
            LocalDate dateDebut, LocalDate dateFin, BigDecimal montantAvance, String centreCout, String status) {
        static MissionOrderResponse from(MissionOrder o) {
            return new MissionOrderResponse(o.id(), o.employeeId(), o.destination(), o.objet(),
                    o.dateDebut(), o.dateFin(), o.montantAvance(), o.centreCout(), o.status().name());
        }
    }
}
