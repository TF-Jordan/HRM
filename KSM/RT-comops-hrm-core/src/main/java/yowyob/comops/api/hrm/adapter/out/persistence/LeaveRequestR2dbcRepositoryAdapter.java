package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LeaveRequestRepository;
import yowyob.comops.api.hrm.domain.model.LeaveRequest;
import yowyob.comops.api.hrm.domain.model.LeaveStatus;
import yowyob.comops.api.hrm.domain.model.LeaveType;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LeaveRequestR2dbcRepositoryAdapter implements LeaveRequestRepository {

    private final LeaveRequestSpringDataRepository repository;

    public LeaveRequestR2dbcRepositoryAdapter(LeaveRequestSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LeaveRequest> save(LeaveRequest leaveRequest) {
        return repository.save(toEntity(leaveRequest)).map(this::toDomain);
    }

    @Override
    public Mono<LeaveRequest> findById(UUID tenantId, UUID leaveRequestId) {
        return repository.findByIdAndTenantId(leaveRequestId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<LeaveRequest> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<LeaveRequest> findPendingByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationIdAndStatus(tenantId, organizationId, "PENDING")
                .map(this::toDomain);
    }

    @Override
    public Flux<LeaveRequest> findPendingByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyIdAndStatus(tenantId, organizationId, agencyId, "PENDING")
                .map(this::toDomain);
    }

    private LeaveRequestEntity toEntity(LeaveRequest r) {
        return new LeaveRequestEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(),
                r.organizationId(), r.agencyId(), r.employeeId(), r.type().name(),
                r.dateDebut(), r.dateFin(), r.nbJours(), r.status().name(), r.motif(),
                r.valideurPartyId(), r.valideurDisplayName(), r.dateValidation(),
                r.commentaireValideur(), r.justificatifFileId());
    }

    private LeaveRequest toDomain(LeaveRequestEntity e) {
        return LeaveRequest.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.employeeId(), LeaveType.valueOf(e.type()),
                e.dateDebut(), e.dateFin(), e.nbJours(), LeaveStatus.valueOf(e.status()),
                e.motif(), e.valideurPartyId(), e.valideurDisplayName(), e.dateValidation(),
                e.commentaireValideur(), e.justificatifFileId());
    }
}
