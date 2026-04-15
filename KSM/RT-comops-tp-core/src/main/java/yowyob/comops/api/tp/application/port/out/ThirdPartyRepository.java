package yowyob.comops.api.tp.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.domain.model.ThirdParty;

public interface ThirdPartyRepository {

    Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceCode);

    Mono<Boolean> existsByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount, UUID excludedThirdPartyId);

    Mono<ThirdParty> findById(UUID tenantId, UUID thirdPartyId);

    Mono<ThirdParty> findByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount);

    Mono<ThirdParty> findByPartyRef(UUID tenantId, UUID organizationId, PartyType partyType, UUID partyId);

    Flux<ThirdParty> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<ThirdParty> save(ThirdParty thirdParty);

    Mono<Void> deleteById(UUID tenantId, UUID thirdPartyId);
}
