package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.payroll.application.port.in.CreateTaxBracketTableCommand;
import yowyob.comops.api.payroll.application.port.in.ManageTaxBracketUseCase;
import yowyob.comops.api.payroll.application.port.out.TaxBracketTableRepository;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.util.UUID;

/**
 * CRUD for the configurable progressive tax scales. The tenant is resolved from the request
 * context so every scale is created and read within the caller's tenant boundary.
 */
@Service
@Profile("!test-memory")
public class TaxBracketTableService implements ManageTaxBracketUseCase {

    private final TaxBracketTableRepository repository;

    public TaxBracketTableService(TaxBracketTableRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<TaxBracketTable> createTable(CreateTaxBracketTableCommand c) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx -> {
            TaxBracketTable table = TaxBracketTable.create(ctx.tenantId(), c.code(), c.label(),
                    c.countryCode(), c.effectiveFrom(), c.effectiveTo(), c.brackets());
            return repository.save(table);
        });
    }

    @Override
    public Mono<TaxBracketTable> deactivateTable(UUID tableId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), tableId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Tax bracket table not found")))
                        .map(TaxBracketTable::deactivate)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<TaxBracketTable> activateTable(UUID tableId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), tableId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Tax bracket table not found")))
                        .map(TaxBracketTable::activate)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<TaxBracketTable> getTable(UUID tableId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), tableId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Tax bracket table not found"))));
    }

    @Override
    public Flux<TaxBracketTable> listTables(String countryCode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByCountry(ctx.tenantId(), countryCode));
    }
}
