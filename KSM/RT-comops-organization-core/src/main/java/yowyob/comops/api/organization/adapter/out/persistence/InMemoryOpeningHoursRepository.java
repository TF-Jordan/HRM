package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OpeningHoursRepository;
import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOpeningHoursRepository implements OpeningHoursRepository {

    private final Map<String, OpeningHoursRule> rules = new ConcurrentHashMap<>();

    @Override
    public Mono<OpeningHoursRule> save(OpeningHoursRule openingHoursRule) {
        return Mono.fromSupplier(() -> {
            rules.put(key(openingHoursRule.tenantId(), openingHoursRule.organizationId(), openingHoursRule.agencyId(), openingHoursRule.dayOfWeek()), openingHoursRule);
            return openingHoursRule;
        });
    }

    @Override
    public Mono<OpeningHoursRule> findByScopeAndDay(UUID tenantId, UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek) {
        return Mono.justOrEmpty(rules.get(key(tenantId, organizationId, agencyId, dayOfWeek)));
    }

    @Override
    public Flux<OpeningHoursRule> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(rules.values().stream()
                .filter(rule -> rule.tenantId().equals(tenantId))
                .filter(rule -> rule.organizationId().equals(organizationId))
                .filter(rule -> rule.agencyId().equals(agencyId)));
    }

    @Override
    public Mono<Void> deleteAllByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.fromRunnable(() -> rules.entrySet().removeIf(entry -> {
            OpeningHoursRule rule = entry.getValue();
            return rule.tenantId().equals(tenantId)
                    && rule.organizationId().equals(organizationId)
                    && rule.agencyId().equals(agencyId);
        }));
    }

    private static String key(UUID tenantId, UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek) {
        return tenantId + ":" + organizationId + ":" + agencyId + ":" + dayOfWeek;
    }
}
