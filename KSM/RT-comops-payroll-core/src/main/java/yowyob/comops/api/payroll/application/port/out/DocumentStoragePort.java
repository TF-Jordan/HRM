package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Outbound port to store a generated document and get back its file id. Implemented by a bootstrap
 * adapter delegating to file-core, so payroll-core never references a file-core type.
 */
public interface DocumentStoragePort {

    Mono<UUID> store(String fileName, String contentType, byte[] content);
}
