package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.tp.application.port.in.AddThirdPartyBankAccountCommand;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.DeleteThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyStatisticsUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.LookupThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyBankAccountsUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyLifecycleUseCase;
import yowyob.comops.api.tp.application.port.in.SearchThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyUseCase;
import yowyob.comops.api.tp.domain.ThirdPartyNotFoundException;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

public abstract class CommercialThirdPartyController {

        private final CreateThirdPartyUseCase createThirdPartyUseCase;
        private final GetThirdPartyUseCase getThirdPartyUseCase;
        private final ListThirdPartiesUseCase listThirdPartiesUseCase;
        private final SearchThirdPartiesUseCase searchThirdPartiesUseCase;
        private final UpdateThirdPartyUseCase updateThirdPartyUseCase;
        private final DeleteThirdPartyUseCase deleteThirdPartyUseCase;
        private final ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase;
        private final ManageThirdPartyBankAccountsUseCase manageThirdPartyBankAccountsUseCase;
        private final LookupThirdPartyUseCase lookupThirdPartyUseCase;
        private final GetThirdPartyStatisticsUseCase getThirdPartyStatisticsUseCase;
        private final String role;
        private final String singularLabel;
        private final boolean forcedProspect;

        protected CommercialThirdPartyController(CreateThirdPartyUseCase createThirdPartyUseCase,
                        GetThirdPartyUseCase getThirdPartyUseCase,
                        ListThirdPartiesUseCase listThirdPartiesUseCase,
                        SearchThirdPartiesUseCase searchThirdPartiesUseCase,
                        UpdateThirdPartyUseCase updateThirdPartyUseCase,
                        DeleteThirdPartyUseCase deleteThirdPartyUseCase,
                        ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase,
                        ManageThirdPartyBankAccountsUseCase manageThirdPartyBankAccountsUseCase,
                        LookupThirdPartyUseCase lookupThirdPartyUseCase,
                        GetThirdPartyStatisticsUseCase getThirdPartyStatisticsUseCase,
                        String role,
                        String singularLabel,
                        boolean forcedProspect) {
                this.createThirdPartyUseCase = createThirdPartyUseCase;
                this.getThirdPartyUseCase = getThirdPartyUseCase;
                this.listThirdPartiesUseCase = listThirdPartiesUseCase;
                this.searchThirdPartiesUseCase = searchThirdPartiesUseCase;
                this.updateThirdPartyUseCase = updateThirdPartyUseCase;
                this.deleteThirdPartyUseCase = deleteThirdPartyUseCase;
                this.manageThirdPartyLifecycleUseCase = manageThirdPartyLifecycleUseCase;
                this.manageThirdPartyBankAccountsUseCase = manageThirdPartyBankAccountsUseCase;
                this.lookupThirdPartyUseCase = lookupThirdPartyUseCase;
                this.getThirdPartyStatisticsUseCase = getThirdPartyStatisticsUseCase;
                this.role = role;
                this.singularLabel = singularLabel;
                this.forcedProspect = forcedProspect;
        }

        @GetMapping
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<List<ThirdPartyResponse>>>> listByOrganization(
                        @RequestParam("organizationId") UUID organizationId,
                        @RequestParam(value = "prospect", required = false) Boolean prospect) {
                return listThirdPartiesUseCase.listThirdParties(organizationId, role, resolveProspect(prospect))
                                .map(ThirdPartyResponse::from)
                                .collectList()
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, singularLabel + " list retrieved.")));
        }

        @GetMapping("/search")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<List<ThirdPartySearchResponse>>>> searchByOrganization(
                        @RequestParam("organizationId") UUID organizationId,
                        @RequestParam("q") String query,
                        @RequestParam(value = "segment", required = false) String segment,
                        @RequestParam(value = "minimumQualificationScore", required = false) Integer minimumQualificationScore,
                        @RequestParam(value = "active", required = false) Boolean active,
                        @RequestParam(value = "followUpStatus", required = false) String followUpStatus,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                return searchThirdPartiesUseCase.searchThirdParties(organizationId, query, role,
                                forcedProspect ? Boolean.TRUE : null, segment, minimumQualificationScore, active,
                                followUpStatus, page, size)
                                .map(ThirdPartySearchResponse::from)
                                .collectList()
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " search results retrieved.")));
        }

        @GetMapping("/{thirdPartyId}")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> getById(@PathVariable UUID thirdPartyId) {
                return getThirdPartyUseCase.getThirdParty(thirdPartyId)
                                .filter(thirdParty -> thirdParty.hasRole(role))
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(thirdPartyId)))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, singularLabel + " retrieved.")));
        }

        @PostMapping
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> create(
                        @Valid @RequestBody Mono<CreateCommercialThirdPartyRequest> requestMono) {
                return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                                .flatMap(tuple -> createThirdPartyUseCase.createThirdParty(new CreateThirdPartyCommand(
                                                tuple.getT2().tenantId(),
                                                tuple.getT1().organizationId(),
                                                tuple.getT1().partyType(),
                                                tuple.getT1().partyId(),
                                                tuple.getT1().referenceCode(),
                                                tuple.getT1().displayName(),
                                                Set.of(role),
                                                resolveProspect(tuple.getT1().prospect()),
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
                                                .body(ApiResponse.success(response, singularLabel + " created.")));
        }

        @PatchMapping("/{thirdPartyId}")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> update(@PathVariable UUID thirdPartyId,
                        @Valid @RequestBody Mono<UpdateThirdPartyRequest> requestMono) {
                return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                                .flatMap(tuple -> updateThirdPartyUseCase.updateThirdParty(new UpdateThirdPartyCommand(
                                                tuple.getT2().tenantId(),
                                                thirdPartyId,
                                                tuple.getT1().referenceCode(),
                                                tuple.getT1().displayName(),
                                                normalizeRoles(tuple.getT1().roles()),
                                                resolveProspect(tuple.getT1().prospect()),
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
                                .filter(thirdParty -> thirdParty.hasRole(role))
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(thirdPartyId)))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, singularLabel + " updated.")));
        }

        @DeleteMapping("/{thirdPartyId}")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable UUID thirdPartyId) {
                return getThirdPartyUseCase.getThirdParty(thirdPartyId)
                                .filter(thirdParty -> thirdParty.hasRole(role))
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(thirdPartyId)))
                                .flatMap(ignored -> deleteThirdPartyUseCase.deleteThirdParty(thirdPartyId))
                                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, singularLabel + " deleted.")));
        }

        @GetMapping("/by-bank-account/{bankAccountNumber}")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> findByBankAccount(
                        @RequestParam("organizationId") UUID organizationId,
                        @PathVariable String bankAccountNumber) {
                return lookupThirdPartyUseCase.findByBankAccountNumber(organizationId, bankAccountNumber, role,
                                forcedProspect ? Boolean.TRUE : null)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " found by bank account.")));
        }

        @GetMapping("/by-accounting-account/{accountingAccount}")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> findByAccountingAccount(
                        @RequestParam("organizationId") UUID organizationId,
                        @PathVariable String accountingAccount) {
                return lookupThirdPartyUseCase.findByAccountingAccount(organizationId, accountingAccount, role,
                                forcedProspect ? Boolean.TRUE : null)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " found by accounting account.")));
        }

        @PatchMapping("/{thirdPartyId}/bank-account")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> defineBankAccount(@PathVariable UUID thirdPartyId,
                        @RequestBody Mono<String> requestMono) {
                return requestMono.map(this::normalizeRawBody)
                                .flatMap(value -> manageThirdPartyBankAccountsUseCase
                                                .definePrimaryBankAccount(thirdPartyId, value))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " bank account updated.")));
        }

        @PatchMapping("/{thirdPartyId}/accounting-account")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> defineAccountingAccount(
                        @PathVariable UUID thirdPartyId,
                        @RequestBody Mono<String> requestMono) {
                return requestMono.map(this::normalizeRawBody)
                                .flatMap(value -> manageThirdPartyLifecycleUseCase.defineAccountingAccount(thirdPartyId,
                                                value))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " accounting account updated.")));
        }

        @PatchMapping("/{thirdPartyId}/qualification")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> qualify(@PathVariable UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyQualificationRequest> requestMono) {
                return requestMono
                                .flatMap(request -> manageThirdPartyLifecycleUseCase.qualifyThirdParty(thirdPartyId,
                                                request.segment(), request.qualificationScore()))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " qualification updated.")));
        }

        @PostMapping("/{thirdPartyId}/score/recompute")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> recomputeScore(@PathVariable UUID thirdPartyId) {
                return manageThirdPartyLifecycleUseCase.recomputeQualificationScore(thirdPartyId)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " qualification score recomputed.")));
        }

        @PatchMapping("/{thirdPartyId}/follow-up/schedule")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> scheduleFollowUp(@PathVariable UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyFollowUpRequest> requestMono) {
                return requestMono
                                .flatMap(request -> manageThirdPartyLifecycleUseCase.scheduleFollowUp(thirdPartyId,
                                                request.nextFollowUpAt()))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " follow-up scheduled.")));
        }

        @PatchMapping("/{thirdPartyId}/follow-up/complete")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> completeFollowUp(@PathVariable UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyFollowUpRequest> requestMono) {
                return requestMono
                                .flatMap(request -> manageThirdPartyLifecycleUseCase.completeFollowUp(thirdPartyId,
                                                request.contactedAt(), request.nextFollowUpAt()))
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " follow-up completed.")));
        }

        @PatchMapping("/{thirdPartyId}/activate")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> activate(@PathVariable UUID thirdPartyId) {
                return manageThirdPartyLifecycleUseCase.activateThirdParty(thirdPartyId)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, singularLabel + " activated.")));
        }

        @PatchMapping("/{thirdPartyId}/deactivate")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> deactivate(@PathVariable UUID thirdPartyId) {
                return manageThirdPartyLifecycleUseCase.deactivateThirdParty(thirdPartyId)
                                .map(ThirdPartyResponse::from)
                                .map(response -> ResponseEntity
                                                .ok(ApiResponse.success(response, singularLabel + " deactivated.")));
        }

        @GetMapping("/statistics")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyStatisticsResponse>>> statistics(
                        @RequestParam("organizationId") UUID organizationId) {
                return getThirdPartyStatisticsUseCase
                                .getStatistics(organizationId, role, forcedProspect ? Boolean.TRUE : null)
                                .map(ThirdPartyStatisticsResponse::from)
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " statistics retrieved.")));
        }

        @GetMapping("/{thirdPartyId}/bank-accounts")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<List<ThirdPartyBankAccountResponse>>>> listBankAccounts(
                        @PathVariable UUID thirdPartyId) {
                return manageThirdPartyBankAccountsUseCase.listBankAccounts(thirdPartyId)
                                .map(ThirdPartyBankAccountResponse::from)
                                .collectList()
                                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                                                singularLabel + " bank accounts retrieved.")));
        }

        @PostMapping("/{thirdPartyId}/bank-accounts")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<ThirdPartyBankAccountResponse>>> addBankAccount(
                        @PathVariable UUID thirdPartyId,
                        @Valid @RequestBody Mono<ThirdPartyBankAccountRequest> requestMono) {
                return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                                .flatMap(tuple -> manageThirdPartyBankAccountsUseCase
                                                .addBankAccount(new AddThirdPartyBankAccountCommand(
                                                                tuple.getT2().tenantId(), thirdPartyId,
                                                                tuple.getT1().label(), tuple.getT1().bankName(),
                                                                tuple.getT1().iban(), tuple.getT1().swiftBic(),
                                                                tuple.getT1().currency(),
                                                                tuple.getT1().primary())))
                                .map(ThirdPartyBankAccountResponse::from)
                                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                                                .body(ApiResponse.success(response,
                                                                singularLabel + " bank account added.")));
        }

        @DeleteMapping("/{thirdPartyId}/bank-accounts/{bankAccountId}")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<Void>>> deleteBankAccount(@PathVariable UUID thirdPartyId,
                        @PathVariable UUID bankAccountId) {
                return manageThirdPartyBankAccountsUseCase.removeBankAccount(thirdPartyId, bankAccountId)
                                .thenReturn(ResponseEntity.ok(
                                                ApiResponse.success(null, singularLabel + " bank account deleted.")));
        }

        @PatchMapping("/{thirdPartyId}/bank-accounts/{bankAccountId}/set-primary")
        @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
        public Mono<ResponseEntity<ApiResponse<Void>>> setPrimaryBankAccount(@PathVariable UUID thirdPartyId,
                        @PathVariable UUID bankAccountId) {
                return manageThirdPartyBankAccountsUseCase.setPrimaryBankAccount(thirdPartyId, bankAccountId)
                                .thenReturn(ResponseEntity.ok(ApiResponse.success(null,
                                                singularLabel + " primary bank account updated.")));
        }

        protected boolean resolveProspect(Boolean value) {
                return forcedProspect || Boolean.TRUE.equals(value);
        }

        private Set<String> normalizeRoles(Set<String> roles) {
                LinkedHashSet<String> normalized = new LinkedHashSet<>();
                if (roles != null) {
                        normalized.addAll(roles);
                }
                normalized.add(role);
                if (forcedProspect) {
                        normalized.add("PROSPECT");
                }
                return Set.copyOf(normalized);
        }

        private String normalizeRawBody(String rawBody) {
                if (rawBody == null) {
                        return null;
                }
                String trimmed = rawBody.trim();
                if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                        return trimmed.substring(1, trimmed.length() - 1).trim();
                }
                return trimmed;
        }
}
