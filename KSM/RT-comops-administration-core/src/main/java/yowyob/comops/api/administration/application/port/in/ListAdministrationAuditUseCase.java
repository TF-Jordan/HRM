package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListAdministrationAuditUseCase {
    Flux<AdminAuditEntry> listAudit(UUID tenantId, int limit);
}
