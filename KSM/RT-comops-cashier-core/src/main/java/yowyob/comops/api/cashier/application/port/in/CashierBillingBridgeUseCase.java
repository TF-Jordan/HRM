package yowyob.comops.api.cashier.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;
import yowyob.comops.api.cashier.web.CashierRequests;
import yowyob.comops.api.cashier.web.CashierViews;

public interface CashierBillingBridgeUseCase {

    Mono<CashierViews.BillView> importAccountingInvoice(UUID invoiceId, CashierBridgeContext context);

    Mono<CashierViews.BillView> payBill(UUID billId, CashierRequests.PayBillRequest request,
            CashierBridgeContext context);

    Mono<CashierViews.BillView> syncLinkedService(UUID billId, CashierBridgeContext context);
}
