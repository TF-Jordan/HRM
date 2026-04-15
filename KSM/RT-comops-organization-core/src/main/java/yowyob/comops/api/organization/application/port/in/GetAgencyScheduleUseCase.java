package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.application.service.AgencyScheduleView;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetAgencyScheduleUseCase {

    Mono<AgencyScheduleView> getSchedule(UUID organizationId, UUID agencyId);
}
