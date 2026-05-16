package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.CheckPaymentRepository;
import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class CheckPaymentR2dbcRepositoryAdapter implements CheckPaymentRepository {

    private final CheckPaymentSpringDataRepository repository;

    public CheckPaymentR2dbcRepositoryAdapter(CheckPaymentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<CheckPayment> findById(UUID checkPaymentId) {
        return repository.findById(checkPaymentId).map(this::toDomain);
    }

    @Override
    public Flux<CheckPayment> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Flux<CheckPayment> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return repository.findAllByTenantIdAndBankAccountId(tenantId, bankAccountId)
                .map(this::toDomain);
    }

    @Override
    public Mono<CheckPayment> save(CheckPayment checkPayment) {
        CheckPaymentEntity entity = new CheckPaymentEntity(checkPayment.id(), checkPayment.tenantId(),
                checkPayment.createdAt(), checkPayment.updatedAt(), checkPayment.organizationId(),
                checkPayment.bankAccountId(), checkPayment.checkNumber(), checkPayment.amount(),
                checkPayment.beneficiary(), checkPayment.status());
        return repository.save(entity).map(this::toDomain);
    }

    private CheckPayment toDomain(CheckPaymentEntity entity) {
        return CheckPayment.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.bankAccountId(), entity.checkNumber(), entity.amount(),
                entity.beneficiary(), entity.status());
    }
}
