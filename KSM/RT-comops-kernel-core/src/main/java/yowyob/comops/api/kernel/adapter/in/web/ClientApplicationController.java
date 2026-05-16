package yowyob.comops.api.kernel.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.port.in.ListClientApplicationsUseCase;
import yowyob.comops.api.kernel.application.port.in.RegisterClientApplicationCommand;
import yowyob.comops.api.kernel.application.port.in.RegisterClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.in.RevokeClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.in.RotateClientApplicationSecretCommand;
import yowyob.comops.api.kernel.application.port.in.RotateClientApplicationSecretUseCase;
import yowyob.comops.api.kernel.application.port.in.UpdateClientApplicationCommand;
import yowyob.comops.api.kernel.application.port.in.UpdateClientApplicationUseCase;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/client-applications")
public class ClientApplicationController {

    private final ListClientApplicationsUseCase listClientApplicationsUseCase;
    private final RegisterClientApplicationUseCase registerClientApplicationUseCase;
    private final RotateClientApplicationSecretUseCase rotateClientApplicationSecretUseCase;
    private final RevokeClientApplicationUseCase revokeClientApplicationUseCase;
    private final UpdateClientApplicationUseCase updateClientApplicationUseCase;

    public ClientApplicationController(
            ListClientApplicationsUseCase listClientApplicationsUseCase,
            RegisterClientApplicationUseCase registerClientApplicationUseCase,
            RotateClientApplicationSecretUseCase rotateClientApplicationSecretUseCase,
            RevokeClientApplicationUseCase revokeClientApplicationUseCase,
            UpdateClientApplicationUseCase updateClientApplicationUseCase) {
        this.listClientApplicationsUseCase = listClientApplicationsUseCase;
        this.registerClientApplicationUseCase = registerClientApplicationUseCase;
        this.rotateClientApplicationSecretUseCase = rotateClientApplicationSecretUseCase;
        this.revokeClientApplicationUseCase = revokeClientApplicationUseCase;
        this.updateClientApplicationUseCase = updateClientApplicationUseCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.canManageClientApplications(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<ClientApplicationResponse>>>> list() {
        return listClientApplicationsUseCase.list()
                .map(ClientApplicationResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Client applications listed.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.canManageClientApplications(authentication)")
    public Mono<ResponseEntity<ApiResponse<ProvisionedClientApplicationResponse>>> create(
            @RequestBody Mono<CreateClientApplicationRequest> requestMono) {
        return requestMono
                .flatMap(request -> registerClientApplicationUseCase.register(new RegisterClientApplicationCommand(
                        request.clientId(), request.name(), request.description(), request.clientSecret(),
                        request.allowedServices(), false)))
                .map(ProvisionedClientApplicationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Client application created.")));
    }

    @PatchMapping("/{clientApplicationId}")
    @PreAuthorize("@businessAccessPolicy.canManageClientApplications(authentication)")
    public Mono<ResponseEntity<ApiResponse<ClientApplicationResponse>>> update(
            @PathVariable UUID clientApplicationId,
            @RequestBody Mono<UpdateClientApplicationRequest> requestMono) {
        return requestMono
                .flatMap(request -> updateClientApplicationUseCase.update(new UpdateClientApplicationCommand(
                        clientApplicationId,
                        request.name(),
                        request.description(),
                        request.allowedServices())))
                .map(ClientApplicationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Client application updated.")));
    }

    @PostMapping("/{clientApplicationId}/rotate-secret")
    @PreAuthorize("@businessAccessPolicy.canManageClientApplications(authentication)")
    public Mono<ResponseEntity<ApiResponse<ProvisionedClientApplicationResponse>>> rotateSecret(
            @PathVariable UUID clientApplicationId,
            @RequestBody(required = false) Mono<RotateClientApplicationSecretRequest> requestMono) {
        return requestMono.defaultIfEmpty(new RotateClientApplicationSecretRequest(null))
                .flatMap(request -> rotateClientApplicationSecretUseCase.rotateSecret(
                        new RotateClientApplicationSecretCommand(clientApplicationId, request.clientSecret())))
                .map(ProvisionedClientApplicationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Client application secret rotated.")));
    }

    @PostMapping("/{clientApplicationId}/revoke")
    @PreAuthorize("@businessAccessPolicy.canManageClientApplications(authentication)")
    public Mono<ResponseEntity<ApiResponse<ClientApplicationResponse>>> revoke(@PathVariable UUID clientApplicationId) {
        return revokeClientApplicationUseCase.revoke(clientApplicationId)
                .map(ClientApplicationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Client application revoked.")));
    }
}
