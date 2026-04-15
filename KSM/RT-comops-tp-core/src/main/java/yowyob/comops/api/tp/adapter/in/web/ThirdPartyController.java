package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.DeleteThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyLifecycleUseCase;
import yowyob.comops.api.tp.application.port.in.SearchThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/third-parties")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
public class ThirdPartyController {

        private final CreateThirdPartyUseCase createThirdPartyUseCase;
        private final GetThirdPartyUseCase getThirdPartyUseCase;
        private final ListThirdPartiesUseCase listThirdPartiesUseCase;
        private final SearchThirdPartiesUseCase searchThirdPartiesUseCase;
        private final UpdateThirdPartyUseCase updateThirdPartyUseCase;
        private final DeleteThirdPartyUseCase deleteThirdPartyUseCase;
        private final ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase;

        public ThirdPartyController(CreateThirdPartyUseCase createThirdPartyUseCase,
                        GetThirdPartyUseCase getThirdPartyUseCase,
                        ListThirdPartiesUseCase listThirdPartiesUseCase,
                        SearchThirdPartiesUseCase searchThirdPartiesUseCase,
                        UpdateThirdPartyUseCase updateThirdPartyUseCase,
                        DeleteThirdPartyUseCase deleteThirdPartyUseCase,
                        ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase) {
                this.createThirdPartyUseCase = createThirdPartyUseCase;
                this.getThirdPartyUseCase = getThirdPartyUseCase;
                this.listThirdPartiesUseCase = listThirdPartiesUseCase;
                this.searchThirdPartiesUseCase = searchThirdPartiesUseCase;
                this.updateThirdPartyUseCase = updateThirdPartyUseCase;
                this.deleteThirdPartyUseCase = deleteThirdPartyUseCase;
                this.manageThirdPartyLifecycleUseCase = manageThirdPartyLifecycleUseCase;
        }

        @PostMapping
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> createThirdParty(
                        @Valid @RequestBody Mono<CreateThirdPartyRequest> requestMono) {
                return requestMono
                                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                                .flatMap(tuple -> createThirdPartyUseCase.createThirdParty(new CreateThirdPartyCommand(
                                                tuple.getT2().tenantId(),
                                                tuple.getT1().organizationId(),
                                                tuple.getT1().partyType(),
                                                tuple.getT1().partyId(),
                                                tuple.getT1().referenceCode(),
                                                tuple.getT1().displayName(),
                                                tuple.getT1().roles(),
                                                tuple.getT1().prospect(),
                                                null,
                                                tuple.getT1().segment(),
                                                tuple.getT1().qualificationScore(),
                                                tuple.getT1().active() == null || tuple.getT1().active(),
                                                tuple.getT1().type(),
                                                tuple.getT1().legalForm(),
                                                tuple.getT1().uniqueIdentificationNumber(),
                                                tuple.getT1().tradeRegistrationNumber(),
                                                tuple.getT1().acronym(),
                                                tuple.getT1().longName(),
                                                tuple.getT1().logoUri(),
                                                tuple.getT1().logoId(),
                                                null,
                                                tuple.getT1().authorizedPaymentMethods(),
                                                tuple.getT1().authorizedCreditLimit(),
                                                tuple.getT1().maxDiscountRate(),
                                                tuple.getT1().vatSubject(),
                                                tuple.getT1().operationsBalance(),
                                                tuple.getT1().openingBalance(),
                                                tuple.getT1().payTermNumber(),
                                                tuple.getT1().payTermType(),
                                                tuple.getT1().thirdPartyFamily(),
                                                tuple.getT1().classification(),
                                                tuple.getT1().taxNumber())))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                                                .body(ApiResponse.success(response, "Third party created.")));
        }

        @GetMapping("/{thirdPartyId}")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> getThirdParty(
                        @PathVariable("thirdPartyId") UUID thirdPartyId) {
                return getThirdPartyUseCase.getThirdParty(thirdPartyId)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, "Third party retrieved.")));
        }

        @GetMapping
        public Mono<ResponseEntity<ApiResponse<List<ThirdPartyResponse>>>> listThirdParties(
                        @RequestParam("organizationId") UUID organizationId,
                        @RequestParam(value = "role", required = false) String role,
                        @RequestParam(value = "prospect", required = false) Boolean prospect) {
                return listThirdPartiesUseCase.listThirdParties(organizationId, role, prospect)
                                .map(ThirdPartyResponse::from)
                                .collectList()
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, "Third parties retrieved.")));
        }

        @GetMapping("/search")
        public Mono<ResponseEntity<ApiResponse<List<ThirdPartySearchResponse>>>> searchThirdParties(
                        @RequestParam("organizationId") UUID organizationId,
                        @RequestParam("q") String query,
                        @RequestParam(value = "role", required = false) String role,
                        @RequestParam(value = "prospect", required = false) Boolean prospect,
                        @RequestParam(value = "segment", required = false) String segment,
                        @RequestParam(value = "minimumQualificationScore", required = false) Integer minimumQualificationScore,
                        @RequestParam(value = "active", required = false) Boolean active,
                        @RequestParam(value = "followUpStatus", required = false) String followUpStatus,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                return searchThirdPartiesUseCase.searchThirdParties(organizationId, query, role, prospect, segment,
                                minimumQualificationScore, active, followUpStatus, page, size)
                                .map(ThirdPartySearchResponse::from)
                                .collectList()
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                "Third party search results retrieved.")));
        }

        @PatchMapping("/{thirdPartyId}")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> updateThirdParty(
                        @PathVariable("thirdPartyId") UUID thirdPartyId,
                        @Valid @RequestBody Mono<UpdateThirdPartyRequest> requestMono) {
                return requestMono
                                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                                .flatMap(tuple -> updateThirdPartyUseCase.updateThirdParty(new UpdateThirdPartyCommand(
                                                tuple.getT2().tenantId(),
                                                thirdPartyId,
                                                tuple.getT1().referenceCode(),
                                                tuple.getT1().displayName(),
                                                tuple.getT1().roles(),
                                                tuple.getT1().prospect(),
                                                tuple.getT1().accountingAccount(),
                                                tuple.getT1().segment(),
                                                tuple.getT1().qualificationScore(),
                                                tuple.getT1().active() == null || tuple.getT1().active(),
                                                tuple.getT1().type(),
                                                tuple.getT1().legalForm(),
                                                tuple.getT1().uniqueIdentificationNumber(),
                                                tuple.getT1().tradeRegistrationNumber(),
                                                tuple.getT1().acronym(),
                                                tuple.getT1().longName(),
                                                tuple.getT1().logoUri(),
                                                tuple.getT1().logoId(),
                                                tuple.getT1().accountingAccountNumbers(),
                                                tuple.getT1().authorizedPaymentMethods(),
                                                tuple.getT1().authorizedCreditLimit(),
                                                tuple.getT1().maxDiscountRate(),
                                                tuple.getT1().vatSubject(),
                                                tuple.getT1().operationsBalance(),
                                                tuple.getT1().openingBalance(),
                                                tuple.getT1().payTermNumber(),
                                                tuple.getT1().payTermType(),
                                                tuple.getT1().thirdPartyFamily(),
                                                tuple.getT1().classification(),
                                                tuple.getT1().taxNumber())))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, "Third party updated.")));
        }

        @PatchMapping("/{thirdPartyId}/qualification")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> qualifyThirdParty(
                        @PathVariable("thirdPartyId") UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyQualificationRequest> requestMono) {
                return requestMono
                                .flatMap(request -> manageThirdPartyLifecycleUseCase.qualifyThirdParty(thirdPartyId,
                                                request.segment(), request.qualificationScore()))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                "Third party qualification updated.")));
        }

        @PostMapping("/{thirdPartyId}/score/recompute")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> recomputeQualificationScore(
                        @PathVariable("thirdPartyId") UUID thirdPartyId) {
                return manageThirdPartyLifecycleUseCase.recomputeQualificationScore(thirdPartyId)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                "Third party qualification score recomputed.")));
        }

        @PatchMapping("/{thirdPartyId}/follow-up/schedule")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> scheduleFollowUp(
                        @PathVariable("thirdPartyId") UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyFollowUpRequest> requestMono) {
                return requestMono
                                .flatMap(request -> manageThirdPartyLifecycleUseCase.scheduleFollowUp(thirdPartyId,
                                                request.nextFollowUpAt()))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                "Third party follow-up scheduled.")));
        }

        @PatchMapping("/{thirdPartyId}/follow-up/complete")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> completeFollowUp(
                        @PathVariable("thirdPartyId") UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyFollowUpRequest> requestMono) {
                return requestMono
                                .flatMap(request -> manageThirdPartyLifecycleUseCase.completeFollowUp(thirdPartyId,
                                                request.contactedAt(), request.nextFollowUpAt()))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                "Third party follow-up completed.")));
        }

        @DeleteMapping("/{thirdPartyId}")
        public Mono<ResponseEntity<ApiResponse<Void>>> deleteThirdParty(
                        @PathVariable("thirdPartyId") UUID thirdPartyId) {
                return deleteThirdPartyUseCase.deleteThirdParty(thirdPartyId)
                                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Third party deleted.")));
        }
}
