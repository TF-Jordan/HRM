package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.common.domain.model.PartyRef;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ThirdPartyR2dbcRepositoryAdapter implements ThirdPartyRepository {

    private final ThirdPartySpringDataRepository repository;

    public ThirdPartyR2dbcRepositoryAdapter(ThirdPartySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceCode) {
        return repository.existsByTenantIdAndOrganizationIdAndReferenceCodeIgnoreCase(tenantId, organizationId,
                referenceCode);
    }

    @Override
    public Mono<Boolean> existsByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount,
            UUID excludedThirdPartyId) {
        if (accountingAccount == null || accountingAccount.isBlank()) {
            return Mono.just(false);
        }
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .filter(entity -> entity.accountingAccountNumbers() != null)
                .filter(entity -> entity.accountingAccountNumbers().stream()
                        .anyMatch(account -> account.equalsIgnoreCase(accountingAccount)))
                .map(entity -> !entity.id().equals(excludedThirdPartyId))
                .next()
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<ThirdParty> findById(UUID tenantId, UUID thirdPartyId) {
        return repository.findByIdAndTenantId(thirdPartyId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<ThirdParty> findByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount) {
        if (accountingAccount == null || accountingAccount.isBlank()) {
            return Mono.empty();
        }
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .filter(entity -> entity.accountingAccountNumbers() != null)
                .filter(entity -> entity.accountingAccountNumbers().stream()
                        .anyMatch(account -> account.equalsIgnoreCase(accountingAccount)))
                .next()
                .map(this::toDomain);
    }

    @Override
    public Mono<ThirdParty> findByPartyRef(UUID tenantId, UUID organizationId, PartyType partyType, UUID partyId) {
        return repository.findFirstByTenantIdAndOrganizationIdAndPartyTypeAndPartyId(
                        tenantId,
                        organizationId,
                        partyType.name(),
                        partyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<ThirdParty> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<ThirdParty> save(ThirdParty tp) {
        ThirdPartyEntity entity = new ThirdPartyEntity(
                tp.id(), tp.tenantId(), tp.createdAt(), tp.updatedAt(),
                tp.organizationId(), tp.partyRef().partyType().name(), tp.partyRef().partyId(),
                tp.code(), tp.referenceCode(), tp.displayName(), tp.roles(), tp.prospect(),
                tp.accountingAccount(), tp.segment(), tp.qualificationScore(),
                tp.lastContactedAt(), tp.nextFollowUpAt(), tp.followUpStatus(),
                tp.active(), tp.convertedAt(),
                // canonical fields
                tp.type(), tp.legalForm(), tp.uniqueIdentificationNumber(), tp.tradeRegistrationNumber(),
                tp.name(), tp.acronym(), tp.longName(), tp.logoUri(), tp.logoId(),
                tp.accountingAccountNumbers(), tp.authorizedPaymentMethods(),
                tp.authorizedCreditLimit(), tp.maxDiscountRate(), tp.vatSubject(),
                tp.operationsBalance(), tp.openingBalance(), tp.payTermNumber(), tp.payTermType(),
                tp.thirdPartyFamily(), tp.classification(), tp.taxNumber(),
                tp.loyaltyPoints(), tp.loyaltyPointsUsed(), tp.loyaltyPointsExpired(),
                tp.enabled(), tp.deletedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID thirdPartyId) {
        return repository.findByIdAndTenantId(thirdPartyId, tenantId).flatMap(repository::delete).then();
    }

    private ThirdParty toDomain(ThirdPartyEntity e) {
        return ThirdParty.rehydrate(
                e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), new PartyRef(PartyType.valueOf(e.partyType()), e.partyId()),
                firstNonBlank(e.code(), e.referenceCode()), firstNonBlank(e.name(), e.displayName()),
                e.roles(), e.prospect(), e.accountingAccount(), e.segment(), e.qualificationScore(),
                e.enabled(),
                e.lastContactedAt(), e.nextFollowUpAt(), e.followUpStatus(), e.convertedAt(),
                // canonical fields
                e.type(), e.legalForm(), e.uniqueIdentificationNumber(), e.tradeRegistrationNumber(),
                e.name(), e.acronym(), e.longName(), e.logoUri(), e.logoId(),
                e.accountingAccountNumbers(), e.authorizedPaymentMethods(),
                e.authorizedCreditLimit(), e.maxDiscountRate(), e.vatSubject(),
                e.operationsBalance(), e.openingBalance(), e.payTermNumber(), e.payTermType(),
                e.thirdPartyFamily(), e.classification(), e.taxNumber(),
                e.loyaltyPoints(), e.loyaltyPointsUsed(), e.loyaltyPointsExpired(),
                e.deletedAt());
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
