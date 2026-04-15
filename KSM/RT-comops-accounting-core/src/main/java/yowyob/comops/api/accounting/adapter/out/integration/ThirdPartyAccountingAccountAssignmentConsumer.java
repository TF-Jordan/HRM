package yowyob.comops.api.accounting.adapter.out.integration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.port.out.BusinessEventConsumer;
import yowyob.comops.api.kernel.domain.model.OutboxEvent;
import yowyob.comops.api.tp.application.port.in.AssignThirdPartyAccountingAccountsUseCase;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.domain.model.ThirdParty;

@Component
public class ThirdPartyAccountingAccountAssignmentConsumer implements BusinessEventConsumer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "THIRD_PARTY_CREATED",
            "THIRD_PARTY_CONVERTED_TO_CUSTOMER");

    private final ThirdPartyRepository thirdPartyRepository;
    private final AssignThirdPartyAccountingAccountsUseCase assignThirdPartyAccountingAccountsUseCase;

    public ThirdPartyAccountingAccountAssignmentConsumer(
            ThirdPartyRepository thirdPartyRepository,
            AssignThirdPartyAccountingAccountsUseCase assignThirdPartyAccountingAccountsUseCase) {
        this.thirdPartyRepository = thirdPartyRepository;
        this.assignThirdPartyAccountingAccountsUseCase = assignThirdPartyAccountingAccountsUseCase;
    }

    @Override
    public boolean supports(OutboxEvent event) {
        return "THIRD_PARTY".equals(event.aggregateType()) && SUPPORTED_EVENT_TYPES.contains(event.eventType());
    }

    @Override
    public Mono<Void> consume(OutboxEvent event) {
        return thirdPartyRepository.findById(event.tenantId(), event.aggregateId())
                .flatMap(thirdParty -> {
                    if (thirdParty.accountingAccount() != null && !thirdParty.accountingAccount().isBlank()) {
                        return Mono.empty();
                    }
                    return thirdPartyRepository.findByOrganizationId(event.tenantId(), thirdParty.organizationId())
                            .collectList()
                            .flatMap(thirdParties -> assignThirdPartyAccountingAccountsUseCase.assignAccountingAccounts(
                                    event.tenantId(),
                                    thirdParty.organizationId(),
                                    thirdParty.id(),
                                    generateAccountingAccounts(thirdParty, thirdParties)))
                            .then();
                })
                .then();
    }

    private List<String> generateAccountingAccounts(ThirdParty thirdParty, List<ThirdParty> organizationThirdParties) {
        LinkedHashSet<String> prefixes = resolvePrefixes(thirdParty.roles());
        LinkedHashSet<String> knownAccounts = organizationThirdParties.stream()
                .filter(existing -> !existing.id().equals(thirdParty.id()))
                .flatMap(existing -> existing.accountingAccountNumbers().stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<String> generatedAccounts = new ArrayList<>();
        for (String prefix : prefixes) {
            long nextSuffix = nextSuffix(prefix, knownAccounts);
            String generated = prefix + nextSuffix;
            knownAccounts.add(generated);
            generatedAccounts.add(generated);
        }
        return generatedAccounts;
    }

    private LinkedHashSet<String> resolvePrefixes(Set<String> roles) {
        LinkedHashSet<String> prefixes = new LinkedHashSet<>();
        if (roles.contains("CUSTOMER")) {
            prefixes.add("411");
        }
        if (roles.contains("SUPPLIER")) {
            prefixes.add("401");
        }
        if (roles.contains("EMPLOYEE") || roles.contains("BENEFICIARY") || roles.contains("PAYEE")) {
            prefixes.add("421");
        }
        if (prefixes.isEmpty()) {
            prefixes.add("471");
        }
        return prefixes;
    }

    private long nextSuffix(String prefix, Set<String> knownAccounts) {
        long max = knownAccounts.stream()
                .filter(account -> account.startsWith(prefix))
                .map(account -> account.substring(prefix.length()))
                .filter(suffix -> !suffix.isBlank() && suffix.chars().allMatch(Character::isDigit))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(99L);
        long candidate = max + 1L;
        while (knownAccounts.contains(prefix + candidate)) {
            candidate++;
        }
        return candidate;
    }
}
