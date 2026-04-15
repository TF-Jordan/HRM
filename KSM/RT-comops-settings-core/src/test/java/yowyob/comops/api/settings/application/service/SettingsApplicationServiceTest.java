package yowyob.comops.api.settings.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.settings.adapter.out.persistence.InMemoryAppBusinessSettingsRepository;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsCommand;
import yowyob.comops.api.settings.application.port.out.DocumentSequenceRepository;
import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class SettingsApplicationServiceTest {

    private final SettingsApplicationService service = new SettingsApplicationService(new NoOpDocumentSequenceRepository(),
            new InMemoryAppBusinessSettingsRepository());

    @Test
    void resolvesAgencyOverrideBeforeOrganizationDefault() {
        UUID tenantId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        service.updateSettings(new UpdateAppBusinessSettingsCommand(tenantId, organizationId, null, false, false, false,
                0, true, "A4", 10, "FAC-", false, false, "XAF", "Savhel HQ", "M021", false, false)).block();

        service.updateSettings(new UpdateAppBusinessSettingsCommand(tenantId, organizationId, agencyId, true, true, true,
                12.5d, true, "A5", 12, "AG-", true, true, "EUR", "Savhel Agency", "M099", true, true)).block();

        var resolved = service.getSettings(tenantId, organizationId, agencyId).block();
        var global = service.getSettings(tenantId, organizationId, null).block();

        assertThat(resolved.defaultCurrency()).isEqualTo("EUR");
        assertThat(resolved.requireSalesOrderApproval()).isTrue();
        assertThat(global.defaultCurrency()).isEqualTo("XAF");
        assertThat(global.requireSalesOrderApproval()).isFalse();
    }

    private static final class NoOpDocumentSequenceRepository implements DocumentSequenceRepository {
        @Override
        public Mono<DocumentSequence> save(DocumentSequence documentSequence) {
            return Mono.just(documentSequence);
        }

        @Override
        public Mono<DocumentSequence> findByScopeAndType(UUID tenantId, UUID organizationId, UUID agencyId,
                String documentType) {
            return Mono.empty();
        }

        @Override
        public Flux<DocumentSequence> findAllByScope(UUID tenantId, UUID organizationId, UUID agencyId) {
            return Flux.empty();
        }
    }
}
