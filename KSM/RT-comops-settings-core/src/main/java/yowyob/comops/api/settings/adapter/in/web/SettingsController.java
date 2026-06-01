package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.settings.application.port.in.ListDocumentSequencesUseCase;
import yowyob.comops.api.settings.application.port.in.UpsertDocumentSequenceCommand;
import yowyob.comops.api.settings.application.port.in.UpsertDocumentSequenceUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/settings/document-sequences")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:write')")
public class SettingsController {

    private final UpsertDocumentSequenceUseCase upsertDocumentSequenceUseCase;
    private final ListDocumentSequencesUseCase listDocumentSequencesUseCase;

    public SettingsController(UpsertDocumentSequenceUseCase upsertDocumentSequenceUseCase,
            ListDocumentSequencesUseCase listDocumentSequencesUseCase) {
        this.upsertDocumentSequenceUseCase = upsertDocumentSequenceUseCase;
        this.listDocumentSequencesUseCase = listDocumentSequencesUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<DocumentSequenceResponse>>> upsertSequence(
            @Valid @RequestBody Mono<UpsertDocumentSequenceRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> upsertDocumentSequenceUseCase.upsert(new UpsertDocumentSequenceCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(),
                        tuple.getT1().documentType(),
                        tuple.getT1().prefix(),
                        tuple.getT1().suffix(),
                        tuple.getT1().paddingWidth(),
                        tuple.getT1().nextNumber())))
                .map(DocumentSequenceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Document sequence saved.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<DocumentSequenceResponse>>>> listSequences(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(value = "agencyId", required = false) UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listDocumentSequencesUseCase.list(context.tenantId(), organizationId, agencyId)
                        .map(DocumentSequenceResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Document sequences retrieved.")));
    }
}
