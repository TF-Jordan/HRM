package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.JobOffer;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JobOfferRepository {

    Mono<JobOffer> save(JobOffer jobOffer);

    Mono<JobOffer> findById(UUID tenantId, UUID jobOfferId);

    Flux<JobOffer> findByOrganizationId(UUID tenantId, UUID organizationId);
}
