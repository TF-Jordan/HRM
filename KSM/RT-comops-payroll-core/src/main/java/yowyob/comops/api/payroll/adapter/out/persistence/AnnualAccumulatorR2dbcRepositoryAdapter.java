package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.AnnualAccumulatorRepository;
import yowyob.comops.api.payroll.domain.model.AnnualAccumulator;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class AnnualAccumulatorR2dbcRepositoryAdapter implements AnnualAccumulatorRepository {

    private final AnnualAccumulatorSpringDataRepository repository;

    public AnnualAccumulatorR2dbcRepositoryAdapter(AnnualAccumulatorSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AnnualAccumulator> save(AnnualAccumulator accumulator) {
        return repository.save(toEntity(accumulator)).map(this::toDomain);
    }

    @Override
    public Mono<AnnualAccumulator> findByEmployeeAndYear(UUID tenantId, UUID employeeId, int year) {
        return repository.findByTenantIdAndEmployeeIdAndYear(tenantId, employeeId, year).map(this::toDomain);
    }

    private AnnualAccumulatorEntity toEntity(AnnualAccumulator a) {
        return new AnnualAccumulatorEntity(a.id(), a.tenantId(), a.createdAt(), a.updatedAt(),
                a.organizationId(), a.employeeId(), a.year(), a.cumulativeGross(), a.cumulativeDeductions(),
                a.cumulativeIncomeTax(), a.cumulativeNet(), a.cumulativeEmployerCharges());
    }

    private AnnualAccumulator toDomain(AnnualAccumulatorEntity e) {
        return AnnualAccumulator.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.year(), e.cumulativeGross(), e.cumulativeDeductions(),
                e.cumulativeIncomeTax(), e.cumulativeNet(), e.cumulativeEmployerCharges());
    }
}
