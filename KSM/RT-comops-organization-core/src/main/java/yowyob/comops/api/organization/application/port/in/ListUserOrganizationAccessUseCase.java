package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListUserOrganizationAccessUseCase {

    Flux<UserOrganizationAccessView> listUserOrganizationAccess(UUID tenantId, UUID userId);
}
