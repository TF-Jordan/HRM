package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.application.port.in.CreatePhysicalSpaceCommand;
import yowyob.comops.api.organization.application.port.in.CreatePhysicalSpaceUseCase;
import yowyob.comops.api.organization.application.port.in.ListPhysicalSpacesUseCase;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.DuplicatePhysicalSpaceCodeException;
import yowyob.comops.api.organization.domain.PhysicalSpaceNotFoundException;
import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PhysicalSpaceApplicationService implements CreatePhysicalSpaceUseCase, ListPhysicalSpacesUseCase {

    private final PhysicalSpaceRepository physicalSpaceRepository;
    private final AgencyRepository agencyRepository;

    public PhysicalSpaceApplicationService(PhysicalSpaceRepository physicalSpaceRepository,
            AgencyRepository agencyRepository) {
        this.physicalSpaceRepository = physicalSpaceRepository;
        this.agencyRepository = agencyRepository;
    }

    @Override
    public Mono<PhysicalSpace> create(CreatePhysicalSpaceCommand command) {
        Objects.requireNonNull(command, "command is required");
        return validateAgencyScope(command.tenantId(), command.organizationId(), command.agencyId())
                .then(validateParentScope(command.tenantId(), command.organizationId(), command.agencyId(),
                        command.parentSpaceId()))
                .then(Mono.defer(() -> {
                    PhysicalSpace physicalSpace = PhysicalSpace.create(command.tenantId(), command.organizationId(),
                            command.agencyId(), command.parentSpaceId(), command.code(), command.name(),
                            command.spaceType(), command.description(), command.levelNumber(), command.capacity(),
                            command.active());
                    return physicalSpaceRepository.existsByAgencyAndCode(command.tenantId(), command.organizationId(),
                                    command.agencyId(), physicalSpace.code())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicatePhysicalSpaceCodeException(physicalSpace.code()))
                                    : physicalSpaceRepository.save(physicalSpace));
                }));
    }

    @Override
    public Flux<PhysicalSpace> listByAgency(UUID tenantId, UUID organizationId, UUID agencyId) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .thenMany(physicalSpaceRepository.findByAgencyId(tenantId, organizationId, agencyId)
                        .sort(Comparator.comparing((PhysicalSpace space) -> space.parentSpaceId() == null ? 0 : 1)
                                .thenComparing(space -> space.levelNumber() == null ? Integer.MAX_VALUE : space.levelNumber())
                                .thenComparing(PhysicalSpace::code, String.CASE_INSENSITIVE_ORDER)));
    }

    @Override
    public Flux<PhysicalSpace> listByOrganization(UUID tenantId, UUID organizationId) {
        return physicalSpaceRepository.findByOrganizationId(tenantId, organizationId)
                .sort(Comparator.comparing(PhysicalSpace::agencyId)
                        .thenComparing(space -> space.parentSpaceId() == null ? 0 : 1)
                        .thenComparing(space -> space.levelNumber() == null ? Integer.MAX_VALUE : space.levelNumber())
                        .thenComparing(PhysicalSpace::code, String.CASE_INSENSITIVE_ORDER));
    }

    private Mono<Void> validateAgencyScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                .flatMap(agency -> {
                    if (!agency.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateParentScope(UUID tenantId, UUID organizationId, UUID agencyId, UUID parentSpaceId) {
        if (parentSpaceId == null) {
            return Mono.empty();
        }
        return physicalSpaceRepository.findById(tenantId, parentSpaceId)
                .switchIfEmpty(Mono.error(new PhysicalSpaceNotFoundException(parentSpaceId)))
                .flatMap(parent -> {
                    if (!parent.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException(
                                "parent physical space does not belong to the organization"));
                    }
                    if (!parent.agencyId().equals(agencyId)) {
                        return Mono.error(new IllegalArgumentException(
                                "parent physical space does not belong to the agency"));
                    }
                    return Mono.empty();
                });
    }
}
