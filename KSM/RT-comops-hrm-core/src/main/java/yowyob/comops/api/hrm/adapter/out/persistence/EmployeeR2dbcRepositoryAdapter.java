package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.EmployeeStatus;
import yowyob.comops.api.hrm.domain.model.MobileOperator;
import yowyob.comops.api.hrm.domain.model.PaymentChannel;
import yowyob.comops.api.hrm.domain.model.TenantOrgPair;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class EmployeeR2dbcRepositoryAdapter implements EmployeeRepository {

    private final EmployeeSpringDataRepository repository;
    private final DatabaseClient databaseClient;

    public EmployeeR2dbcRepositoryAdapter(EmployeeSpringDataRepository repository,
                                           DatabaseClient databaseClient) {
        this.repository = repository;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Employee> save(Employee employee) {
        return repository.save(toEntity(employee)).map(this::toDomain);
    }

    @Override
    public Mono<Employee> findById(UUID tenantId, UUID employeeId) {
        return repository.findByIdAndTenantId(employeeId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByActorIdAndTenantId(UUID actorId, UUID tenantId) {
        return repository.existsByActorIdAndTenantId(actorId, tenantId);
    }

    @Override
    public Mono<Employee> findByActorId(UUID tenantId, UUID actorId) {
        return repository.findByActorIdAndTenantId(actorId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumCnpsAndTenantId(String numCnps, UUID tenantId) {
        return repository.existsByNumCnpsAndTenantId(numCnps, tenantId);
    }

    @Override
    public Flux<Employee> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Flux<Employee> findByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Employee> findActiveByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationIdAndStatus(tenantId, organizationId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<Employee> findActiveByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyIdAndStatus(tenantId, organizationId, agencyId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<TenantOrgPair> findDistinctActiveOrganizations() {
        return databaseClient
                .sql("SELECT DISTINCT tenant_id, organization_id FROM hrm_employee WHERE status = 'ACTIVE'")
                .map((row, metadata) -> new TenantOrgPair(
                        row.get("tenant_id", UUID.class),
                        row.get("organization_id", UUID.class)))
                .all();
    }

    private EmployeeEntity toEntity(Employee e) {
        return new EmployeeEntity(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.agencyId(), e.actorId(), e.managerId(), e.matricule(), e.numCnps(), e.categorie(), e.echelon(),
                e.dateEmbauche(), e.status().name(), e.departmentCode(), e.modePaiement().name(),
                e.compteBancaire(), e.numMobileMoney(),
                e.operateurMm() != null ? e.operateurMm().name() : null, e.actorDisplayName(),
                e.dateSortie(), e.motifSortie());
    }

    private Employee toDomain(EmployeeEntity e) {
        return Employee.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.agencyId(), e.actorId(), e.managerId(), e.matricule(), e.numCnps(), e.categorie(), e.echelon(),
                e.dateEmbauche(), EmployeeStatus.valueOf(e.status()), e.departmentCode(),
                PaymentChannel.valueOf(e.modePaiement()), e.compteBancaire(), e.numMobileMoney(),
                e.operateurMm() != null ? MobileOperator.valueOf(e.operateurMm()) : null, e.actorDisplayName(),
                e.dateSortie(), e.motifSortie());
    }
}
