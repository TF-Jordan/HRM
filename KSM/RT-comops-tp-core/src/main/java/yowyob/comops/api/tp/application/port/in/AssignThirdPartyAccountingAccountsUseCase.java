package yowyob.comops.api.tp.application.port.in;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;
import yowyob.comops.api.tp.domain.model.ThirdParty;

public interface AssignThirdPartyAccountingAccountsUseCase {

    Mono<ThirdParty> assignAccountingAccounts(UUID tenantId, UUID organizationId, UUID thirdPartyId,
            List<String> accountingAccountNumbers);
}
