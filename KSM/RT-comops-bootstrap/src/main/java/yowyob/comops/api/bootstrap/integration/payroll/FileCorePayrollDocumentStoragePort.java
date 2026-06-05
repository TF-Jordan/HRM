package yowyob.comops.api.bootstrap.integration.payroll;

import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.file.application.port.in.StoreFileCommand;
import yowyob.comops.api.file.application.port.in.StoreFileUseCase;
import yowyob.comops.api.payroll.application.port.out.DocumentStoragePort;

import java.util.UUID;

/**
 * Bridges payroll-core's {@link DocumentStoragePort} onto file-core, storing generated document
 * bytes and returning the stored file id. payroll-core never references a file-core type.
 */
@Component
public class FileCorePayrollDocumentStoragePort implements DocumentStoragePort {

    private final StoreFileUseCase storeFileUseCase;

    public FileCorePayrollDocumentStoragePort(StoreFileUseCase storeFileUseCase) {
        this.storeFileUseCase = storeFileUseCase;
    }

    @Override
    public Mono<UUID> store(String fileName, String contentType, byte[] content) {
        var buffer = DefaultDataBufferFactory.sharedInstance.wrap(content);
        var command = new StoreFileCommand(fileName, contentType, content.length, Flux.just(buffer));
        return storeFileUseCase.store(command).map(stored -> stored.id());
    }
}
