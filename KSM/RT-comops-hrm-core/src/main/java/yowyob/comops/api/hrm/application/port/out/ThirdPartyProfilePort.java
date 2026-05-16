package yowyob.comops.api.hrm.application.port.out;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ThirdPartyProfilePort {

    Mono<ThirdPartyProfile> ensureEmployeeFinancialProfile(UUID tenantId, UUID organizationId, UUID actorId,
            String referenceCode, String displayName);

    record ThirdPartyProfile(UUID thirdPartyId, String referenceCode, String accountingAccount) {
    }
}
