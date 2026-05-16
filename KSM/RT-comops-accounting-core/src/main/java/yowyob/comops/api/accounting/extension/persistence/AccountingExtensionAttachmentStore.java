package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingOperationsService;

@Component
public class AccountingExtensionAttachmentStore {

    private final AccountingExtensionAttachmentRepository repository;

    public AccountingExtensionAttachmentStore(AccountingExtensionAttachmentRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingOperationsService.Attachment> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingOperationsService.Attachment attachment) {
        return repository.save(toEntity(attachment)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionAttachmentEntity toEntity(AccountingOperationsService.Attachment attachment) {
        return new AccountingExtensionAttachmentEntity(
                attachment.id(),
                attachment.organizationId(),
                attachment.targetType(),
                attachment.targetId(),
                attachment.filename(),
                attachment.contentType(),
                attachment.sizeBytes(),
                attachment.content(),
                attachment.createdAt());
    }

    private AccountingOperationsService.Attachment toDomain(AccountingExtensionAttachmentEntity entity) {
        return new AccountingOperationsService.Attachment(
                entity.id(),
                entity.organizationId(),
                entity.targetType(),
                entity.targetId(),
                entity.filename(),
                entity.contentType(),
                entity.sizeBytes(),
                entity.content(),
                entity.createdAt());
    }
}
