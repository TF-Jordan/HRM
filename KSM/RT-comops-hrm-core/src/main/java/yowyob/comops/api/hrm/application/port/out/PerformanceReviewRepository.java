package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.PerformanceReview;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PerformanceReviewRepository {

    Mono<PerformanceReview> save(PerformanceReview review);

    Mono<PerformanceReview> findById(UUID tenantId, UUID reviewId);

    Flux<PerformanceReview> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<PerformanceReview> findByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);
}
