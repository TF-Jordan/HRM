package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayElementRepository;
import yowyob.comops.api.payroll.domain.model.CalculationMethod;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class PayElementR2dbcRepositoryAdapter implements PayElementRepository {

    private final PayElementSpringDataRepository repository;

    public PayElementR2dbcRepositoryAdapter(PayElementSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayElement> save(PayElement element) {
        return repository.save(toEntity(element)).map(this::toDomain);
    }

    @Override
    public Mono<PayElement> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<PayElement> findByCode(UUID tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, code).map(this::toDomain);
    }

    @Override
    public Flux<PayElement> findByCountry(UUID tenantId, String countryCode) {
        return repository.findAllByTenantIdAndCountryCodeOrderByDisplayOrder(tenantId, countryCode)
                .map(this::toDomain);
    }

    @Override
    public Flux<PayElement> findActiveByCountry(UUID tenantId, String countryCode) {
        return repository.findAllByTenantIdAndCountryCodeAndActiveOrderByDisplayOrder(
                tenantId, countryCode, true).map(this::toDomain);
    }

    private PayElementEntity toEntity(PayElement e) {
        return new PayElementEntity(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.code(), e.label(),
                e.category().name(), e.method().name(), e.baseReference(), e.rate(), e.ceiling(), e.floor(),
                e.exemptionThreshold(), e.flatAmount(), e.bracketTableCode(), e.lookupTableCode(),
                e.taxable(), e.socialContributable(), e.countryCode(), e.displayOrder(), e.active(),
                e.effectiveFrom(), e.effectiveTo());
    }

    private PayElement toDomain(PayElementEntity e) {
        return PayElement.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.code(), e.label(),
                PayElementCategory.valueOf(e.category()), CalculationMethod.valueOf(e.method()),
                e.baseReference(), e.rate(), e.ceiling(), e.floorValue(), e.exemptionThreshold(),
                e.flatAmount(), e.bracketTableCode(), e.lookupTableCode(), e.taxable(),
                e.socialContributable(), e.countryCode(), e.displayOrder(), e.active(),
                e.effectiveFrom(), e.effectiveTo());
    }
}
