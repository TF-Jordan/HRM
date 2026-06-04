package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.FinalSettlementRepository;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;
import yowyob.comops.api.payroll.domain.model.FinalSettlementStatus;
import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class FinalSettlementR2dbcRepositoryAdapter implements FinalSettlementRepository {

    private final FinalSettlementSpringDataRepository repository;

    public FinalSettlementR2dbcRepositoryAdapter(FinalSettlementSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<FinalSettlement> save(FinalSettlement settlement) {
        return repository.save(toEntity(settlement)).map(this::toDomain);
    }

    @Override
    public Mono<FinalSettlement> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<FinalSettlement> findByEmployee(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<FinalSettlement> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private FinalSettlementEntity toEntity(FinalSettlement s) {
        return new FinalSettlementEntity(s.id(), s.tenantId(), s.createdAt(), s.updatedAt(),
                s.organizationId(), s.employeeId(), s.periode(), s.departureDate(), s.reason().name(),
                s.currency(), s.seniorityYears(), s.proratedSalary(), s.leaveCompensation(),
                s.noticeIndemnity(), s.severanceIndemnity(), s.gratification(), s.grossSettlement(),
                s.loanDeducted(), s.netSettlement(), s.status().name());
    }

    private FinalSettlement toDomain(FinalSettlementEntity e) {
        return FinalSettlement.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.periode(), e.departureDate(),
                TerminationReason.valueOf(e.reason()), e.currency(), e.seniorityYears(), e.proratedSalary(),
                e.leaveCompensation(), e.noticeIndemnity(), e.severanceIndemnity(), e.gratification(),
                e.grossSettlement(), e.loanDeducted(), e.netSettlement(),
                FinalSettlementStatus.valueOf(e.status()));
    }
}
