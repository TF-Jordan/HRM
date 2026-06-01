package yowyob.comops.api.tp.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ThirdPartyBankAccountSpringDataRepository
        extends ReactiveCrudRepository<ThirdPartyBankAccountEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndThirdPartyIdAndIbanIgnoreCase(UUID tenantId, UUID thirdPartyId, String iban);

    Mono<ThirdPartyBankAccountEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<ThirdPartyBankAccountEntity> findAllByTenantIdAndThirdPartyId(UUID tenantId, UUID thirdPartyId);

    Mono<ThirdPartyBankAccountEntity> findFirstByTenantIdAndThirdPartyIdAndPrimaryAccountTrue(UUID tenantId,
            UUID thirdPartyId);

    Mono<ThirdPartyBankAccountEntity> findFirstByTenantIdAndIbanIgnoreCase(UUID tenantId, String iban);
}
