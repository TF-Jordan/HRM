package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.GarnishmentOrderRepository;
import yowyob.comops.api.payroll.domain.model.GarnishmentOrder;
import yowyob.comops.api.payroll.domain.model.GarnishmentStatus;
import yowyob.comops.api.payroll.domain.model.GarnishmentType;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class GarnishmentOrderR2dbcRepositoryAdapter implements GarnishmentOrderRepository {

    private final GarnishmentOrderSpringDataRepository repository;

    public GarnishmentOrderR2dbcRepositoryAdapter(GarnishmentOrderSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<GarnishmentOrder> save(GarnishmentOrder order) {
        return repository.save(toEntity(order)).map(this::toDomain);
    }

    @Override
    public Mono<GarnishmentOrder> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<GarnishmentOrder> findByEmployee(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<GarnishmentOrder> findActiveByEmployee(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeIdAndStatus(tenantId, employeeId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<GarnishmentOrder> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private GarnishmentOrderEntity toEntity(GarnishmentOrder o) {
        return new GarnishmentOrderEntity(o.id(), o.tenantId(), o.createdAt(), o.updatedAt(),
                o.organizationId(), o.employeeId(), o.type().name(), o.beneficiary(), o.reference(),
                o.totalAmount(), o.remainingBalance(), o.monthlyAmount(), o.status().name());
    }

    private GarnishmentOrder toDomain(GarnishmentOrderEntity e) {
        return GarnishmentOrder.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), GarnishmentType.valueOf(e.type()), e.beneficiary(),
                e.reference(), e.totalAmount(), e.remainingBalance(), e.monthlyAmount(),
                GarnishmentStatus.valueOf(e.status()));
    }
}
