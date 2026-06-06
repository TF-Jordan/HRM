package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.payroll.application.port.in.CreatePayElementCommand;
import yowyob.comops.api.payroll.application.port.in.ManagePayElementUseCase;
import yowyob.comops.api.payroll.application.port.out.PayElementRepository;
import yowyob.comops.api.payroll.domain.model.PayElement;

import java.util.UUID;

/**
 * CRUD for the configurable pay-element catalogue. The tenant is resolved from the request
 * context so every element is created and read within the caller's tenant boundary.
 */
@Service
@Profile("!test-memory")
public class PayElementService implements ManagePayElementUseCase {

    private final PayElementRepository repository;

    public PayElementService(PayElementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayElement> createPayElement(CreatePayElementCommand c) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx -> {
            PayElement element = PayElement.create(ctx.tenantId(), c.code(), c.label(), c.category(),
                    c.method(), c.baseReference(), c.rate(), c.ceiling(), c.floor(), c.exemptionThreshold(),
                    c.flatAmount(), c.bracketTableCode(), c.lookupTableCode(), c.taxable(),
                    c.socialContributable(), c.countryCode(), c.displayOrder(), c.effectiveFrom(),
                    c.effectiveTo());
            return repository.save(element);
        });
    }

    @Override
    public Mono<PayElement> deactivatePayElement(UUID payElementId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), payElementId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Pay element not found")))
                        .map(PayElement::deactivate)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<PayElement> activatePayElement(UUID payElementId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), payElementId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Pay element not found")))
                        .map(PayElement::activate)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<PayElement> getPayElement(UUID payElementId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), payElementId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Pay element not found"))));
    }

    @Override
    public Flux<PayElement> listPayElements(String countryCode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByCountry(ctx.tenantId(), countryCode));
    }
}
