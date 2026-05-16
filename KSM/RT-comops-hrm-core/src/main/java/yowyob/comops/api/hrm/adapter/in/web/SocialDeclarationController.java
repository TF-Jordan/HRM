package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateSocialDeclarationCommand;
import yowyob.comops.api.hrm.application.port.in.ManageSocialDeclarationUseCase;
import yowyob.comops.api.hrm.domain.model.SocialDeclaration;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/declarations")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class SocialDeclarationController {

    private final ManageSocialDeclarationUseCase socialDeclarationUseCase;

    public SocialDeclarationController(ManageSocialDeclarationUseCase socialDeclarationUseCase) {
        this.socialDeclarationUseCase = socialDeclarationUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:declaration:create')")
    public Mono<ResponseEntity<ApiResponse<SocialDeclarationResponse>>> create(
            @Valid @RequestBody Mono<CreateSocialDeclarationRequest> requestMono) {
        return requestMono.map(CreateSocialDeclarationRequest::toCommand)
                .flatMap(socialDeclarationUseCase::create)
                .map(SocialDeclarationResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Declaration created.")));
    }

    @PutMapping("/{id}/generate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:declaration:manage')")
    public Mono<ResponseEntity<ApiResponse<SocialDeclarationResponse>>> generate(
            @PathVariable UUID id, @Valid @RequestBody Mono<GenerateRequest> requestMono) {
        return requestMono.flatMap(req -> socialDeclarationUseCase.generate(id, req.fichierId()))
                .map(SocialDeclarationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Declaration generated.")));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:declaration:manage')")
    public Mono<ResponseEntity<ApiResponse<SocialDeclarationResponse>>> submit(@PathVariable UUID id) {
        return socialDeclarationUseCase.submit(id).map(SocialDeclarationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Declaration submitted.")));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:declaration:manage')")
    public Mono<ResponseEntity<ApiResponse<SocialDeclarationResponse>>> acknowledge(@PathVariable UUID id) {
        return socialDeclarationUseCase.acknowledge(id).map(SocialDeclarationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Declaration acknowledged.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:declaration:read')")
    public Mono<ResponseEntity<ApiResponse<SocialDeclarationResponse>>> get(@PathVariable UUID id) {
        return socialDeclarationUseCase.get(id).map(SocialDeclarationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Declaration fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:declaration:read')")
    public Mono<ResponseEntity<ApiResponse<List<SocialDeclarationResponse>>>> list(@RequestParam UUID orgId) {
        return socialDeclarationUseCase.listByOrganization(orgId)
                .map(SocialDeclarationResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Declarations fetched.")));
    }

    public record CreateSocialDeclarationRequest(UUID organizationId, String type, String periode, String format) {
        CreateSocialDeclarationCommand toCommand() {
            return new CreateSocialDeclarationCommand(organizationId, type, periode, format);
        }
    }

    public record GenerateRequest(UUID fichierId) {}

    public record SocialDeclarationResponse(UUID id, UUID organizationId, String type, String periode,
            String format, String statut, UUID fichierId, Instant generatedAt, Instant submittedAt) {
        static SocialDeclarationResponse from(SocialDeclaration d) {
            return new SocialDeclarationResponse(d.id(), d.organizationId(), d.type().name(), d.periode(),
                    d.format(), d.statut().name(), d.fichierId(), d.generatedAt(), d.submittedAt());
        }
    }
}
