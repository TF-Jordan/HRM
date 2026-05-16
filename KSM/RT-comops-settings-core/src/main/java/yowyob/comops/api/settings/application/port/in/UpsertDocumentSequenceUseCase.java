package yowyob.comops.api.settings.application.port.in;

import yowyob.comops.api.settings.domain.model.DocumentSequence;
import reactor.core.publisher.Mono;

public interface UpsertDocumentSequenceUseCase {

    Mono<DocumentSequence> upsert(UpsertDocumentSequenceCommand command);
}
