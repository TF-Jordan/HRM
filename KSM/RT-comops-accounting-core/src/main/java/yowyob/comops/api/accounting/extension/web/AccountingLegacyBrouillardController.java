package yowyob.comops.api.accounting.extension.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import yowyob.comops.api.accounting.extension.service.AccountingLegacyBrouillardService;
import yowyob.comops.api.common.domain.model.ApiResponse;

@RestController
@RequestMapping("/api/accounting/brouillards")
public class AccountingLegacyBrouillardController {

    private final AccountingLegacyBrouillardService brouillardService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingLegacyBrouillardController(AccountingLegacyBrouillardService brouillardService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.brouillardService = brouillardService;
        this.contextResolver = contextResolver;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<AccountingLegacyDtos.BrouillardComptableDto>>>> listBrouillards(
            ServerHttpRequest request,
            @RequestParam(name = "statut", required = false) AccountingLegacyDtos.BrouillardStatut statut,
            @RequestParam(name = "type", required = false) AccountingLegacyDtos.BrouillardType type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return contextResolver.resolve(request)
                .flatMap(context -> brouillardService.listBrouillards(statut, type, page, size, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Draft entries loaded successfully")));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.BrouillardComptableDto>>> getBrouillard(
            ServerHttpRequest request,
            @PathVariable UUID id) {
        return contextResolver.resolve(request)
                .flatMap(context -> brouillardService.getBrouillard(id, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Draft entry loaded successfully")));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.BrouillardComptableDto>>> uploadBrouillard(
            ServerHttpRequest request,
            @RequestPart("file") FilePart file) {
        return contextResolver.resolve(request)
                .flatMap(context -> brouillardService.uploadDraft(file, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Facture importée et brouillard créé avec succès.")));
    }

    @PostMapping("/{id}/validate")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.BrouillardComptableDto>>> validateBrouillard(
            ServerHttpRequest request,
            @PathVariable UUID id,
            @RequestBody(required = false) Mono<AccountingLegacyDtos.BrouillardValidationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono.defaultIfEmpty(new AccountingLegacyDtos.BrouillardValidationRequest(null, Boolean.FALSE)))
                .flatMap(tuple -> brouillardService.validateBrouillard(id, tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Draft validated successfully")));
    }

    @PostMapping("/{id}/reject")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.BrouillardComptableDto>>> rejectBrouillard(
            ServerHttpRequest request,
            @PathVariable UUID id,
            @Valid @RequestBody Mono<AccountingLegacyDtos.BrouillardRejectionRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> brouillardService.rejectBrouillard(id, tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Draft rejected")));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBrouillard(ServerHttpRequest request, @PathVariable UUID id) {
        return contextResolver.resolve(request)
                .flatMap(context -> brouillardService.deleteBrouillard(id, context))
                .thenReturn(ResponseEntity.noContent().build());
    }
}
