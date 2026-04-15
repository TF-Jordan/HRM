package yowyob.comops.api.tp.adapter.out.persistence;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.domain.model.ThirdParty;

@Component
@Profile("test-memory")
public class InMemoryThirdPartyRepository implements ThirdPartyRepository {

    private final Map<UUID, ThirdParty> thirdParties = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceCode) {
        return Mono.fromSupplier(() -> thirdParties.values().stream()
                .filter(thirdParty -> thirdParty.tenantId().equals(tenantId))
                .filter(thirdParty -> thirdParty.organizationId().equals(organizationId))
                .anyMatch(thirdParty -> thirdParty.referenceCode().equalsIgnoreCase(referenceCode)));
    }

    @Override
    public Mono<Boolean> existsByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount,
            UUID excludedThirdPartyId) {
        if (accountingAccount == null || accountingAccount.isBlank()) {
            return Mono.just(false);
        }
        return Mono.fromSupplier(() -> thirdParties.values().stream()
                .filter(thirdParty -> thirdParty.tenantId().equals(tenantId))
                .filter(thirdParty -> thirdParty.organizationId().equals(organizationId))
                .filter(thirdParty -> !thirdParty.id().equals(excludedThirdPartyId))
                .anyMatch(thirdParty -> thirdParty.accountingAccountNumbers().stream()
                        .anyMatch(account -> accountingAccount.equalsIgnoreCase(account))));
    }

    @Override
    public Mono<ThirdParty> findById(UUID tenantId, UUID thirdPartyId) {
        return Mono.justOrEmpty(thirdParties.get(thirdPartyId))
                .filter(thirdParty -> thirdParty.tenantId().equals(tenantId));
    }

    @Override
    public Mono<ThirdParty> findByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount) {
        if (accountingAccount == null || accountingAccount.isBlank()) {
            return Mono.empty();
        }
        return Flux.fromStream(thirdParties.values().stream()
                .filter(thirdParty -> thirdParty.tenantId().equals(tenantId))
                .filter(thirdParty -> thirdParty.organizationId().equals(organizationId))
                .filter(thirdParty -> thirdParty.accountingAccountNumbers().stream()
                        .anyMatch(account -> accountingAccount.equalsIgnoreCase(account))))
                .next();
    }

    @Override
    public Mono<ThirdParty> findByPartyRef(UUID tenantId, UUID organizationId, PartyType partyType, UUID partyId) {
        return Flux.fromStream(thirdParties.values().stream()
                .filter(thirdParty -> thirdParty.tenantId().equals(tenantId))
                .filter(thirdParty -> thirdParty.organizationId().equals(organizationId))
                .filter(thirdParty -> thirdParty.partyRef().partyType() == partyType)
                .filter(thirdParty -> thirdParty.partyRef().partyId().equals(partyId)))
                .next();
    }

    @Override
    public Flux<ThirdParty> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(thirdParties.values().stream()
                .filter(thirdParty -> thirdParty.tenantId().equals(tenantId))
                .filter(thirdParty -> thirdParty.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<ThirdParty> save(ThirdParty thirdParty) {
        return Mono.fromSupplier(() -> {
            thirdParties.put(thirdParty.id(), thirdParty);
            return thirdParty;
        });
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID thirdPartyId) {
        return Mono.fromRunnable(() -> {
            ThirdParty existing = thirdParties.get(thirdPartyId);
            if (existing != null && existing.tenantId().equals(tenantId)) {
                thirdParties.remove(thirdPartyId);
            }
        });
    }
}
