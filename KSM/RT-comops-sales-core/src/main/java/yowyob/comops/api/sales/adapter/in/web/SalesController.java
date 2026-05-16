package yowyob.comops.api.sales.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.sales.application.port.in.CancelSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.ConfirmSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.CreateSalesOrderCommand;
import yowyob.comops.api.sales.application.port.in.CreateSalesOrderLineCommand;
import yowyob.comops.api.sales.application.port.in.CreateSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.DeleteSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.GetSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.ListSalesOrdersUseCase;
import yowyob.comops.api.sales.application.port.in.UpdateSalesOrderCommand;
import yowyob.comops.api.sales.application.port.in.UpdateSalesOrderUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/sales/orders")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'sales:write')")
public class SalesController {

    private final CreateSalesOrderUseCase createSalesOrderUseCase;
    private final UpdateSalesOrderUseCase updateSalesOrderUseCase;
    private final ConfirmSalesOrderUseCase confirmSalesOrderUseCase;
    private final CancelSalesOrderUseCase cancelSalesOrderUseCase;
    private final DeleteSalesOrderUseCase deleteSalesOrderUseCase;
    private final GetSalesOrderUseCase getSalesOrderUseCase;
    private final ListSalesOrdersUseCase listSalesOrdersUseCase;

    public SalesController(CreateSalesOrderUseCase createSalesOrderUseCase,
            UpdateSalesOrderUseCase updateSalesOrderUseCase,
            ConfirmSalesOrderUseCase confirmSalesOrderUseCase,
            CancelSalesOrderUseCase cancelSalesOrderUseCase,
            DeleteSalesOrderUseCase deleteSalesOrderUseCase,
            GetSalesOrderUseCase getSalesOrderUseCase,
            ListSalesOrdersUseCase listSalesOrdersUseCase) {
        this.createSalesOrderUseCase = createSalesOrderUseCase;
        this.updateSalesOrderUseCase = updateSalesOrderUseCase;
        this.confirmSalesOrderUseCase = confirmSalesOrderUseCase;
        this.cancelSalesOrderUseCase = cancelSalesOrderUseCase;
        this.deleteSalesOrderUseCase = deleteSalesOrderUseCase;
        this.getSalesOrderUseCase = getSalesOrderUseCase;
        this.listSalesOrdersUseCase = listSalesOrdersUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<SalesOrderResponse>>> createOrder(
            @Valid @RequestBody Mono<CreateSalesOrderRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createSalesOrderUseCase.createOrder(new CreateSalesOrderCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(),
                        tuple.getT1().customerThirdPartyId(),
                        tuple.getT1().productId(),
                        tuple.getT1().orderNumber(),
                        tuple.getT1().quantity(),
                        tuple.getT1().unitPrice(),
                        tuple.getT1().currency(),
                        tuple.getT1().lines() == null ? null : tuple.getT1().lines().stream()
                                .map(line -> new CreateSalesOrderLineCommand(line.productId(), line.quantity(),
                                        line.unitPrice()))
                                .toList())))
                .map(SalesOrderResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Sales order created.")));
    }

    @PatchMapping("/{orderId}")
    public Mono<ResponseEntity<ApiResponse<SalesOrderResponse>>> updateOrder(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody Mono<CreateSalesOrderRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateSalesOrderUseCase.updateOrder(new UpdateSalesOrderCommand(
                        tuple.getT2().tenantId(),
                        orderId,
                        tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(),
                        tuple.getT1().customerThirdPartyId(),
                        tuple.getT1().orderNumber(),
                        tuple.getT1().productId(),
                        tuple.getT1().quantity(),
                        tuple.getT1().unitPrice(),
                        tuple.getT1().currency(),
                        tuple.getT1().lines() == null ? null : tuple.getT1().lines().stream()
                                .map(line -> new CreateSalesOrderLineCommand(line.productId(), line.quantity(),
                                        line.unitPrice()))
                                .toList())))
                .map(SalesOrderResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Sales order updated.")));
    }

    @PostMapping("/{orderId}/confirm")
    public Mono<ResponseEntity<ApiResponse<SalesOrderResponse>>> confirmOrder(@PathVariable("orderId") UUID orderId) {
        return confirmSalesOrderUseCase.confirm(orderId)
                .map(SalesOrderResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Sales order confirmed.")));
    }

    @PostMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<ApiResponse<SalesOrderResponse>>> cancelOrder(@PathVariable("orderId") UUID orderId) {
        return cancelSalesOrderUseCase.cancel(orderId)
                .map(SalesOrderResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Sales order cancelled.")));
    }

    @DeleteMapping("/{orderId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteOrder(@PathVariable("orderId") UUID orderId) {
        return deleteSalesOrderUseCase.delete(orderId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Sales order deleted.")));
    }

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<ApiResponse<SalesOrderResponse>>> getOrder(@PathVariable("orderId") UUID orderId) {
        return getSalesOrderUseCase.getById(orderId)
                .map(SalesOrderResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Sales order retrieved.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<SalesOrderResponse>>>> listOrders(
            @RequestParam("organizationId") UUID organizationId) {
        return listSalesOrdersUseCase.listByOrganization(organizationId)
                .map(SalesOrderResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Sales orders retrieved.")));
    }
}
