package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.application.port.in.CreateInvoiceCommand;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceFromSalesOrderUseCase;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceLineCommand;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.DeleteInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.GetInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.ListInvoicesUseCase;
import yowyob.comops.api.accounting.application.port.in.PostInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.UpdateInvoiceCommand;
import yowyob.comops.api.accounting.application.port.in.UpdateInvoiceUseCase;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/accounting/invoices")
public class AccountingController {

    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final UpdateInvoiceUseCase updateInvoiceUseCase;
    private final DeleteInvoiceUseCase deleteInvoiceUseCase;
    private final CreateInvoiceFromSalesOrderUseCase createInvoiceFromSalesOrderUseCase;
    private final GetInvoiceUseCase getInvoiceUseCase;
    private final ListInvoicesUseCase listInvoicesUseCase;
    private final PostInvoiceUseCase postInvoiceUseCase;

    public AccountingController(CreateInvoiceUseCase createInvoiceUseCase,
            UpdateInvoiceUseCase updateInvoiceUseCase,
            DeleteInvoiceUseCase deleteInvoiceUseCase,
            CreateInvoiceFromSalesOrderUseCase createInvoiceFromSalesOrderUseCase,
            GetInvoiceUseCase getInvoiceUseCase,
            ListInvoicesUseCase listInvoicesUseCase,
            PostInvoiceUseCase postInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.updateInvoiceUseCase = updateInvoiceUseCase;
        this.deleteInvoiceUseCase = deleteInvoiceUseCase;
        this.createInvoiceFromSalesOrderUseCase = createInvoiceFromSalesOrderUseCase;
        this.getInvoiceUseCase = getInvoiceUseCase;
        this.listInvoicesUseCase = listInvoicesUseCase;
        this.postInvoiceUseCase = postInvoiceUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<ApiResponse<InvoiceResponse>>> createInvoice(
            @Valid @RequestBody Mono<CreateInvoiceRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createInvoiceUseCase.createInvoice(new CreateInvoiceCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().customerThirdPartyId(),
                        tuple.getT1().orderId(),
                        tuple.getT1().invoiceNumber(),
                        toCommands(tuple.getT1()),
                        tuple.getT1().currency())))
                .map(InvoiceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Invoice created.")));
    }

    @PatchMapping("/{invoiceId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<ApiResponse<InvoiceResponse>>> updateInvoice(
            @PathVariable("invoiceId") java.util.UUID invoiceId,
            @Valid @RequestBody Mono<CreateInvoiceRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateInvoiceUseCase.updateInvoice(new UpdateInvoiceCommand(
                        tuple.getT2().tenantId(),
                        invoiceId,
                        tuple.getT1().organizationId(),
                        tuple.getT1().customerThirdPartyId(),
                        tuple.getT1().orderId(),
                        tuple.getT1().invoiceNumber(),
                        toCommands(tuple.getT1()),
                        tuple.getT1().currency())))
                .map(InvoiceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Invoice updated.")));
    }

    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteInvoice(@PathVariable("invoiceId") java.util.UUID invoiceId) {
        return deleteInvoiceUseCase.deleteInvoice(invoiceId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted.")));
    }

    @PostMapping("/from-orders/{orderId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<ApiResponse<InvoiceResponse>>> createInvoiceFromSalesOrder(
            @PathVariable("orderId") java.util.UUID orderId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> createInvoiceFromSalesOrderUseCase.createFromSalesOrder(context.tenantId(), orderId))
                .map(InvoiceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Invoice created from sales order.")));
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<ResponseEntity<ApiResponse<InvoiceResponse>>> getInvoice(@PathVariable("invoiceId") java.util.UUID invoiceId) {
        return getInvoiceUseCase.getInvoice(invoiceId)
                .map(InvoiceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Invoice fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<InvoiceResponse>>>> listInvoices(@RequestParam("organizationId") java.util.UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> listInvoicesUseCase.listInvoices(context.tenantId(), organizationId))
                .map(InvoiceResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Invoices fetched.")));
    }

    @PostMapping({"/{invoiceId}/post", "/{invoiceId}/validate"})
    @PreAuthorize("@businessAccessPolicy.canPostInvoice(authentication)")
    public Mono<ResponseEntity<ApiResponse<InvoiceResponse>>> postInvoice(@PathVariable("invoiceId") java.util.UUID invoiceId) {
        return postInvoiceUseCase.post(invoiceId)
                .map(InvoiceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Invoice posted.")));
    }

    private List<CreateInvoiceLineCommand> toCommands(CreateInvoiceRequest request) {
        if (request.lines() != null && !request.lines().isEmpty()) {
            return request.lines().stream()
                    .map(line -> new CreateInvoiceLineCommand(line.productId(), line.quantity(), line.unitPrice()))
                    .toList();
        }
        if (request.productId() == null || request.quantity() == null || request.unitPrice() == null) {
            throw new IllegalArgumentException("legacy invoice payload requires productId, quantity and unitPrice");
        }
        return List.of(new CreateInvoiceLineCommand(request.productId(), request.quantity(), request.unitPrice()));
    }
}
