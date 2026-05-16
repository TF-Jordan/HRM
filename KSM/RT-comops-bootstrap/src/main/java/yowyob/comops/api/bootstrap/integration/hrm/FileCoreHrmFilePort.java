package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.file.application.port.in.GetStoredFileUseCase;
import yowyob.comops.api.file.application.port.in.StoreFileCommand;
import yowyob.comops.api.file.application.port.in.StoreFileUseCase;
import yowyob.comops.api.hrm.application.port.out.FilePort;

import java.util.UUID;

import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FileCoreHrmFilePort implements FilePort {

    private final StoreFileUseCase storeFileUseCase;
    private final GetStoredFileUseCase getStoredFileUseCase;

    public FileCoreHrmFilePort(StoreFileUseCase storeFileUseCase, GetStoredFileUseCase getStoredFileUseCase) {
        this.storeFileUseCase = storeFileUseCase;
        this.getStoredFileUseCase = getStoredFileUseCase;
    }

    @Override
    public Mono<UUID> storeDocument(String fileName, String contentType, long size, byte[] content) {
        var dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(content);
        var command = new StoreFileCommand(fileName, contentType, size, Flux.just(dataBuffer));
        return storeFileUseCase.store(command).map(storedFile -> storedFile.id());
    }

    @Override
    public Mono<Boolean> exists(UUID fileId) {
        return getStoredFileUseCase.getMetadata(fileId)
                .map(storedFile -> true)
                .defaultIfEmpty(false);
    }
}
