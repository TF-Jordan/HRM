package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OpeningHoursExceptionRepository;
import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOpeningHoursExceptionRepository implements OpeningHoursExceptionRepository {

    private final Map<UUID, OpeningHoursExceptionRule> rules = new ConcurrentHashMap<>();

    @Override
    public Mono<OpeningHoursExceptionRule> save(OpeningHoursExceptionRule exceptionRule) {
        return Mono.fromSupplier(() -> {
            rules.put(exceptionRule.id(), exceptionRule);
            return exceptionRule;
        });
    }

    @Override
    public Mono<OpeningHoursExceptionRule> findById(UUID tenantId, UUID exceptionId) {
        return Mono.justOrEmpty(rules.get(exceptionId)).filter(rule -> rule.tenantId().equals(tenantId));
    }

    @Override
    public Flux<OpeningHoursExceptionRule> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(rules.values().stream()
                .filter(rule -> rule.tenantId().equals(tenantId))
                .filter(rule -> rule.organizationId().equals(organizationId))
                .filter(rule -> rule.agencyId().equals(agencyId)));
    }

    @Override
    public Flux<OpeningHoursExceptionRule> findFutureByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId,
            LocalDate fromDate) {
        return findByAgencyId(tenantId, organizationId, agencyId)
                .filter(rule -> !rule.exceptionDate().isBefore(fromDate));
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID exceptionId) {
        return Mono.fromRunnable(() -> rules.computeIfPresent(exceptionId,
                (id, rule) -> rule.tenantId().equals(tenantId) ? null : rule));
    }
}
