package yowyob.comops.api.kernel.application.port.in;

import yowyob.comops.api.kernel.domain.model.SystemAuditEntry;
import reactor.core.publisher.Flux;

public interface ListSystemAuditUseCase {
    Flux<SystemAuditEntry> listCurrentUserActivity(int limit);
    Flux<SystemAuditEntry> listCurrentOrganizationActivity(int limit);
}
