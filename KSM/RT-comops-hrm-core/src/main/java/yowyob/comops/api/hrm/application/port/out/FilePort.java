package yowyob.comops.api.hrm.application.port.out;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface FilePort {

    Mono<UUID> storeDocument(String fileName, String contentType, long size, byte[] content);

    Mono<Boolean> exists(UUID fileId);
}
