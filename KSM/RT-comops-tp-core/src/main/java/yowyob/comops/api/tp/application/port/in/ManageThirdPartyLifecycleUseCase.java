package yowyob.comops.api.tp.application.port.in;

import java.time.Instant;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import reactor.core.publisher.Mono;

public interface ManageThirdPartyLifecycleUseCase {
    Mono<ThirdParty> activateThirdParty(java.util.UUID thirdPartyId);
    Mono<ThirdParty> deactivateThirdParty(java.util.UUID thirdPartyId);
    Mono<ThirdParty> defineAccountingAccount(java.util.UUID thirdPartyId, String accountingAccount);
    Mono<ThirdParty> qualifyThirdParty(java.util.UUID thirdPartyId, String segment, Integer qualificationScore);
    Mono<ThirdParty> recomputeQualificationScore(java.util.UUID thirdPartyId);
    Mono<ThirdParty> scheduleFollowUp(java.util.UUID thirdPartyId, Instant nextFollowUpAt);
    Mono<ThirdParty> completeFollowUp(java.util.UUID thirdPartyId, Instant contactedAt, Instant nextFollowUpAt);
    Mono<ThirdParty> convertProspectToCustomer(java.util.UUID thirdPartyId);
}
