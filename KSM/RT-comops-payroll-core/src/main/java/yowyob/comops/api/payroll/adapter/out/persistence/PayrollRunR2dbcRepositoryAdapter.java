package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayrollRunStatus;
import yowyob.comops.api.payroll.domain.model.RunType;

import java.util.UUID;

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
    public Mono<PayrollRun> findByOrganizationAndPeriodAndType(UUID tenantId, UUID organizationId,
                                                               String period, String runType) {
        return repository
                .findByTenantIdAndOrganizationIdAndAgencyIdIsNullAndPeriodeAndRunType(
                        tenantId, organizationId, period, runType)
                .map(this::toDomain);
    }

    @Override
    public Mono<PayrollRun> findByOrganizationAndAgencyAndPeriodAndType(UUID tenantId, UUID organizationId,
                                                                        UUID agencyId, String period,
                                                                        String runType) {
        return repository
                .findByTenantIdAndOrganizationIdAndAgencyIdAndPeriodeAndRunType(
                        tenantId, organizationId, agencyId, period, runType)
                .map(this::toDomain);
    }

    @Override
    public Flux<PayrollRun> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private PayrollRunEntity toEntity(PayrollRun r) {
        return new PayrollRunEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(), r.organizationId(),
                r.agencyId(), r.period().format(), r.runType().name(), r.status().name(), r.currency(),
                r.totalGross(), r.totalEmployeeDeductions(), r.totalIncomeTax(), r.totalNet(),
                r.totalEmployerCharges(), r.nbEmployes(), r.calculatedAt(), r.validatedBy(), r.validatedAt(),
                r.approvedBy(), r.approvedAt(), r.paidAt(), r.closedAt(),
                r.rejectionReason(), r.rejectedBy(), r.rejectedAt());
    }

    private PayrollRun toDomain(PayrollRunEntity e) {
        return PayrollRun.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.agencyId(), PayPeriod.parse(e.periode()), RunType.valueOf(e.runType()),
                PayrollRunStatus.valueOf(e.status()), e.currency(), e.totalGross(),
                e.totalEmployeeDeductions(), e.totalIncomeTax(), e.totalNet(), e.totalEmployerCharges(),
                e.nbEmployes(), e.calculatedAt(), e.validatedBy(), e.validatedAt(), e.approvedBy(),
                e.approvedAt(), e.paidAt(), e.closedAt(),
                e.rejectionReason(), e.rejectedBy(), e.rejectedAt());
    }
}
