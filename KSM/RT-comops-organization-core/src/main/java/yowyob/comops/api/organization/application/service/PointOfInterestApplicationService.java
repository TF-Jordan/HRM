package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.application.port.in.CreatePointOfInterestCommand;
import yowyob.comops.api.organization.application.port.in.CreatePointOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.LinkAgencyToPointOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.ListAllPointsOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.ListPointsOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.UnlinkAgencyFromPointOfInterestUseCase;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.PointOfInterestLinkRepository;
import yowyob.comops.api.organization.application.port.out.PointOfInterestRepository;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.DuplicatePointOfInterestException;
import yowyob.comops.api.organization.domain.model.PointOfInterest;
import yowyob.comops.api.organization.domain.model.PointOfInterestLink;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class PointOfInterestApplicationService implements CreatePointOfInterestUseCase, ListPointsOfInterestUseCase,
        ListAllPointsOfInterestUseCase, LinkAgencyToPointOfInterestUseCase, UnlinkAgencyFromPointOfInterestUseCase {

    private final PointOfInterestRepository pointOfInterestRepository;
    private final PointOfInterestLinkRepository pointOfInterestLinkRepository;
    private final AgencyRepository agencyRepository;

    public PointOfInterestApplicationService(PointOfInterestRepository pointOfInterestRepository,
            PointOfInterestLinkRepository pointOfInterestLinkRepository,
            AgencyRepository agencyRepository) {
        this.pointOfInterestRepository = pointOfInterestRepository;
        this.pointOfInterestLinkRepository = pointOfInterestLinkRepository;
        this.agencyRepository = agencyRepository;
    }

    @Override
    public Mono<PointOfInterest> create(CreatePointOfInterestCommand command) {
        Objects.requireNonNull(command, "command is required");
        return validateAgencyScope(command.tenantId(), command.organizationId(), command.agencyId())
                .then(Mono.defer(() -> {
                    PointOfInterest pointOfInterest = PointOfInterest.create(command.tenantId(), command.organizationId(),
                            command.agencyId(), command.name(), command.poiType(), command.latitude(),
                            command.longitude());
                    return pointOfInterestRepository.existsByAgencyAndName(command.tenantId(), command.organizationId(),
                                    command.agencyId(), pointOfInterest.name())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicatePointOfInterestException(pointOfInterest.name()))
                                    : pointOfInterestRepository.save(pointOfInterest));
                }));
    }

    @Override
    public Flux<PointOfInterest> listByAgency(java.util.UUID organizationId, java.util.UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> pointOfInterestRepository.findByAgencyId(context.tenantId(), organizationId, agencyId));
    }

    @Override
    public Flux<PointOfInterest> listAll(java.util.UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> pointOfInterestRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<PointOfInterestLink> link(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID agencyId,
            java.util.UUID pointOfInterestId, Integer distanceMeters, String description) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .then(pointOfInterestRepository.findById(tenantId, pointOfInterestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("point of interest not found")))
                        .flatMap(poi -> {
                            if (!poi.organizationId().equals(organizationId)) {
                                return Mono.error(new IllegalArgumentException(
                                        "point of interest does not belong to the organization"));
                            }
                            return pointOfInterestLinkRepository.existsByAgencyIdAndPointOfInterestId(tenantId, agencyId,
                                            pointOfInterestId)
                                    .flatMap(exists -> exists
                                            ? Mono.error(new IllegalArgumentException("point of interest link already exists"))
                                            : pointOfInterestLinkRepository.save(PointOfInterestLink.create(tenantId,
                                                    organizationId, agencyId, pointOfInterestId, distanceMeters,
                                                    description)));
                        }));
    }

    @Override
    public Mono<Void> unlink(java.util.UUID tenantId, java.util.UUID agencyId, java.util.UUID pointOfInterestId) {
        return pointOfInterestLinkRepository.deleteByAgencyIdAndPointOfInterestId(tenantId, agencyId, pointOfInterestId);
    }

    private Mono<Void> validateAgencyScope(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID agencyId) {
        return agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                .flatMap(agency -> {
                    if (!agency.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                    }
                    return Mono.empty();
                });
    }
}
