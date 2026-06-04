package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.LookupTableRepository;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.LookupTableEntry;

import java.util.List;
import java.util.UUID;

/**
 * Persists a {@link LookupTable} as a parent row plus child entry rows, replacing entries
 * wholesale on save (delete-by-table then insert).
 */
@Component
@Profile("r2dbc")
public class LookupTableR2dbcRepositoryAdapter implements LookupTableRepository {

    private final LookupTableSpringDataRepository tableRepository;
    private final LookupEntrySpringDataRepository entryRepository;

    public LookupTableR2dbcRepositoryAdapter(LookupTableSpringDataRepository tableRepository,
                                             LookupEntrySpringDataRepository entryRepository) {
        this.tableRepository = tableRepository;
        this.entryRepository = entryRepository;
    }

    @Override
    public Mono<LookupTable> save(LookupTable table) {
        List<LookupEntryEntity> entries = table.entries().stream()
                .map(e -> new LookupEntryEntity(UUID.randomUUID(), table.tenantId(), table.id(),
                        e.ordre(), e.lowerBound(), e.upperBound(), e.amount()))
                .toList();
        return tableRepository.save(toEntity(table))
                .then(entryRepository.deleteAllByTenantIdAndTableId(table.tenantId(), table.id()))
                .thenMany(entryRepository.saveAll(entries))
                .then(Mono.just(table));
    }

    @Override
    public Mono<LookupTable> findById(UUID tenantId, UUID id) {
        return tableRepository.findByIdAndTenantId(id, tenantId).flatMap(this::assemble);
    }

    @Override
    public Mono<LookupTable> findByCode(UUID tenantId, String code) {
        return tableRepository.findByTenantIdAndCode(tenantId, code).flatMap(this::assemble);
    }

    @Override
    public Flux<LookupTable> findByCountry(UUID tenantId, String countryCode) {
        return tableRepository.findAllByTenantIdAndCountryCode(tenantId, countryCode).flatMap(this::assemble);
    }

    private Mono<LookupTable> assemble(LookupTableEntity parent) {
        return entryRepository
                .findAllByTenantIdAndTableIdOrderByOrdre(parent.tenantId(), parent.id())
                .map(e -> new LookupTableEntry(e.ordre(), e.lowerBound(), e.upperBound(), e.amount()))
                .collectList()
                .map(entries -> LookupTable.rehydrate(parent.id(), parent.tenantId(), parent.createdAt(),
                        parent.updatedAt(), parent.code(), parent.label(), parent.countryCode(),
                        parent.effectiveFrom(), parent.effectiveTo(), parent.active(), entries));
    }

    private LookupTableEntity toEntity(LookupTable t) {
        return new LookupTableEntity(t.id(), t.tenantId(), t.createdAt(), t.updatedAt(), t.code(),
                t.label(), t.countryCode(), t.effectiveFrom(), t.effectiveTo(), t.active());
    }
}
