package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.adapter.in.web.AddressResponse;
import yowyob.comops.api.common.adapter.in.web.ContactResponse;
import yowyob.comops.api.common.application.port.in.CreateAddressCommand;
import yowyob.comops.api.common.application.port.in.CreateAddressUseCase;
import yowyob.comops.api.common.application.port.in.CreateContactCommand;
import yowyob.comops.api.common.application.port.in.CreateContactUseCase;
import yowyob.comops.api.common.application.port.in.DeleteAddressUseCase;
import yowyob.comops.api.common.application.port.in.DeleteContactUseCase;
import yowyob.comops.api.common.application.port.in.ListAddressesUseCase;
import yowyob.comops.api.common.application.port.in.ListContactsUseCase;
import yowyob.comops.api.common.domain.model.AddressType;
import yowyob.comops.api.common.domain.model.AddressableType;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.common.domain.model.ContactableType;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class OrganizationAddressBookController {

    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final CreateAddressUseCase createAddressUseCase;
    private final ListAddressesUseCase listAddressesUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final CreateContactUseCase createContactUseCase;
    private final ListContactsUseCase listContactsUseCase;
    private final DeleteContactUseCase deleteContactUseCase;

    public OrganizationAddressBookController(OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository,
            CreateAddressUseCase createAddressUseCase,
            ListAddressesUseCase listAddressesUseCase,
            DeleteAddressUseCase deleteAddressUseCase,
            CreateContactUseCase createContactUseCase,
            ListContactsUseCase listContactsUseCase,
            DeleteContactUseCase deleteContactUseCase) {
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.createAddressUseCase = createAddressUseCase;
        this.listAddressesUseCase = listAddressesUseCase;
        this.deleteAddressUseCase = deleteAddressUseCase;
        this.createContactUseCase = createContactUseCase;
        this.listContactsUseCase = listContactsUseCase;
        this.deleteContactUseCase = deleteContactUseCase;
    }

    @PostMapping("/organizations/{organizationId}/addresses")
    public Mono<ResponseEntity<ApiResponse<AddressResponse>>> createOrganizationAddress(@PathVariable UUID organizationId,
            @Valid @RequestBody Mono<NestedAddressRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureOrganizationExists(tuple.getT2().tenantId(), organizationId)
                        .then(createAddressUseCase.createAddress(new CreateAddressCommand(
                                tuple.getT2().tenantId(), AddressableType.ORGANIZATION, organizationId,
                                tuple.getT1().type(), tuple.getT1().addressLine1(), tuple.getT1().addressLine2(),
                                tuple.getT1().city(), tuple.getT1().state(), tuple.getT1().locality(),
                                tuple.getT1().countryId(), tuple.getT1().zipCode(), tuple.getT1().postalCode(),
                                tuple.getT1().poBox(), tuple.getT1().isDefault(), tuple.getT1().neighborhood(),
                                tuple.getT1().informalDescription(), tuple.getT1().latitude(),
                                tuple.getT1().longitude()))))
                .map(AddressResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Organization address created.")));
    }

    @GetMapping("/organizations/{organizationId}/addresses")
    public Mono<ResponseEntity<ApiResponse<List<AddressResponse>>>> listOrganizationAddresses(@PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureOrganizationExists(context.tenantId(), organizationId)
                        .then(listAddressesUseCase.listAddresses(context.tenantId(), AddressableType.ORGANIZATION,
                                organizationId).map(AddressResponse::from).collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization addresses retrieved.")));
    }

    @DeleteMapping("/organizations/{organizationId}/addresses/{addressId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteOrganizationAddress(@PathVariable UUID organizationId,
            @PathVariable UUID addressId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureOrganizationExists(context.tenantId(), organizationId)
                        .then(deleteAddressUseCase.deleteAddress(context.tenantId(), addressId)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Organization address deleted.")));
    }

    @PostMapping("/organizations/{organizationId}/contacts")
    public Mono<ResponseEntity<ApiResponse<ContactResponse>>> createOrganizationContact(@PathVariable UUID organizationId,
            @Valid @RequestBody Mono<NestedContactRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureOrganizationExists(tuple.getT2().tenantId(), organizationId)
                        .then(createContactUseCase.createContact(new CreateContactCommand(
                                tuple.getT2().tenantId(), ContactableType.ORGANIZATION, organizationId,
                                tuple.getT1().firstName(), tuple.getT1().lastName(), tuple.getT1().title(),
                                tuple.getT1().isEmailVerified(), tuple.getT1().isPhoneNumberVerified(),
                                tuple.getT1().isFavorite(), tuple.getT1().phoneNumber(),
                                tuple.getT1().secondaryPhoneNumber(), tuple.getT1().faxNumber(),
                                tuple.getT1().email(), tuple.getT1().secondaryEmail()))))
                .map(ContactResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Organization contact created.")));
    }

    @GetMapping("/organizations/{organizationId}/contacts")
    public Mono<ResponseEntity<ApiResponse<List<ContactResponse>>>> listOrganizationContacts(@PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureOrganizationExists(context.tenantId(), organizationId)
                        .then(listContactsUseCase.listContacts(context.tenantId(), ContactableType.ORGANIZATION,
                                organizationId).map(ContactResponse::from).collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization contacts retrieved.")));
    }

    @DeleteMapping("/organizations/{organizationId}/contacts/{contactId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteOrganizationContact(@PathVariable UUID organizationId,
            @PathVariable UUID contactId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureOrganizationExists(context.tenantId(), organizationId)
                        .then(deleteContactUseCase.deleteContact(context.tenantId(), contactId)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Organization contact deleted.")));
    }

    @PostMapping("/agencies/{agencyId}/addresses")
    public Mono<ResponseEntity<ApiResponse<AddressResponse>>> createAgencyAddress(@PathVariable UUID agencyId,
            @Valid @RequestBody Mono<NestedAddressRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureAgencyExists(tuple.getT2().tenantId(), agencyId)
                        .then(createAddressUseCase.createAddress(new CreateAddressCommand(
                                tuple.getT2().tenantId(), AddressableType.AGENCY, agencyId,
                                tuple.getT1().type(), tuple.getT1().addressLine1(), tuple.getT1().addressLine2(),
                                tuple.getT1().city(), tuple.getT1().state(), tuple.getT1().locality(),
                                tuple.getT1().countryId(), tuple.getT1().zipCode(), tuple.getT1().postalCode(),
                                tuple.getT1().poBox(), tuple.getT1().isDefault(), tuple.getT1().neighborhood(),
                                tuple.getT1().informalDescription(), tuple.getT1().latitude(),
                                tuple.getT1().longitude()))))
                .map(AddressResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency address created.")));
    }

    @GetMapping("/agencies/{agencyId}/addresses")
    public Mono<ResponseEntity<ApiResponse<List<AddressResponse>>>> listAgencyAddresses(@PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureAgencyExists(context.tenantId(), agencyId)
                        .then(listAddressesUseCase.listAddresses(context.tenantId(), AddressableType.AGENCY, agencyId)
                                .map(AddressResponse::from).collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency addresses retrieved.")));
    }

    @DeleteMapping("/agencies/{agencyId}/addresses/{addressId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteAgencyAddress(@PathVariable UUID agencyId,
            @PathVariable UUID addressId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureAgencyExists(context.tenantId(), agencyId)
                        .then(deleteAddressUseCase.deleteAddress(context.tenantId(), addressId)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Agency address deleted.")));
    }

    @PostMapping("/agencies/{agencyId}/contacts")
    public Mono<ResponseEntity<ApiResponse<ContactResponse>>> createAgencyContact(@PathVariable UUID agencyId,
            @Valid @RequestBody Mono<NestedContactRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureAgencyExists(tuple.getT2().tenantId(), agencyId)
                        .then(createContactUseCase.createContact(new CreateContactCommand(
                                tuple.getT2().tenantId(), ContactableType.AGENCY, agencyId,
                                tuple.getT1().firstName(), tuple.getT1().lastName(), tuple.getT1().title(),
                                tuple.getT1().isEmailVerified(), tuple.getT1().isPhoneNumberVerified(),
                                tuple.getT1().isFavorite(), tuple.getT1().phoneNumber(),
                                tuple.getT1().secondaryPhoneNumber(), tuple.getT1().faxNumber(),
                                tuple.getT1().email(), tuple.getT1().secondaryEmail()))))
                .map(ContactResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency contact created.")));
    }

    @GetMapping("/agencies/{agencyId}/contacts")
    public Mono<ResponseEntity<ApiResponse<List<ContactResponse>>>> listAgencyContacts(@PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureAgencyExists(context.tenantId(), agencyId)
                        .then(listContactsUseCase.listContacts(context.tenantId(), ContactableType.AGENCY, agencyId)
                                .map(ContactResponse::from).collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency contacts retrieved.")));
    }

    @DeleteMapping("/agencies/{agencyId}/contacts/{contactId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteAgencyContact(@PathVariable UUID agencyId,
            @PathVariable UUID contactId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureAgencyExists(context.tenantId(), agencyId)
                        .then(deleteContactUseCase.deleteContact(context.tenantId(), contactId)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Agency contact deleted.")));
    }

    private Mono<Void> ensureOrganizationExists(UUID tenantId, UUID organizationId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                .then();
    }

    private Mono<Void> ensureAgencyExists(UUID tenantId, UUID agencyId) {
        return agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                .then();
    }

    record NestedAddressRequest(
            @NotNull AddressType type,
            @NotBlank String addressLine1,
            String addressLine2,
            String city,
            String state,
            String locality,
            UUID countryId,
            String zipCode,
            String postalCode,
            String poBox,
            boolean isDefault,
            String neighborhood,
            String informalDescription,
            Double latitude,
            Double longitude) {
    }

    record NestedContactRequest(
            String firstName,
            String lastName,
            String title,
            boolean isEmailVerified,
            boolean isPhoneNumberVerified,
            boolean isFavorite,
            String phoneNumber,
            String secondaryPhoneNumber,
            String faxNumber,
            String email,
            String secondaryEmail) {
    }
}
