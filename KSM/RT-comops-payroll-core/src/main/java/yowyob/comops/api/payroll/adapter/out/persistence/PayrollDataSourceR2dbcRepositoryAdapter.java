package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile("r2dbc")
public class PayrollDataSourceR2dbcRepositoryAdapter implements PayrollDataSourceRepository {

    private final PayrollDataSourceSpringDataRepository repository;

    public PayrollDataSourceR2dbcRepositoryAdapter(PayrollDataSourceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Source> get(UUID tenantId, UUID organizationId) {
        return repository.findByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(e -> Source.valueOf(e.source()))
                .defaultIfEmpty(Source.HRM);
    }

    @Override
    public Mono<Void> set(UUID tenantId, UUID organizationId, Source source) {
        Instant now = Instant.now();
        return repository.findByTenantIdAndOrganizationId(tenantId, organizationId)
                .flatMap(existing -> repository.save(new PayrollDataSourceEntity(
                        existing.id(), existing.tenantId(), existing.createdAt(), now,
                        existing.organizationId(), source.name())))
                .switchIfEmpty(repository.save(new PayrollDataSourceEntity(
                        UUID.randomUUID(), tenantId, now, now, organizationId, source.name())))
                .then();
    }
}
