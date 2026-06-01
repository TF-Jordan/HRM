package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineSummaryView(
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount) {
}
