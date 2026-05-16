package yowyob.comops.api.settings.application.port.in;

import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListDocumentSequencesUseCase {

    Flux<DocumentSequence> list(UUID tenantId, UUID organizationId, UUID agencyId);
}
