package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Organization;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListMyOrganizationsUseCase {

    Flux<Organization> listMine(UUID tenantId, UUID userId);
}
