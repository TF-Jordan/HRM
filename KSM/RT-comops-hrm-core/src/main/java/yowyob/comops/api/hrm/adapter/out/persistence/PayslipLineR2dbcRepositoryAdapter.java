package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PayslipLineRepository;
import yowyob.comops.api.hrm.domain.model.PayslipLine;
import yowyob.comops.api.hrm.domain.model.PayslipLineType;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PayslipLineR2dbcRepositoryAdapter implements PayslipLineRepository {

    private final PayslipLineSpringDataRepository repository;

    public PayslipLineR2dbcRepositoryAdapter(PayslipLineSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayslipLine> save(PayslipLine line) {
        return repository.save(toEntity(line)).map(this::toDomain);
    }

    @Override
    public Flux<PayslipLine> findByPayrollEntryId(UUID tenantId, UUID payrollEntryId) {
        return repository.findAllByTenantIdAndPayrollEntryId(tenantId, payrollEntryId).map(this::toDomain);
    }

    private PayslipLineEntity toEntity(PayslipLine l) {
        return new PayslipLineEntity(l.id(), l.tenantId(), l.payrollEntryId(), l.libelle(),
                l.type().name(), l.base(), l.taux(), l.montant(), l.ordreAffichage());
    }

    private PayslipLine toDomain(PayslipLineEntity e) {
        return new PayslipLine(e.id(), e.tenantId(), e.payrollEntryId(), e.libelle(),
                PayslipLineType.valueOf(e.type()), e.base(), e.taux(), e.montant(), e.ordreAffichage());
    }
}
