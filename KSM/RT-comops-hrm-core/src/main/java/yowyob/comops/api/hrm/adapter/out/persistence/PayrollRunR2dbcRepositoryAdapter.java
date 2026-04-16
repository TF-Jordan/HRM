package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PayrollRunRepository;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.hrm.domain.model.PayrollRunStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PayrollRunR2dbcRepositoryAdapter implements PayrollRunRepository {

    private final PayrollRunSpringDataRepository repository;

    public PayrollRunR2dbcRepositoryAdapter(PayrollRunSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayrollRun> save(PayrollRun run) {
        return repository.save(toEntity(run)).map(this::toDomain);
    }

    @Override
    public Mono<PayrollRun> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<PayrollRun> findByOrganizationIdAndPeriode(UUID tenantId, UUID orgId, String periode) {
        return repository.findByTenantIdAndOrganizationIdAndPeriode(tenantId, orgId, periode).map(this::toDomain);
    }

    @Override
    public Mono<PayrollRun> findByOrganizationIdAndAgencyIdAndPeriode(UUID tenantId, UUID orgId, UUID agencyId, String periode) {
        return repository.findByTenantIdAndOrganizationIdAndAgencyIdAndPeriode(tenantId, orgId, agencyId, periode).map(this::toDomain);
    }

    @Override
    public Flux<PayrollRun> findByOrganizationId(UUID tenantId, UUID orgId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, orgId).map(this::toDomain);
    }

    private PayrollRunEntity toEntity(PayrollRun r) {
        return new PayrollRunEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(),
                r.organizationId(), r.agencyId(), r.periode(), r.status().name(),
                r.totalBrut(), r.totalNet(), r.totalCnpsEmploye(), r.totalCnpsEmployeur(),
                r.totalIrpp(), r.nbEmployes(), r.calculatedAt(), r.validatedBy(), r.validatedAt());
    }

    private PayrollRun toDomain(PayrollRunEntity e) {
        return PayrollRun.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.periode(), PayrollRunStatus.valueOf(e.status()),
                e.totalBrut(), e.totalNet(), e.totalCnpsEmploye(), e.totalCnpsEmployeur(),
                e.totalIrpp(), e.nbEmployes(), e.calculatedAt(), e.validatedBy(), e.validatedAt());
    }
}
