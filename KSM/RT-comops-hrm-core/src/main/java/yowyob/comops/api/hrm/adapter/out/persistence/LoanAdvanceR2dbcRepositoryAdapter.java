package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import yowyob.comops.api.hrm.domain.model.LoanAdvanceStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LoanAdvanceR2dbcRepositoryAdapter implements LoanAdvanceRepository {

    private final LoanAdvanceSpringDataRepository repository;

    public LoanAdvanceR2dbcRepositoryAdapter(LoanAdvanceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LoanAdvance> save(LoanAdvance loanAdvance) {
        return repository.save(toEntity(loanAdvance)).map(this::toDomain);
    }

    @Override
    public Mono<LoanAdvance> findById(UUID tenantId, UUID loanAdvanceId) {
        return repository.findByIdAndTenantId(loanAdvanceId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<LoanAdvance> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<LoanAdvance> findActiveByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeIdAndStatus(tenantId, employeeId, "IN_REPAYMENT")
                .map(this::toDomain);
    }

    @Override
    public Flux<LoanAdvance> findByOrganization(UUID tenantId, UUID organizationId, String statusOpt) {
        Flux<LoanAdvanceEntity> source = (statusOpt == null || statusOpt.isBlank())
                ? repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                : repository.findAllByTenantIdAndOrganizationIdAndStatus(tenantId, organizationId, statusOpt);
        return source.map(this::toDomain);
    }

    private LoanAdvanceEntity toEntity(LoanAdvance la) {
        return new LoanAdvanceEntity(la.id(), la.tenantId(), la.createdAt(), la.updatedAt(),
                la.organizationId(), la.agencyId(), la.employeeId(), la.montant(), la.soldeRestant(),
                la.mensualite(), la.status().name(), la.dateDebut(), la.nbEcheances(), la.motif(),
                la.approvedBy());
    }

    private LoanAdvance toDomain(LoanAdvanceEntity e) {
        return LoanAdvance.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.employeeId(), e.montant(), e.soldeRestant(),
                e.mensualite(), LoanAdvanceStatus.valueOf(e.status()), e.dateDebut(), e.nbEcheances(),
                e.motif(), e.approvedBy());
    }
}
