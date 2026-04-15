package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionInvoiceUploadStore {

    private final AccountingExtensionInvoiceUploadRepository repository;

    public AccountingExtensionInvoiceUploadStore(AccountingExtensionInvoiceUploadRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.InvoiceUpload> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.InvoiceUpload upload) {
        return repository.save(toEntity(upload)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionInvoiceUploadEntity toEntity(AccountingBookkeepingService.InvoiceUpload upload) {
        return new AccountingExtensionInvoiceUploadEntity(
                upload.id(),
                upload.organizationId(),
                upload.filename(),
                upload.contentType(),
                upload.sizeBytes(),
                upload.createdAt());
    }

    private AccountingBookkeepingService.InvoiceUpload toDomain(AccountingExtensionInvoiceUploadEntity entity) {
        return new AccountingBookkeepingService.InvoiceUpload(
                entity.id(),
                entity.organizationId(),
                entity.filename(),
                entity.contentType(),
                entity.sizeBytes(),
                entity.createdAt());
    }
}
