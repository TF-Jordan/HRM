package yowyob.comops.api.accounting.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AccountingJournalSpringDataRepository extends ReactiveCrudRepository<AccountingJournalEntity, UUID> {

    Flux<AccountingJournalEntity> findAllByTenantIdAndOrganizationIdOrderByCodeAsc(UUID tenantId, UUID organizationId);
}
