package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.ContractStatus;
import yowyob.comops.api.hrm.domain.model.ContractType;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ContractR2dbcRepositoryAdapter implements ContractRepository {

    private final ContractSpringDataRepository repository;

    public ContractR2dbcRepositoryAdapter(ContractSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Contract> save(Contract contract) {
        return repository.save(toEntity(contract)).map(this::toDomain);
    }

    @Override
    public Mono<Contract> findById(UUID tenantId, UUID contractId) {
        return repository.findByTenantIdAndId(tenantId, contractId).map(this::toDomain);
    }

    @Override
    public Mono<Contract> findActiveByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findByTenantIdAndEmployeeIdAndStatus(tenantId, employeeId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<Contract> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private ContractEntity toEntity(Contract c) {
        return new ContractEntity(c.id(), c.tenantId(), c.createdAt(), c.updatedAt(), c.organizationId(),
                c.agencyId(), c.employeeId(), c.type().name(), c.dateDebut(), c.dateFin(), c.salaireBase(),
                c.avantagesNature(), c.periodeEssai(), c.status().name(), c.motifFin(), c.documentFileId());
    }

    private Contract toDomain(ContractEntity e) {
        return Contract.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.agencyId(), e.employeeId(), ContractType.valueOf(e.type()), e.dateDebut(), e.dateFin(),
                e.salaireBase(), e.avantagesNature(), e.periodeEssai(), ContractStatus.valueOf(e.status()),
                e.motifFin(), e.documentFileId());
    }
}
