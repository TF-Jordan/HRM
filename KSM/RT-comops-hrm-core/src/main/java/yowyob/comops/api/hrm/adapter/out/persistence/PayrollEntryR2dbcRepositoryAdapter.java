package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.hrm.domain.model.PaymentStatus;
import yowyob.comops.api.hrm.domain.model.PayrollEntry;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Flux<PayrollEntry> findByPayrollRunId(UUID tenantId, UUID payrollRunId) {
        return repository.findAllByTenantIdAndPayrollRunId(tenantId, payrollRunId).map(this::toDomain);
    }

    @Override
    public Mono<PayrollEntry> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollEntry> findByPayrollRunIdAndPaymentStatus(UUID tenantId, UUID payrollRunId, String status) {
        return repository.findAllByTenantIdAndPayrollRunIdAndPaymentStatus(tenantId, payrollRunId, status)
                .map(this::toDomain);
    }

    private PayrollEntryEntity toEntity(PayrollEntry e) {
        return new PayrollEntryEntity(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.payrollRunId(), e.employeeId(), e.salaireBase(), e.brut(),
                e.net(), e.cnpsEmploye(), e.cnpsEmployeur(), e.irpp(), e.cac(), e.primes(),
                e.retenues(), e.avancesDeduites(), e.paymentStatus().name(), e.paymentChannel(),
                e.accountRef());
    }

    private PayrollEntry toDomain(PayrollEntryEntity e) {
        return PayrollEntry.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.payrollRunId(), e.employeeId(), e.salaireBase(), e.brut(),
                e.net(), e.cnpsEmploye(), e.cnpsEmployeur(), e.irpp(), e.cac(), e.primes(),
                e.retenues(), e.avancesDeduites(), PaymentStatus.valueOf(e.paymentStatus()),
                e.paymentChannel(), e.accountRef());
    }
}
