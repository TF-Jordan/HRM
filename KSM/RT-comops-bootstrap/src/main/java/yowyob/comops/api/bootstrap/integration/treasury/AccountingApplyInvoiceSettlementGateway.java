package yowyob.comops.api.bootstrap.integration.treasury;

import yowyob.comops.api.accounting.application.port.in.ApplyInvoiceSettlementCommand;
import yowyob.comops.api.accounting.application.port.in.ApplyInvoiceSettlementUseCase;
import yowyob.comops.api.treasury.application.port.out.ApplyInvoiceSettlementGateway;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountingApplyInvoiceSettlementGateway implements ApplyInvoiceSettlementGateway {

    private final ApplyInvoiceSettlementUseCase applyInvoiceSettlementUseCase;

    public AccountingApplyInvoiceSettlementGateway(ApplyInvoiceSettlementUseCase applyInvoiceSettlementUseCase) {
        this.applyInvoiceSettlementUseCase = applyInvoiceSettlementUseCase;
    }

    @Override
    public Mono<Void> apply(UUID invoiceId, String settlementNumber, BigDecimal amount) {
        return applyInvoiceSettlementUseCase.apply(new ApplyInvoiceSettlementCommand(invoiceId, settlementNumber, amount))
                .then();
    }
}
