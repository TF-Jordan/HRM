package yowyob.comops.api.treasury.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ApplyInvoiceSettlementGateway {

    Mono<Void> apply(UUID invoiceId, String settlementNumber, BigDecimal amount);
}
