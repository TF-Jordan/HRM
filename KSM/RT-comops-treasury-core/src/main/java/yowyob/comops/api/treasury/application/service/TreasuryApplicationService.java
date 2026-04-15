package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.kernel.application.port.out.CanonicalBankAccountAllocation;
import yowyob.comops.api.kernel.application.port.out.CanonicalBankAccountProvisioner;
import yowyob.comops.api.treasury.application.port.in.GetBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankAccountsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.DuplicateBankAccountException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TreasuryApplicationService
        implements RegisterBankAccountUseCase, GetBankAccountUseCase, ListBankAccountsUseCase, CanonicalBankAccountProvisioner {

    private final BankAccountRepository bankAccountRepository;

    public TreasuryApplicationService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public Mono<BankAccount> register(RegisterBankAccountCommand command) {
        Objects.requireNonNull(command, "command is required");
        BankAccount bankAccount = BankAccount.rehydrate(
                UUID.randomUUID(),
                command.tenantId(),
                java.time.Instant.now(),
                java.time.Instant.now(),
                command.organizationId(),
                command.bankThirdPartyId(),
                command.ownerThirdPartyId(),
                command.bankName(),
                command.accountNumber(),
                command.iban(),
                command.currency(),
                "ACTIVE");
        return bankAccountRepository.existsByAccountNumber(bankAccount.tenantId(), bankAccount.organizationId(), bankAccount.accountNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateBankAccountException(bankAccount.accountNumber()))
                        : bankAccountRepository.save(bankAccount));
    }

    @Override
    public Mono<CanonicalBankAccountAllocation> ensureBankAccount(
            UUID tenantId,
            UUID organizationId,
            UUID ownerThirdPartyId,
            String purpose,
            String currency) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(organizationId, "organizationId is required");
        Objects.requireNonNull(ownerThirdPartyId, "ownerThirdPartyId is required");
        return bankAccountRepository.findByOwnerThirdPartyId(tenantId, organizationId, ownerThirdPartyId)
                .switchIfEmpty(bankAccountRepository.save(
                        BankAccount.provision(tenantId, organizationId, ownerThirdPartyId, purpose, currency)))
                .map(this::toAllocation);
    }

    @Override
    public Mono<BankAccount> getBankAccount(UUID bankAccountId) {
        return bankAccountRepository.findById(bankAccountId)
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(bankAccountId)));
    }

    @Override
    public Flux<BankAccount> listBankAccounts(UUID tenantId, UUID organizationId) {
        return bankAccountRepository.findByOrganizationId(tenantId, organizationId);
    }

    private CanonicalBankAccountAllocation toAllocation(BankAccount bankAccount) {
        return new CanonicalBankAccountAllocation(
                bankAccount.id(),
                bankAccount.organizationId(),
                bankAccount.ownerThirdPartyId(),
                bankAccount.bankName(),
                bankAccount.accountNumber(),
                bankAccount.iban(),
                bankAccount.currency(),
                bankAccount.status());
    }
}
