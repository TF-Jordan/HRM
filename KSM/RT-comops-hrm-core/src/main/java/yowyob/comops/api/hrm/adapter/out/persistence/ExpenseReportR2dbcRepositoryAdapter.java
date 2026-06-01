package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.ExpenseReportRepository;
import yowyob.comops.api.hrm.domain.model.ExpenseReport;
import yowyob.comops.api.hrm.domain.model.ExpenseReportStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ExpenseReportR2dbcRepositoryAdapter implements ExpenseReportRepository {

    private final ExpenseReportSpringDataRepository repository;

    public ExpenseReportR2dbcRepositoryAdapter(ExpenseReportSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ExpenseReport> save(ExpenseReport report) {
        return repository.save(toEntity(report)).map(this::toDomain);
    }

    @Override
    public Mono<ExpenseReport> findById(UUID tenantId, UUID reportId) {
        return repository.findByIdAndTenantId(reportId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<ExpenseReport> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<ExpenseReport> findAll(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public Flux<ExpenseReport> findByStatus(UUID tenantId, String status) {
        return repository.findAllByTenantIdAndStatus(tenantId, status).map(this::toDomain);
    }

    private ExpenseReportEntity toEntity(ExpenseReport r) {
        return new ExpenseReportEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(),
                r.employeeId(), r.periode(), r.totalMontant(), r.motif(), r.status().name(),
                r.missionOrderId());
    }

    private ExpenseReport toDomain(ExpenseReportEntity e) {
        return ExpenseReport.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.periode(), e.totalMontant(), e.motif(),
                ExpenseReportStatus.valueOf(e.status()), e.missionOrderId());
    }
}
