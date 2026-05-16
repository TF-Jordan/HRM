package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdPartyStatistics;
import reactor.core.publisher.Mono;

public interface GetThirdPartyStatisticsUseCase {
    Mono<ThirdPartyStatistics> getStatistics(java.util.UUID organizationId, String role, Boolean prospect);
    Mono<Long> getProspectConversionCount(java.util.UUID organizationId);
}
