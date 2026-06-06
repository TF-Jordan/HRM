package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.payroll.application.port.in.CreateLookupTableCommand;
import yowyob.comops.api.payroll.application.port.in.ManageLookupTableUseCase;
import yowyob.comops.api.payroll.application.port.out.LookupTableRepository;
import yowyob.comops.api.payroll.domain.model.LookupTable;

import java.util.UUID;

/**
 * CRUD for the configurable stepped forfait scales (RAV, TDL…). The tenant is resolved from the
 * request context so every scale is created and read within the caller's tenant boundary.
 */
@Service
@Profile("!test-memory")
public class LookupTableService implements ManageLookupTableUseCase {

    private final LookupTableRepository repository;

    public LookupTableService(LookupTableRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LookupTable> createTable(CreateLookupTableCommand c) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx -> {
            LookupTable table = LookupTable.create(ctx.tenantId(), c.code(), c.label(),
                    c.countryCode(), c.effectiveFrom(), c.effectiveTo(), c.entries());
            return repository.save(table);
        });
    }

    @Override
    public Mono<LookupTable> deactivateTable(UUID tableId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), tableId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Lookup table not found")))
                        .map(LookupTable::deactivate)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<LookupTable> activateTable(UUID tableId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), tableId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Lookup table not found")))
                        .map(LookupTable::activate)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<LookupTable> getTable(UUID tableId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), tableId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Lookup table not found"))));
    }

    @Override
    public Flux<LookupTable> listTables(String countryCode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByCountry(ctx.tenantId(), countryCode));
    }
}
