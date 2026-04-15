package yowyob.comops.api.tp.adapter.in.web;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileCommand;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileUseCase;

@RestController
@RequestMapping("/api/third-parties/actors")
public class ActorFinancialProfileController {

    private final EnsureActorFinancialProfileUseCase ensureActorFinancialProfileUseCase;

    public ActorFinancialProfileController(EnsureActorFinancialProfileUseCase ensureActorFinancialProfileUseCase) {
        this.ensureActorFinancialProfileUseCase = ensureActorFinancialProfileUseCase;
    }

    @PostMapping("/{actorId}/financial-profile")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
    public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> ensureFinancialProfile(
            @PathVariable UUID actorId,
            @Valid @RequestBody Mono<EnsureActorFinancialProfileRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureActorFinancialProfileUseCase.ensureActorFinancialProfile(
                        new EnsureActorFinancialProfileCommand(
                                tuple.getT2().tenantId(),
                                tuple.getT1().organizationId(),
                                actorId,
                                tuple.getT1().role(),
                                tuple.getT1().referenceCode(),
                                tuple.getT1().displayName())))
                .map(ThirdPartyResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Actor financial profile ensured.")));
    }
}
