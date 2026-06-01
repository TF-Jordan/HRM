package yowyob.comops.api.bootstrap.integration.treasury;

import yowyob.comops.api.accounting.application.port.in.GetPostedInvoiceSnapshotUseCase;
import yowyob.comops.api.treasury.application.port.out.PostedInvoiceSettlementSource;
import yowyob.comops.api.treasury.application.port.out.PostedInvoiceSettlementSourceProvider;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountingPostedInvoiceSettlementSourceProvider implements PostedInvoiceSettlementSourceProvider {

    private final GetPostedInvoiceSnapshotUseCase getPostedInvoiceSnapshotUseCase;

    public AccountingPostedInvoiceSettlementSourceProvider(GetPostedInvoiceSnapshotUseCase getPostedInvoiceSnapshotUseCase) {
        this.getPostedInvoiceSnapshotUseCase = getPostedInvoiceSnapshotUseCase;
    }

    @Override
    public Mono<PostedInvoiceSettlementSource> getPostedInvoice(UUID invoiceId) {
        return getPostedInvoiceSnapshotUseCase.getPostedInvoice(invoiceId)
                .map(snapshot -> new PostedInvoiceSettlementSource(
                        snapshot.invoiceId(),
                        snapshot.tenantId(),
                        snapshot.organizationId(),
                        snapshot.customerThirdPartyId(),
                        snapshot.invoiceNumber(),
                        snapshot.currency(),
                        snapshot.paymentStatus(),
                        snapshot.totalAmount(),
                        snapshot.settledAmount(),
                        snapshot.outstandingAmount()));
    }
}
