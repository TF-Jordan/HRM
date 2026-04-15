package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.InvoiceSettlementRepository;
import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class InvoiceSettlementR2dbcRepositoryAdapter implements InvoiceSettlementRepository {

    private final InvoiceSettlementSpringDataRepository repository;

    public InvoiceSettlementR2dbcRepositoryAdapter(InvoiceSettlementSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsBySettlementNumber(UUID tenantId, UUID organizationId, String settlementNumber) {
        return repository.existsByTenantIdAndOrganizationIdAndSettlementNumberIgnoreCase(tenantId, organizationId,
                settlementNumber);
    }

    @Override
    public Mono<InvoiceSettlement> findById(UUID settlementId) {
        return repository.findById(settlementId).map(this::toDomain);
    }

    @Override
    public Flux<InvoiceSettlement> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<InvoiceSettlement> save(InvoiceSettlement settlement) {
        InvoiceSettlementEntity entity = new InvoiceSettlementEntity(settlement.id(), settlement.tenantId(),
                settlement.createdAt(), settlement.updatedAt(), settlement.organizationId(), settlement.bankAccountId(),
                settlement.invoiceId(), settlement.settlementNumber(), settlement.paymentMethod(), settlement.amount(),
                settlement.currency(), settlement.status());
        return repository.save(entity).map(this::toDomain);
    }

    private InvoiceSettlement toDomain(InvoiceSettlementEntity entity) {
        return InvoiceSettlement.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.bankAccountId(), entity.invoiceId(), entity.settlementNumber(),
                entity.paymentMethod(), entity.amount(), entity.currency(), entity.status());
    }
}
