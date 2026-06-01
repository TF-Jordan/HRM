package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionInvoiceAccountingStore {

    private final AccountingExtensionInvoiceAccountingRepository repository;

    public AccountingExtensionInvoiceAccountingStore(AccountingExtensionInvoiceAccountingRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.InvoiceAccounting> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.InvoiceAccounting invoiceAccounting) {
        return repository.save(toEntity(invoiceAccounting)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionInvoiceAccountingEntity toEntity(AccountingBookkeepingService.InvoiceAccounting invoiceAccounting) {
        return new AccountingExtensionInvoiceAccountingEntity(
                invoiceAccounting.id(),
                invoiceAccounting.organizationId(),
                invoiceAccounting.invoiceId(),
                invoiceAccounting.customerThirdPartyId(),
                invoiceAccounting.customerAccountingAccount(),
                invoiceAccounting.accountingStatus(),
                invoiceAccounting.createdAt());
    }

    private AccountingBookkeepingService.InvoiceAccounting toDomain(AccountingExtensionInvoiceAccountingEntity entity) {
        return new AccountingBookkeepingService.InvoiceAccounting(
                entity.id(),
                entity.organizationId(),
                entity.invoiceId(),
                entity.customerThirdPartyId(),
                entity.customerAccountingAccount(),
                entity.accountingStatus(),
                entity.createdAt());
    }
}
