package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayrollEmployeeRepository;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class PayrollEmployeeR2dbcRepositoryAdapter implements PayrollEmployeeRepository {

    private final PayrollEmployeeSpringDataRepository repository;

    public PayrollEmployeeR2dbcRepositoryAdapter(PayrollEmployeeSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayrollEmployee> save(PayrollEmployee employee) {
        return repository.save(toEntity(employee)).map(this::toDomain);
    }

    @Override
    public Mono<PayrollEmployee> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<PayrollEmployee> findByMatricule(UUID tenantId, UUID organizationId, String matricule) {
        return repository.findByTenantIdAndOrganizationIdAndMatricule(tenantId, organizationId, matricule)
                .map(this::toDomain);
    }

    @Override
    public Mono<PayrollEmployee> findByActorId(UUID tenantId, UUID actorId) {
        return repository.findFirstByTenantIdAndActorId(tenantId, actorId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollEmployee> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Flux<PayrollEmployee> findActiveByOrganization(UUID tenantId, UUID organizationId, UUID agencyId) {
        Flux<PayrollEmployeeEntity> entities = agencyId != null
                ? repository.findAllByTenantIdAndOrganizationIdAndAgencyIdAndActiveTrue(
                        tenantId, organizationId, agencyId)
                : repository.findAllByTenantIdAndOrganizationIdAndActiveTrue(tenantId, organizationId);
        return entities.map(this::toDomain);
    }

    private PayrollEmployeeEntity toEntity(PayrollEmployee e) {
        return new PayrollEmployeeEntity(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.actorId(), e.matricule(), e.displayName(),
                e.email(), e.socialSecurityNo(), e.categorie(), e.echelon(), e.departmentCode(),
                e.hireDate(), e.departureDate(), e.maritalStatus().name(), e.dependentChildren(),
                e.baseSalary(), e.benefitsInKind(), e.position(), e.paymentChannel().name(),
                e.accountRef(), e.active());
    }

    private PayrollEmployee toDomain(PayrollEmployeeEntity e) {
        return PayrollEmployee.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.actorId(), e.matricule(), e.displayName(),
                e.email(), e.socialSecurityNo(), e.categorie(), e.echelon(), e.departmentCode(),
                e.hireDate(), e.departureDate(), MaritalStatus.valueOf(e.maritalStatus()),
                e.dependentChildren(), e.baseSalary(), e.benefitsInKind(), e.position(),
                PaymentChannel.valueOf(e.paymentChannel()), e.accountRef(), e.active());
    }
}
