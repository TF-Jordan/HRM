package yowyob.comops.api.accounting.application.port.in;

import java.util.List;
import java.util.UUID;

public record CreateInvoiceCommand(
        UUID tenantId,
        UUID organizationId,
        UUID customerThirdPartyId,
        UUID orderId,
        String invoiceNumber,
        List<CreateInvoiceLineCommand> lines,
        String currency) {
}
