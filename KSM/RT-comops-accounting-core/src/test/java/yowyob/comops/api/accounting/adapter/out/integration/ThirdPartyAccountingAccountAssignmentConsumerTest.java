package yowyob.comops.api.accounting.adapter.out.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.common.domain.model.PartyRef;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.kernel.domain.model.OutboxEvent;
import yowyob.comops.api.kernel.domain.model.OutboxEventStatus;
import yowyob.comops.api.tp.application.port.in.AssignThirdPartyAccountingAccountsUseCase;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.domain.model.ThirdParty;

class ThirdPartyAccountingAccountAssignmentConsumerTest {

    @Test
    void assignsNextCustomerAccountWhenThirdPartyHasNoAccountingAccount() {
        UUID tenantId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID thirdPartyId = UUID.randomUUID();
        ThirdParty target = thirdParty(thirdPartyId, tenantId, organizationId, Set.of("CUSTOMER"), null, null);
        ThirdParty existing = thirdParty(UUID.randomUUID(), tenantId, organizationId, Set.of("CUSTOMER"), "411100",
                List.of("411100", "411101"));

        StubRepository repository = new StubRepository(target, List.of(existing));
        StubAssignUseCase assignUseCase = new StubAssignUseCase(target);
        ThirdPartyAccountingAccountAssignmentConsumer consumer =
                new ThirdPartyAccountingAccountAssignmentConsumer(repository, assignUseCase);

        consumer.consume(event(tenantId, organizationId, thirdPartyId, "THIRD_PARTY_CREATED")).block();

        assertThat(assignUseCase.calls).isEqualTo(1);
        assertThat(assignUseCase.assignedAccounts).containsExactly("411102");
        assertThat(assignUseCase.assignedThirdPartyId).isEqualTo(thirdPartyId);
    }

    private static ThirdParty thirdParty(UUID id, UUID tenantId, UUID organizationId, Set<String> roles,
            String accountingAccount, List<String> accountingAccountNumbers) {
        return ThirdParty.rehydrate(id, tenantId, Instant.now(), Instant.now(), organizationId,
                new PartyRef(PartyType.ORGANIZATION, UUID.randomUUID()), "tp-" + id.toString().substring(0, 8),
                "Third Party", roles, false, accountingAccount, "A", 50, true, null, null, null, null,
                "COMPANY", "SARL", null, null, "Third Party", null, null, null, null, accountingAccountNumbers,
                List.of(), null, null, false, null, null, null, null, "GENERAL", "A", null, 0, 0, 0, null);
    }

    private static OutboxEvent event(UUID tenantId, UUID organizationId, UUID aggregateId, String eventType) {
        Instant now = Instant.now();
        return OutboxEvent.rehydrate(UUID.randomUUID(), tenantId, now, now, organizationId, eventType, "THIRD_PARTY",
                aggregateId, now, Map.of(), OutboxEventStatus.PENDING, 0, null, now, null, null, null);
    }

    private static final class StubRepository implements ThirdPartyRepository {

        private final ThirdParty target;
        private final List<ThirdParty> organizationThirdParties;

        private StubRepository(ThirdParty target, List<ThirdParty> organizationThirdParties) {
            this.target = target;
            this.organizationThirdParties = new ArrayList<>(organizationThirdParties);
        }

        @Override
        public Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceCode) {
            return Mono.just(false);
        }

        @Override
        public Mono<Boolean> existsByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount,
                UUID excludedThirdPartyId) {
            return Mono.just(false);
        }

        @Override
        public Mono<ThirdParty> findById(UUID tenantId, UUID thirdPartyId) {
            return target.id().equals(thirdPartyId) ? Mono.just(target) : Mono.empty();
        }

        @Override
        public Mono<ThirdParty> findByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount) {
            return Mono.empty();
        }

        @Override
        public Mono<ThirdParty> findByPartyRef(UUID tenantId, UUID organizationId, PartyType partyType, UUID partyId) {
            return Mono.empty();
        }

        @Override
        public Flux<ThirdParty> findByOrganizationId(UUID tenantId, UUID organizationId) {
            return Flux.fromIterable(organizationThirdParties);
        }

        @Override
        public Mono<ThirdParty> save(ThirdParty thirdParty) {
            return Mono.just(thirdParty);
        }

        @Override
        public Mono<Void> deleteById(UUID tenantId, UUID thirdPartyId) {
            return Mono.empty();
        }
    }

    private static final class StubAssignUseCase implements AssignThirdPartyAccountingAccountsUseCase {

        private final ThirdParty result;
        private int calls;
        private UUID assignedThirdPartyId;
        private List<String> assignedAccounts = List.of();

        private StubAssignUseCase(ThirdParty result) {
            this.result = result;
        }

        @Override
        public Mono<ThirdParty> assignAccountingAccounts(UUID tenantId, UUID organizationId, UUID thirdPartyId,
                List<String> accountingAccountNumbers) {
            this.calls++;
            this.assignedThirdPartyId = thirdPartyId;
            this.assignedAccounts = List.copyOf(accountingAccountNumbers);
            return Mono.just(result);
        }
    }
}
