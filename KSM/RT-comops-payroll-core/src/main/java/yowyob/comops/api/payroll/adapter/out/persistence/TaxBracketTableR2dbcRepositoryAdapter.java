package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.TaxBracketTableRepository;
import yowyob.comops.api.payroll.domain.model.TaxBracket;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.util.List;
import java.util.UUID;

/**
 * Persists a {@link TaxBracketTable} as a parent row plus child bracket rows. On save the
 * brackets are replaced wholesale (delete-by-table then insert) — config tables are small and
 * edited as a unit, so this keeps the mapping simple and consistent.
 */
@Component
@Profile("r2dbc")
public class TaxBracketTableR2dbcRepositoryAdapter implements TaxBracketTableRepository {

    private final TaxBracketTableSpringDataRepository tableRepository;
    private final TaxBracketSpringDataRepository bracketRepository;

    public TaxBracketTableR2dbcRepositoryAdapter(TaxBracketTableSpringDataRepository tableRepository,
                                                 TaxBracketSpringDataRepository bracketRepository) {
        this.tableRepository = tableRepository;
        this.bracketRepository = bracketRepository;
    }

    @Override
    public Mono<TaxBracketTable> save(TaxBracketTable table) {
        List<TaxBracketEntity> brackets = table.brackets().stream()
                .map(b -> new TaxBracketEntity(UUID.randomUUID(), table.tenantId(), table.id(),
                        b.ordre(), b.lowerBound(), b.upperBound(), b.rate()))
                .toList();
        return tableRepository.save(toEntity(table))
                .then(bracketRepository.deleteAllByTenantIdAndTableId(table.tenantId(), table.id()))
                .thenMany(bracketRepository.saveAll(brackets))
                .then(Mono.just(table));
    }

    @Override
    public Mono<TaxBracketTable> findById(UUID tenantId, UUID id) {
        return tableRepository.findByIdAndTenantId(id, tenantId).flatMap(this::assemble);
    }

    @Override
    public Mono<TaxBracketTable> findByCode(UUID tenantId, String code) {
        return tableRepository.findByTenantIdAndCode(tenantId, code).flatMap(this::assemble);
    }

    @Override
    public Flux<TaxBracketTable> findByCountry(UUID tenantId, String countryCode) {
        return tableRepository.findAllByTenantIdAndCountryCode(tenantId, countryCode).flatMap(this::assemble);
    }

    private Mono<TaxBracketTable> assemble(TaxBracketTableEntity parent) {
        return bracketRepository
                .findAllByTenantIdAndTableIdOrderByOrdre(parent.tenantId(), parent.id())
                .map(b -> new TaxBracket(b.ordre(), b.lowerBound(), b.upperBound(), b.rate()))
                .collectList()
                .map(brackets -> TaxBracketTable.rehydrate(parent.id(), parent.tenantId(),
                        parent.createdAt(), parent.updatedAt(), parent.code(), parent.label(),
                        parent.countryCode(), parent.effectiveFrom(), parent.effectiveTo(),
                        parent.active(), brackets));
    }

    private TaxBracketTableEntity toEntity(TaxBracketTable t) {
        return new TaxBracketTableEntity(t.id(), t.tenantId(), t.createdAt(), t.updatedAt(), t.code(),
                t.label(), t.countryCode(), t.effectiveFrom(), t.effectiveTo(), t.active());
    }
}
