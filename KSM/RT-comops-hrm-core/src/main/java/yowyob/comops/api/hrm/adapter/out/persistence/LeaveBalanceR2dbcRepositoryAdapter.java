package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveType;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LeaveBalanceR2dbcRepositoryAdapter implements LeaveBalanceRepository {

    private final LeaveBalanceSpringDataRepository repository;

    public LeaveBalanceR2dbcRepositoryAdapter(LeaveBalanceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LeaveBalance> save(LeaveBalance leaveBalance) {
        return repository.save(toEntity(leaveBalance)).map(this::toDomain);
    }

    @Override
    public Mono<LeaveBalance> findByEmployeeIdAndTypeAndAnnee(UUID tenantId, UUID employeeId, LeaveType type, int annee) {
        return repository.findByTenantIdAndEmployeeIdAndTypeAndAnnee(tenantId, employeeId, type.name(), annee)
                .map(this::toDomain);
    }

    @Override
    public Flux<LeaveBalance> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<LeaveBalance> findByEmployeeIdAndAnnee(UUID tenantId, UUID employeeId, int annee) {
        return repository.findAllByTenantIdAndEmployeeIdAndAnnee(tenantId, employeeId, annee).map(this::toDomain);
    }

    private LeaveBalanceEntity toEntity(LeaveBalance lb) {
        return new LeaveBalanceEntity(lb.id(), lb.tenantId(), lb.createdAt(), lb.updatedAt(),
                lb.organizationId(), lb.employeeId(), lb.type().name(), lb.acquis(), lb.pris(), lb.annee(),
                lb.lastAccrualPeriod());
    }

    private LeaveBalance toDomain(LeaveBalanceEntity e) {
        return LeaveBalance.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), LeaveType.valueOf(e.type()), e.acquis(), e.pris(), e.annee(),
                e.lastAccrualPeriod());
    }
}
