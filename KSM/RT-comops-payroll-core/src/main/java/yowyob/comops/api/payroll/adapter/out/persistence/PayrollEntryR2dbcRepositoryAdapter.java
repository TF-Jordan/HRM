package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PaymentStatus;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class PayrollEntryR2dbcRepositoryAdapter implements PayrollEntryRepository {

    private final PayrollEntrySpringDataRepository repository;

    public PayrollEntryR2dbcRepositoryAdapter(PayrollEntrySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayrollEntry> save(PayrollEntry entry) {
        return repository.save(toEntity(entry)).map(this::toDomain);
    }

    @Override
    public Mono<PayrollEntry> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollEntry> findByRun(UUID tenantId, UUID payrollRunId) {
        return repository.findAllByTenantIdAndPayrollRunId(tenantId, payrollRunId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollEntry> findByEmployee(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private PayrollEntryEntity toEntity(PayrollEntry e) {
        return new PayrollEntryEntity(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.payrollRunId(), e.employeeId(), e.currency(), e.salaireBase(), e.brut(),
                e.totalDeductions(), e.incomeTax(), e.employerCharges(), e.net(),
                e.paymentStatus().name(), e.paymentChannel().name(), e.accountRef());
    }

    private PayrollEntry toDomain(PayrollEntryEntity e) {
        return PayrollEntry.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.payrollRunId(), e.employeeId(), e.currency(), e.salaireBase(), e.brut(),
                e.totalDeductions(), e.incomeTax(), e.employerCharges(), e.net(),
                PaymentStatus.valueOf(e.paymentStatus()), PaymentChannel.valueOf(e.paymentChannel()),
                e.accountRef());
    }
}
