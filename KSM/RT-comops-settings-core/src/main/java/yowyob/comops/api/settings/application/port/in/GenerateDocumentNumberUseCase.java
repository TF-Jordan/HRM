package yowyob.comops.api.settings.application.port.in;

import reactor.core.publisher.Mono;

public interface GenerateDocumentNumberUseCase {

    Mono<String> generate(GenerateDocumentNumberCommand command);
}
