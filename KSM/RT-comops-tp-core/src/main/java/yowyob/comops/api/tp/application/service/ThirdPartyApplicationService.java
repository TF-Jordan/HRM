package yowyob.comops.api.tp.application.service;

import yowyob.comops.api.common.domain.model.PartyRef;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.kernel.application.port.in.RecordSystemAuditUseCase;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.CanonicalBankAccountProvisioner;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.tp.application.port.in.AddThirdPartyBankAccountCommand;
import yowyob.comops.api.tp.application.port.in.AssignThirdPartyAccountingAccountsUseCase;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.DeleteThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileCommand;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyStatisticsUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.LookupThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyBankAccountsUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyLifecycleUseCase;
import yowyob.comops.api.tp.application.port.in.SearchThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.out.ThirdPartyBankAccountRepository;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.application.port.out.ThirdPartySearchGateway;
import yowyob.comops.api.tp.domain.DuplicateThirdPartyReferenceException;
import yowyob.comops.api.tp.domain.ProspectConversionNotAllowedException;
import yowyob.comops.api.tp.domain.ThirdPartyAccountingAccountAlreadyExistsException;
import yowyob.comops.api.tp.domain.ThirdPartyBankAccountAlreadyExistsException;
import yowyob.comops.api.tp.domain.ThirdPartyBankAccountNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartyLookupNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartyNotFoundException;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import yowyob.comops.api.tp.domain.model.ThirdPartySearchResult;
import yowyob.comops.api.tp.domain.model.ThirdPartyStatistics;
import yowyob.comops.api.file.application.port.out.StoredFileRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ThirdPartyApplicationService implements CreateThirdPartyUseCase, GetThirdPartyUseCase,
                ListThirdPartiesUseCase, SearchThirdPartiesUseCase, UpdateThirdPartyUseCase, DeleteThirdPartyUseCase,
                ManageThirdPartyBankAccountsUseCase, LookupThirdPartyUseCase, ManageThirdPartyLifecycleUseCase,
                GetThirdPartyStatisticsUseCase, AssignThirdPartyAccountingAccountsUseCase,
                EnsureActorFinancialProfileUseCase {

        private static final Logger log = LoggerFactory.getLogger(ThirdPartyApplicationService.class);

        private static final Set<String> AUTO_MANAGED_SEGMENTS = Set.of("NEW", "QUALIFIED", "HOT");

        private final ThirdPartyRepository thirdPartyRepository;
        private final ThirdPartyBankAccountRepository thirdPartyBankAccountRepository;
        private final Optional<ThirdPartySearchGateway> thirdPartySearchGateway;
        private final BusinessEventPublisher businessEventPublisher;
        private final ReactiveTransactionalExecutor transactionalExecutor;
        private final RecordSystemAuditUseCase recordSystemAuditUseCase;
        private final StoredFileRepository storedFileRepository;
        private final Optional<CanonicalBankAccountProvisioner> canonicalBankAccountProvisioner;

        public ThirdPartyApplicationService(ThirdPartyRepository thirdPartyRepository,
                        ThirdPartyBankAccountRepository thirdPartyBankAccountRepository,
                        BusinessEventPublisher businessEventPublisher,
                        ReactiveTransactionalExecutor transactionalExecutor,
                        Optional<ThirdPartySearchGateway> thirdPartySearchGateway,
                        RecordSystemAuditUseCase recordSystemAuditUseCase,
                        StoredFileRepository storedFileRepository,
                        Optional<CanonicalBankAccountProvisioner> canonicalBankAccountProvisioner) {
                this.thirdPartyRepository = thirdPartyRepository;
                this.thirdPartyBankAccountRepository = thirdPartyBankAccountRepository;
                this.businessEventPublisher = businessEventPublisher;
                this.transactionalExecutor = transactionalExecutor;
                this.thirdPartySearchGateway = thirdPartySearchGateway;
                this.recordSystemAuditUseCase = recordSystemAuditUseCase;
                this.storedFileRepository = storedFileRepository;
                this.canonicalBankAccountProvisioner = canonicalBankAccountProvisioner;
        }

        @Override
        public Mono<ThirdParty> createThirdParty(CreateThirdPartyCommand command) {
                Objects.requireNonNull(command, "command is required");
                ThirdParty thirdParty = ThirdParty.create(
                                command.tenantId(),
                                command.organizationId(),
                                new PartyRef(command.partyType(), command.partyId()),
                                command.code(),
                                command.name(),
                                command.roles(),
                                command.prospect(),
                                command.accountingAccount(),
                                command.segment(),
                                command.qualificationScore(),
                                command.enabled(),
                                command.type(),
                                command.legalForm(),
                                command.uniqueIdentificationNumber(),
                                command.tradeRegistrationNumber(),
                                command.name(),
                                command.acronym(),
                                command.longName(),
                                command.logoUri(),
                                command.logoId(),
                                command.accountingAccountNumbers(),
                                command.authorizedPaymentMethods(),
                                command.authorizedCreditLimit(),
                                command.maxDiscountRate(),
                                command.vatSubject() == null ? false : command.vatSubject(),
                                command.operationsBalance(),
                                command.openingBalance(),
                                command.payTermNumber(),
                                command.payTermType(),
                                command.thirdPartyFamily(),
                                command.classification(),
                                command.taxNumber());

                Mono<ThirdParty> operation = assertReferenceAvailable(thirdParty.tenantId(),
                                thirdParty.organizationId(),
                                thirdParty.referenceCode())
                                .then(validateLogoReference(thirdParty.tenantId(), thirdParty.logoId()))
                                .then(assertAccountingAccountAvailable(thirdParty.tenantId(),
                                                thirdParty.organizationId(),
                                                thirdParty.accountingAccount(), null))
                                .then(Mono.fromSupplier(() -> prepareNewThirdPartyForInsert(thirdParty, false)))
                                .flatMap(thirdPartyRepository::save)
                                .flatMap(saved -> ReactiveRequestContextHolder.getRequiredContext()
                                                .flatMap(context -> businessEventPublisher
                                                                .publish(thirdPartyEvent("THIRD_PARTY_CREATED", saved))
                                                                .then(recordSystemAuditUseCase.record(saved.tenantId(),
                                                                                saved.organizationId(),
                                                                                context.userId(), "THIRD_PARTY_CREATED",
                                                                                "THIRD_PARTY", saved.id().toString(),
                                                                                saved.referenceCode()))
                                                                .thenReturn(saved)));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> ensureActorFinancialProfile(EnsureActorFinancialProfileCommand command) {
                Objects.requireNonNull(command, "command is required");
                String normalizedRole = normalizeFinancialRole(command.role());
                String resolvedReference = resolveFinancialReferenceCode(command.referenceCode(), command.actorId(),
                                normalizedRole);
                String resolvedDisplayName = resolveFinancialDisplayName(command.displayName(), command.actorId());

                Mono<ThirdParty> operation = thirdPartyRepository
                                .findByPartyRef(command.tenantId(), command.organizationId(), PartyType.ACTOR,
                                                command.actorId())
                                .flatMap(existing -> mergeFinancialRole(existing, normalizedRole))
                                .switchIfEmpty(createActorFinancialCounterparty(command, normalizedRole, resolvedReference,
                                                resolvedDisplayName))
                                .flatMap(this::ensureAssignedAccountingAccounts)
                                .flatMap(thirdParty -> ensureCanonicalBankAccount(thirdParty, normalizedRole))
                                .flatMap(thirdParty -> ReactiveRequestContextHolder.getRequiredContext()
                                                .flatMap(context -> businessEventPublisher
                                                                .publish(thirdPartyEvent(
                                                                                "THIRD_PARTY_FINANCIAL_PROFILE_ENSURED",
                                                                                thirdParty))
                                                                .then(recordSystemAuditUseCase.record(
                                                                                thirdParty.tenantId(),
                                                                                thirdParty.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_FINANCIAL_PROFILE_ENSURED",
                                                                                "THIRD_PARTY",
                                                                                thirdParty.id().toString(),
                                                                                normalizedRole))
                                                                .thenReturn(thirdParty)));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> getThirdParty(UUID thirdPartyId) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId))
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(thirdPartyId)));
        }

        @Override
        public Flux<ThirdParty> listThirdParties(UUID organizationId, String role, Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMapMany(context -> thirdPartyRepository.findByOrganizationId(context.tenantId(),
                                                organizationId))
                                .filter(thirdParty -> role == null || role.isBlank() || thirdParty.hasRole(role))
                                .filter(thirdParty -> prospect == null || thirdParty.prospect() == prospect);
        }

        @Override
        public Flux<ThirdPartySearchResult> searchThirdParties(UUID organizationId, String query, String role,
                        Boolean prospect, String segment, Integer minimumQualificationScore, Boolean active,
                        String followUpStatus, int page, int size) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMapMany(context -> thirdPartySearchGateway
                                                .map(gateway -> gateway
                                                                .search(context.tenantId(), organizationId, query, role,
                                                                                prospect,
                                                                                segment, minimumQualificationScore,
                                                                                active, followUpStatus, page, size)
                                                                .onErrorResume(e -> {
                                                                        log.warn("Elasticsearch query failed, falling back to repository for organization {}",
                                                                                        organizationId, e);
                                                                        return Mono.empty();
                                                                })
                                                                .switchIfEmpty(Flux.defer(() -> {
                                                                        log.warn("Falling back to repository search: ES returned 0 results or failed for organization {}",
                                                                                        organizationId);
                                                                        return searchFromRepository(context.tenantId(),
                                                                                        organizationId, query, role,
                                                                                        prospect, segment,
                                                                                        minimumQualificationScore,
                                                                                        active, followUpStatus, page,
                                                                                        size);
                                                                })))
                                                .orElseGet(() -> searchFromRepository(context.tenantId(),
                                                                organizationId, query, role,
                                                                prospect, segment, minimumQualificationScore, active,
                                                                followUpStatus, page, size)));
        }

        @Override
        public Mono<ThirdParty> updateThirdParty(UpdateThirdPartyCommand command) {
                Objects.requireNonNull(command, "command is required");
                Mono<ThirdParty> operation = thirdPartyRepository.findById(command.tenantId(), command.thirdPartyId())
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(command.thirdPartyId())))
                                .flatMap(existing -> {
                                        ThirdParty updated = existing.update(command.referenceCode(),
                                                        command.displayName(), command.roles(),
                                                        command.prospect(), command.accountingAccount(),
                                                        command.segment(),
                                                        command.qualificationScore(), command.enabled(),
                                                        command.type(),
                                                        command.legalForm(),
                                                        command.uniqueIdentificationNumber(),
                                                        command.tradeRegistrationNumber(),
                                                        command.name(),
                                                        command.acronym(),
                                                        command.longName(),
                                                        command.logoUri(),
                                                        command.logoId(),
                                                        command.accountingAccountNumbers(),
                                                        command.authorizedPaymentMethods(),
                                                        command.authorizedCreditLimit(),
                                                        command.maxDiscountRate(),
                                                        command.vatSubject() == null ? false : command.vatSubject(),
                                                        command.operationsBalance(),
                                                        command.openingBalance(),
                                                        command.payTermNumber(),
                                                        command.payTermType(),
                                                        command.thirdPartyFamily(),
                                                        command.classification(),
                                                        command.taxNumber());
                                        Mono<Void> duplicateReferenceCheck = existing.referenceCode()
                                                        .equalsIgnoreCase(updated.referenceCode())
                                                                        ? Mono.empty()
                                                                        : assertReferenceAvailable(updated.tenantId(),
                                                                                        updated.organizationId(),
                                                                                        updated.referenceCode());
                                        return duplicateReferenceCheck
                                                        .then(validateLogoReference(updated.tenantId(), updated.logoId()))
                                                        .then(assertAccountingAccountAvailable(updated.tenantId(),
                                                                        updated.organizationId(),
                                                                        updated.accountingAccount(), updated.id()))
                                                        .then(thirdPartyBankAccountRepository
                                                                        .findByThirdPartyId(updated.tenantId(),
                                                                                        updated.id())
                                                                        .hasElements()
                                                                        .map(hasBankAccount -> applyQualificationPolicy(
                                                                                        updated, hasBankAccount,
                                                                                        false)))
                                                        .flatMap(thirdPartyRepository::save)
                                                        .flatMap(saved -> ReactiveRequestContextHolder
                                                                        .getRequiredContext()
                                                                        .flatMap(context -> businessEventPublisher
                                                                                        .publish(
                                                                                                        thirdPartyEvent("THIRD_PARTY_UPDATED",
                                                                                                                        saved))
                                                                                        .then(recordSystemAuditUseCase
                                                                                                        .record(saved.tenantId(),
                                                                                                                        saved.organizationId(),
                                                                                                                        context.userId(),
                                                                                                                        "THIRD_PARTY_UPDATED",
                                                                                                                        "THIRD_PARTY",
                                                                                                                        saved.id().toString(),
                                                                                                                        saved.referenceCode()))
                                                                                        .thenReturn(saved)));
                                });
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<Void> deleteThirdParty(UUID thirdPartyId) {
                Mono<Void> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .flatMap(bankAccount -> thirdPartyBankAccountRepository
                                                                                .deleteById(context.tenantId(),
                                                                                                bankAccount.id()))
                                                                .then(thirdPartyRepository.deleteById(
                                                                                context.tenantId(), existing.id()))
                                                                .then(businessEventPublisher.publish(thirdPartyEvent(
                                                                                "THIRD_PARTY_DELETED", existing)))
                                                                .then(recordSystemAuditUseCase.record(
                                                                                existing.tenantId(),
                                                                                existing.organizationId(),
                                                                                context.userId(), "THIRD_PARTY_DELETED",
                                                                                "THIRD_PARTY",
                                                                                existing.id().toString(),
                                                                                existing.referenceCode()))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Flux<ThirdPartyBankAccount> listBankAccounts(UUID thirdPartyId) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMapMany(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .thenMany(thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(), thirdPartyId)));
        }

        @Override
        public Mono<ThirdPartyBankAccount> addBankAccount(AddThirdPartyBankAccountCommand command) {
                Mono<ThirdPartyBankAccount> operation = thirdPartyRepository
                                .findById(command.tenantId(), command.thirdPartyId())
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(command.thirdPartyId())))
                                .flatMap(thirdParty -> thirdPartyBankAccountRepository.existsByIban(command.tenantId(),
                                                command.thirdPartyId(), command.iban())
                                                .flatMap(exists -> exists
                                                                ? Mono.error(new ThirdPartyBankAccountAlreadyExistsException(
                                                                                command.iban()))
                                                                : thirdPartyBankAccountRepository
                                                                                .findByThirdPartyId(command.tenantId(),
                                                                                                command.thirdPartyId())
                                                                                .flatMap(account -> thirdPartyBankAccountRepository
                                                                                                .save(command.primary()
                                                                                                                ? account.clearPrimary()
                                                                                                                : account))
                                                                                .then(thirdPartyBankAccountRepository
                                                                                                .save(ThirdPartyBankAccount
                                                                                                                .create(
                                                                                                                                command.tenantId(),
                                                                                                                                command.thirdPartyId(),
                                                                                                                                command.label(),
                                                                                                                                command.bankName(),
                                                                                                                                command.iban(),
                                                                                                                                command.swiftBic(),
                                                                                                                                command.currency(),
                                                                                                                                command.primary())))))
                                .flatMap(saved -> ReactiveRequestContextHolder.getRequiredContext()
                                                .flatMap(context -> businessEventPublisher.publish(BusinessEvent.now(
                                                                saved.tenantId(),
                                                                context.organizationId(),
                                                                "THIRD_PARTY_BANK_ACCOUNT_ADDED", "THIRD_PARTY",
                                                                saved.thirdPartyId(), payload(
                                                                                "iban", saved.iban(),
                                                                                "bankName", saved.bankName(),
                                                                                "currency", saved.currency(),
                                                                                "primary", saved.primary())))
                                                                .then(recordSystemAuditUseCase.record(saved.tenantId(),
                                                                                context.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_BANK_ACCOUNT_ADDED",
                                                                                "THIRD_PARTY",
                                                                                saved.thirdPartyId().toString(),
                                                                                saved.iban()))
                                                                .thenReturn(saved)));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<Void> removeBankAccount(UUID thirdPartyId, UUID bankAccountId) {
                Mono<Void> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .then(thirdPartyBankAccountRepository.findById(context.tenantId(),
                                                                bankAccountId))
                                                .filter(account -> account.thirdPartyId().equals(thirdPartyId))
                                                .switchIfEmpty(Mono.error(new ThirdPartyBankAccountNotFoundException(
                                                                bankAccountId)))
                                                .flatMap(account -> thirdPartyBankAccountRepository
                                                                .deleteById(context.tenantId(), account.id())
                                                                .then(recordSystemAuditUseCase.record(
                                                                                context.tenantId(),
                                                                                context.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_BANK_ACCOUNT_DELETED",
                                                                                "THIRD_PARTY",
                                                                                thirdPartyId.toString(),
                                                                                account.iban()))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<Void> setPrimaryBankAccount(UUID thirdPartyId, UUID bankAccountId) {
                Mono<Void> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .then(thirdPartyBankAccountRepository.findById(context.tenantId(),
                                                                bankAccountId))
                                                .filter(account -> account.thirdPartyId().equals(thirdPartyId))
                                                .switchIfEmpty(Mono.error(new ThirdPartyBankAccountNotFoundException(
                                                                bankAccountId)))
                                                .flatMap(target -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .flatMap(account -> thirdPartyBankAccountRepository
                                                                                .save(account.id().equals(target.id())
                                                                                                ? account.markPrimary()
                                                                                                : account.clearPrimary()))
                                                                .then(recordSystemAuditUseCase.record(
                                                                                context.tenantId(),
                                                                                context.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_BANK_ACCOUNT_PRIMARY_UPDATED",
                                                                                "THIRD_PARTY", thirdPartyId.toString(),
                                                                                target.iban()))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> definePrimaryBankAccount(UUID thirdPartyId, String iban) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(thirdParty -> thirdPartyBankAccountRepository
                                                                .findByIban(context.tenantId(), iban)
                                                                .filter(account -> account.thirdPartyId()
                                                                                .equals(thirdPartyId))
                                                                .flatMap(account -> setPrimaryBankAccount(thirdPartyId,
                                                                                account.id())
                                                                                .thenReturn(thirdParty))
                                                                .switchIfEmpty(addBankAccount(
                                                                                new AddThirdPartyBankAccountCommand(
                                                                                                context.tenantId(),
                                                                                                thirdPartyId, "PRIMARY",
                                                                                                "UNSPECIFIED", iban,
                                                                                                null, "XAF", true))
                                                                                .thenReturn(thirdParty))));
        }

        @Override
        public Mono<ThirdParty> findByBankAccountNumber(UUID organizationId, String bankAccountNumber, String role,
                        Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyBankAccountRepository
                                                .findByIban(context.tenantId(), bankAccountNumber)
                                                .flatMap(account -> thirdPartyRepository.findById(context.tenantId(),
                                                                account.thirdPartyId()))
                                                .filter(thirdParty -> thirdParty.organizationId()
                                                                .equals(organizationId))
                                                .filter(thirdParty -> role == null || role.isBlank()
                                                                || thirdParty.hasRole(role))
                                                .filter(thirdParty -> prospect == null
                                                                || thirdParty.prospect() == prospect)
                                                .switchIfEmpty(Mono.error(
                                                                new ThirdPartyLookupNotFoundException("bank account",
                                                                                bankAccountNumber))));
        }

        @Override
        public Mono<ThirdParty> findByAccountingAccount(UUID organizationId, String accountingAccount, String role,
                        Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository
                                                .findByAccountingAccount(context.tenantId(), organizationId,
                                                                accountingAccount)
                                                .filter(thirdParty -> role == null || role.isBlank()
                                                                || thirdParty.hasRole(role))
                                                .filter(thirdParty -> prospect == null
                                                                || thirdParty.prospect() == prospect)
                                                .switchIfEmpty(Mono.error(new ThirdPartyLookupNotFoundException(
                                                                "accounting account",
                                                                accountingAccount))));
        }

        @Override
        public Mono<ThirdParty> activateThirdParty(UUID thirdPartyId) {
                return mutateLifecycle(thirdPartyId, ThirdParty::activate, "THIRD_PARTY_ACTIVATED",
                                "THIRD_PARTY_ACTIVATED");
        }

        @Override
        public Mono<ThirdParty> deactivateThirdParty(UUID thirdPartyId) {
                return mutateLifecycle(thirdPartyId, ThirdParty::deactivate, "THIRD_PARTY_DEACTIVATED",
                                "THIRD_PARTY_DEACTIVATED");
        }

        @Override
        public Mono<ThirdParty> defineAccountingAccount(UUID thirdPartyId, String accountingAccount) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> assertAccountingAccountAvailable(
                                                                context.tenantId(),
                                                                existing.organizationId(), accountingAccount,
                                                                existing.id())
                                                                .then(thirdPartyBankAccountRepository
                                                                                .findByThirdPartyId(context.tenantId(),
                                                                                                thirdPartyId)
                                                                                .hasElements()
                                                                                .map(hasBankAccount -> applyQualificationPolicy(
                                                                                                existing.defineAccountingAccount(
                                                                                                                accountingAccount),
                                                                                                hasBankAccount,
                                                                                                false)))
                                                                .flatMap(thirdPartyRepository::save)
                                                                .flatMap(saved -> businessEventPublisher
                                                                                .publish(thirdPartyEvent(
                                                                                                "THIRD_PARTY_ACCOUNTING_ACCOUNT_UPDATED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_ACCOUNTING_ACCOUNT_UPDATED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .accountingAccount())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> assignAccountingAccounts(UUID tenantId, UUID organizationId, UUID thirdPartyId,
                        List<String> accountingAccountNumbers) {
                Objects.requireNonNull(tenantId, "tenantId is required");
                Objects.requireNonNull(organizationId, "organizationId is required");
                Objects.requireNonNull(thirdPartyId, "thirdPartyId is required");
                List<String> normalizedAccounts = normalizeAccountingAccounts(accountingAccountNumbers);
                Mono<ThirdParty> operation = thirdPartyRepository.findById(tenantId, thirdPartyId)
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(thirdPartyId)))
                                .flatMap(existing -> {
                                        if (!existing.organizationId().equals(organizationId)) {
                                                return Mono.error(new IllegalArgumentException(
                                                                "third party does not belong to the provided organization"));
                                        }
                                        if (existing.accountingAccountNumbers().equals(normalizedAccounts)) {
                                                return Mono.just(existing);
                                        }
                                        return reactor.core.publisher.Flux.fromIterable(normalizedAccounts)
                                                        .concatMap(account -> assertAccountingAccountAvailable(
                                                                        tenantId,
                                                                        organizationId,
                                                                        account,
                                                                        existing.id()))
                                                        .then(Mono.fromSupplier(() -> existing.assignAccountingAccounts(
                                                                        normalizedAccounts)))
                                                        .flatMap(thirdPartyRepository::save)
                                                        .flatMap(saved -> businessEventPublisher.publish(
                                                                        thirdPartyEvent(
                                                                                        "THIRD_PARTY_ACCOUNTING_ACCOUNT_UPDATED",
                                                                                        saved))
                                                                        .then(recordSystemAuditUseCase.record(
                                                                                        saved.tenantId(),
                                                                                        saved.organizationId(),
                                                                                        null,
                                                                                        "THIRD_PARTY_ACCOUNTING_ACCOUNT_ASSIGNED",
                                                                                        "THIRD_PARTY",
                                                                                        saved.id().toString(),
                                                                                        String.join(",",
                                                                                                        saved.accountingAccountNumbers())))
                                                                        .thenReturn(saved));
                                });
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> qualifyThirdParty(UUID thirdPartyId, String segment, Integer qualificationScore) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyRepository
                                                                .save(existing.qualify(segment, qualificationScore))
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_QUALIFIED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_QUALIFIED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                (saved.segment() == null
                                                                                                                ? "UNCLASSIFIED"
                                                                                                                : saved.segment())
                                                                                                                + ":"
                                                                                                                + saved.qualificationScore()))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> recomputeQualificationScore(UUID thirdPartyId) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .hasElements()
                                                                .map(hasBankAccount -> applyQualificationPolicy(
                                                                                existing, hasBankAccount, true))
                                                                .flatMap(thirdPartyRepository::save)
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_SCORE_RECOMPUTED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_SCORE_RECOMPUTED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .qualificationScore())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> scheduleFollowUp(UUID thirdPartyId, Instant nextFollowUpAt) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyRepository
                                                                .save(existing.scheduleFollowUp(nextFollowUpAt))
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_FOLLOW_UP_SCHEDULED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_FOLLOW_UP_SCHEDULED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .nextFollowUpAt())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> completeFollowUp(UUID thirdPartyId, Instant contactedAt, Instant nextFollowUpAt) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyRepository
                                                                .save(existing.completeFollowUp(contactedAt,
                                                                                nextFollowUpAt))
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_FOLLOW_UP_COMPLETED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_FOLLOW_UP_COMPLETED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .lastContactedAt())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> convertProspectToCustomer(UUID thirdPartyId) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> {
                                                        if (!existing.prospect() || !existing.active()) {
                                                                return Mono.error(
                                                                                new ProspectConversionNotAllowedException(
                                                                                                thirdPartyId));
                                                        }
                                                        return thirdPartyBankAccountRepository
                                                                        .findByThirdPartyId(context.tenantId(),
                                                                                        thirdPartyId)
                                                                        .hasElements()
                                                                        .map(hasBankAccount -> applyQualificationPolicy(
                                                                                        existing.convertToCustomer(),
                                                                                        hasBankAccount, true))
                                                                        .flatMap(thirdPartyRepository::save)
                                                                        .flatMap(saved -> businessEventPublisher
                                                                                        .publish(
                                                                                                        thirdPartyEvent("THIRD_PARTY_CONVERTED_TO_CUSTOMER",
                                                                                                                        saved))
                                                                                        .then(recordSystemAuditUseCase
                                                                                                        .record(saved.tenantId(),
                                                                                                                        saved.organizationId(),
                                                                                                                        context.userId(),
                                                                                                                        "THIRD_PARTY_CONVERTED_TO_CUSTOMER",
                                                                                                                        "THIRD_PARTY",
                                                                                                                        saved.id().toString(),
                                                                                                                        saved.referenceCode()))
                                                                                        .thenReturn(saved));
                                                }));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdPartyStatistics> getStatistics(UUID organizationId, String role, Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository
                                                .findByOrganizationId(context.tenantId(), organizationId)
                                                .filter(thirdParty -> role == null || role.isBlank()
                                                                || thirdParty.hasRole(role))
                                                .filter(thirdParty -> prospect == null
                                                                || thirdParty.prospect() == prospect)
                                                .collectList()
                                                .flatMap(thirdParties -> Flux.fromIterable(thirdParties)
                                                                .flatMap(thirdParty -> thirdPartyBankAccountRepository
                                                                                .findByThirdPartyId(context.tenantId(),
                                                                                                thirdParty.id())
                                                                                .hasElements()
                                                                                .map(hasBankAccount -> Map.entry(
                                                                                                thirdParty,
                                                                                                hasBankAccount)))
                                                                .collectList()
                                                                .map(entries -> buildStatistics(entries,
                                                                                thirdParties))));
        }

        @Override
        public Mono<Long> getProspectConversionCount(UUID organizationId) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository
                                                .findByOrganizationId(context.tenantId(), organizationId)
                                                .filter(thirdParty -> !thirdParty.prospect())
                                                .filter(thirdParty -> thirdParty.convertedAt() != null)
                                                .count());
        }

        private Mono<Void> assertReferenceAvailable(UUID tenantId, UUID organizationId, String referenceCode) {
                return thirdPartyRepository.existsByReference(tenantId, organizationId, referenceCode)
                                .flatMap(exists -> exists
                                                ? Mono.error(new DuplicateThirdPartyReferenceException(referenceCode))
                                                : Mono.empty());
        }

        private Mono<Void> assertAccountingAccountAvailable(UUID tenantId, UUID organizationId,
                        String accountingAccount,
                        UUID excludedThirdPartyId) {
                return thirdPartyRepository.existsByAccountingAccount(tenantId, organizationId, accountingAccount,
                                excludedThirdPartyId)
                                .flatMap(exists -> exists
                                                ? Mono.error(new ThirdPartyAccountingAccountAlreadyExistsException(
                                                                accountingAccount))
                                                : Mono.empty());
        }

        private Mono<Void> validateLogoReference(UUID tenantId, UUID logoId) {
                if (logoId == null) {
                        return Mono.empty();
                }
                return storedFileRepository.findById(tenantId, logoId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                "logoId does not reference an existing file")))
                                .then();
        }

        private Mono<ThirdParty> mutateLifecycle(UUID thirdPartyId, Function<ThirdParty, ThirdParty> mutator,
                        String eventType, String auditAction) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .hasElements()
                                                                .map(hasBankAccount -> applyQualificationPolicy(
                                                                                mutator.apply(existing),
                                                                                hasBankAccount, false))
                                                                .flatMap(thirdPartyRepository::save)
                                                                .flatMap(saved -> businessEventPublisher
                                                                                .publish(thirdPartyEvent(eventType,
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                auditAction,
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                saved.referenceCode()))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        private ThirdPartyStatistics buildStatistics(List<Map.Entry<ThirdParty, Boolean>> entries,
                        List<ThirdParty> thirdParties) {
                long activeCount = thirdParties.stream().filter(ThirdParty::active).count();
                long prospectCount = thirdParties.stream().filter(ThirdParty::prospect).count();
                long convertedCount = thirdParties.stream().filter(thirdParty -> thirdParty.convertedAt() != null)
                                .count();
                long withBankAccountCount = entries.stream().filter(Map.Entry::getValue).count();
                return new ThirdPartyStatistics(thirdParties.size(), activeCount, thirdParties.size() - activeCount,
                                prospectCount, convertedCount, withBankAccountCount);
        }

        private Flux<ThirdPartySearchResult> searchFromRepository(UUID tenantId, UUID organizationId, String query,
                        String role, Boolean prospect, String segment, Integer minimumQualificationScore,
                        Boolean active, String followUpStatus, int page, int size) {
                String normalizedQuery = query == null ? null : query.trim();
                String normalizedSegment = segment == null ? null : segment.trim().toUpperCase();
                String normalizedFollowUp = followUpStatus == null ? null : followUpStatus.trim().toUpperCase();
                return thirdPartyRepository.findByOrganizationId(tenantId, organizationId)
                                .filter(thirdParty -> role == null || role.isBlank() || thirdParty.hasRole(role))
                                .filter(thirdParty -> prospect == null || thirdParty.prospect() == prospect)
                                .filter(thirdParty -> active == null || thirdParty.active() == active)
                                .filter(thirdParty -> normalizedSegment == null || normalizedSegment.isBlank()
                                                || normalizedSegment.equals(thirdParty.segment()))
                                .filter(thirdParty -> normalizedFollowUp == null || normalizedFollowUp.isBlank()
                                                || normalizedFollowUp.equals(thirdParty.followUpStatus()))
                                .filter(thirdParty -> minimumQualificationScore == null
                                                || (thirdParty.qualificationScore() != null
                                                                && thirdParty.qualificationScore() >= minimumQualificationScore))
                                .filter(thirdParty -> matchesQuery(thirdParty, normalizedQuery))
                                .map(this::toSearchResult)
                                .skip((long) page * size)
                                .take(size);
        }

        private boolean matchesQuery(ThirdParty thirdParty, String query) {
                if (query == null || query.isBlank()) {
                        return true;
                }
                String normalized = query.toUpperCase();
                return thirdParty.referenceCode().contains(normalized)
                                || thirdParty.displayName().toUpperCase().contains(normalized)
                                || (thirdParty.longName() != null && thirdParty.longName().toUpperCase().contains(normalized))
                                || (thirdParty.acronym() != null && thirdParty.acronym().toUpperCase().contains(normalized));
        }

        private ThirdPartySearchResult toSearchResult(ThirdParty thirdParty) {
                return new ThirdPartySearchResult(
                                thirdParty.id(),
                                thirdParty.tenantId(),
                                thirdParty.organizationId(),
                                thirdParty.partyRef().partyType(),
                                thirdParty.partyRef().partyId(),
                                thirdParty.code(),
                                thirdParty.name(),
                                thirdParty.type(),
                                thirdParty.longName(),
                                thirdParty.roles(),
                                thirdParty.prospect(),
                                thirdParty.accountingAccount(),
                                thirdParty.segment(),
                                thirdParty.qualificationScore(),
                                thirdParty.enabled(),
                                thirdParty.lastContactedAt(),
                                thirdParty.nextFollowUpAt(),
                                thirdParty.followUpStatus(),
                                thirdParty.convertedAt());
        }

        private ThirdParty applyQualificationPolicy(ThirdParty thirdParty, boolean hasBankAccount,
                        boolean forceRecompute) {
                Integer score = thirdParty.qualificationScore();
                String segment = thirdParty.segment();
                boolean autoManaged = forceRecompute || isAutoManagedQualification(thirdParty);

                if (score == null || autoManaged) {
                        score = computeQualificationScore(thirdParty, hasBankAccount);
                }
                if (segment == null || autoManaged) {
                        segment = deriveSegment(score);
                }
                return thirdParty.qualify(segment, score);
        }

        private ThirdParty prepareNewThirdPartyForInsert(ThirdParty thirdParty, boolean hasBankAccount) {
                ThirdParty qualified = applyQualificationPolicy(thirdParty, hasBankAccount, false);
                return ThirdParty.rehydrate(qualified.id(), qualified.tenantId(), thirdParty.createdAt(),
                                thirdParty.createdAt(),
                                qualified.organizationId(), qualified.partyRef(), qualified.referenceCode(),
                                qualified.displayName(),
                                qualified.roles(), qualified.prospect(), qualified.accountingAccount(),
                                qualified.segment(),
                                qualified.qualificationScore(), qualified.active(), qualified.lastContactedAt(),
                                qualified.nextFollowUpAt(), qualified.followUpStatus(), qualified.convertedAt(),
                                qualified.type(), qualified.legalForm(), qualified.uniqueIdentificationNumber(),
                                qualified.tradeRegistrationNumber(), qualified.name(), qualified.acronym(),
                                qualified.longName(), qualified.logoUri(), qualified.logoId(),
                                qualified.accountingAccountNumbers(), qualified.authorizedPaymentMethods(),
                                qualified.authorizedCreditLimit(), qualified.maxDiscountRate(), qualified.vatSubject(),
                                qualified.operationsBalance(), qualified.openingBalance(), qualified.payTermNumber(),
                                qualified.payTermType(), qualified.thirdPartyFamily(), qualified.classification(),
                                qualified.taxNumber(), qualified.loyaltyPoints(), qualified.loyaltyPointsUsed(),
                                qualified.loyaltyPointsExpired(), qualified.deletedAt());
        }

        private boolean isAutoManagedQualification(ThirdParty thirdParty) {
                return thirdParty.segment() == null || AUTO_MANAGED_SEGMENTS.contains(thirdParty.segment());
        }

        private int computeQualificationScore(ThirdParty thirdParty, boolean hasBankAccount) {
                int score = 0;
                if (thirdParty.active()) {
                        score += 20;
                }
                score += thirdParty.prospect() ? 25 : 10;
                if (thirdParty.accountingAccount() != null) {
                        score += 20;
                }
                if (hasBankAccount) {
                        score += 20;
                }
                score += Math.min(thirdParty.roles().size() * 5, 15);
                if (thirdParty.segment() != null && thirdParty.segment().startsWith("VIP")) {
                        score += 10;
                }
                return Math.min(score, 100);
        }

        private String deriveSegment(int score) {
                if (score >= 80) {
                        return "HOT";
                }
                if (score >= 50) {
                        return "QUALIFIED";
                }
                return "NEW";
        }

        private BusinessEvent thirdPartyEvent(String eventType, ThirdParty thirdParty) {
                return BusinessEvent.now(thirdParty.tenantId(), thirdParty.organizationId(), eventType,
                                "THIRD_PARTY", thirdParty.id(), payload(
                                                "code", thirdParty.code(),
                                                "referenceCode", thirdParty.referenceCode(),
                                                "name", thirdParty.name(),
                                                "displayName", thirdParty.displayName(),
                                                "type", thirdParty.type(),
                                                "longName", thirdParty.longName(),
                                                "roles", thirdParty.roles(),
                                                "prospect", thirdParty.prospect(),
                                                "partyType", thirdParty.partyRef().partyType().name(),
                                                "partyId", thirdParty.partyRef().partyId(),
                                                "accountingAccount", thirdParty.accountingAccount(),
                                                "segment", thirdParty.segment(),
                                                "qualificationScore", thirdParty.qualificationScore(),
                                                "lastContactedAt", thirdParty.lastContactedAt(),
                                                "nextFollowUpAt", thirdParty.nextFollowUpAt(),
                                                "followUpStatus", thirdParty.followUpStatus(),
                                                "enabled", thirdParty.enabled(),
                                                "active", thirdParty.active(),
                                                "classification", thirdParty.classification(),
                                                "convertedAt", thirdParty.convertedAt()));
        }

        private Map<String, Object> payload(Object... entries) {
                Map<String, Object> payload = new LinkedHashMap<>();
                for (int index = 0; index < entries.length; index += 2) {
                        payload.put(entries[index].toString(), entries[index + 1]);
                }
                return payload;
        }

        private List<String> normalizeAccountingAccounts(List<String> accountingAccountNumbers) {
                if (accountingAccountNumbers == null || accountingAccountNumbers.isEmpty()) {
                        throw new IllegalArgumentException("accountingAccountNumbers must not be empty");
                }
                List<String> normalized = accountingAccountNumbers.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .distinct()
                                .toList();
                if (normalized.isEmpty()) {
                        throw new IllegalArgumentException("accountingAccountNumbers must not be empty");
                }
                return normalized;
        }

        private Mono<ThirdParty> createActorFinancialCounterparty(EnsureActorFinancialProfileCommand command,
                        String normalizedRole,
                        String resolvedReference,
                        String resolvedDisplayName) {
                ThirdParty created = ThirdParty.create(
                                command.tenantId(),
                                command.organizationId(),
                                new PartyRef(PartyType.ACTOR, command.actorId()),
                                resolvedReference,
                                resolvedDisplayName,
                                Set.of(normalizedRole),
                                false,
                                null,
                                null,
                                null,
                                true);
                return assertReferenceAvailable(created.tenantId(), created.organizationId(), created.referenceCode())
                                .then(Mono.fromSupplier(() -> prepareNewThirdPartyForInsert(created, false)))
                                .flatMap(thirdPartyRepository::save)
                                .flatMap(saved -> businessEventPublisher.publish(thirdPartyEvent("THIRD_PARTY_CREATED", saved))
                                                .thenReturn(saved));
        }

        private Mono<ThirdParty> mergeFinancialRole(ThirdParty existing, String normalizedRole) {
                if (existing.hasRole(normalizedRole) && existing.active()) {
                        return Mono.just(existing);
                }
                java.util.LinkedHashSet<String> mergedRoles = new java.util.LinkedHashSet<>(existing.roles());
                mergedRoles.add(normalizedRole);
                ThirdParty updated = existing.update(
                                existing.referenceCode(),
                                existing.displayName(),
                                mergedRoles,
                                existing.prospect(),
                                existing.accountingAccount(),
                                existing.segment(),
                                existing.qualificationScore(),
                                existing.active(),
                                existing.type(),
                                existing.legalForm(),
                                existing.uniqueIdentificationNumber(),
                                existing.tradeRegistrationNumber(),
                                existing.name(),
                                existing.acronym(),
                                existing.longName(),
                                existing.logoUri(),
                                existing.logoId(),
                                existing.accountingAccountNumbers(),
                                existing.authorizedPaymentMethods(),
                                existing.authorizedCreditLimit(),
                                existing.maxDiscountRate(),
                                existing.vatSubject(),
                                existing.operationsBalance(),
                                existing.openingBalance(),
                                existing.payTermNumber(),
                                existing.payTermType(),
                                existing.thirdPartyFamily(),
                                existing.classification(),
                                existing.taxNumber());
                return thirdPartyRepository.save(updated);
        }

        private Mono<ThirdParty> ensureAssignedAccountingAccounts(ThirdParty thirdParty) {
                if (thirdParty.accountingAccount() != null && !thirdParty.accountingAccount().isBlank()) {
                        return Mono.just(thirdParty);
                }
                return thirdPartyRepository.findByOrganizationId(thirdParty.tenantId(), thirdParty.organizationId())
                                .collectList()
                                .flatMap(thirdParties -> assignAccountingAccounts(
                                                thirdParty.tenantId(),
                                                thirdParty.organizationId(),
                                                thirdParty.id(),
                                                generateFinancialAccountingAccounts(thirdParty, thirdParties)));
        }

        private Mono<ThirdParty> ensureCanonicalBankAccount(ThirdParty thirdParty, String normalizedRole) {
                if (canonicalBankAccountProvisioner.isEmpty()) {
                        return Mono.just(thirdParty);
                }
                return canonicalBankAccountProvisioner.get()
                                .ensureBankAccount(thirdParty.tenantId(), thirdParty.organizationId(), thirdParty.id(),
                                                normalizedRole, "XAF")
                                .thenReturn(thirdParty);
        }

        private List<String> generateFinancialAccountingAccounts(ThirdParty thirdParty,
                        List<ThirdParty> organizationThirdParties) {
                java.util.LinkedHashSet<String> prefixes = resolveFinancialPrefixes(thirdParty.roles());
                java.util.LinkedHashSet<String> knownAccounts = organizationThirdParties.stream()
                                .filter(existing -> !existing.id().equals(thirdParty.id()))
                                .flatMap(existing -> existing.accountingAccountNumbers().stream())
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

                java.util.List<String> generatedAccounts = new java.util.ArrayList<>();
                for (String prefix : prefixes) {
                        long nextSuffix = nextFinancialSuffix(prefix, knownAccounts);
                        String generated = prefix + nextSuffix;
                        knownAccounts.add(generated);
                        generatedAccounts.add(generated);
                }
                return generatedAccounts;
        }

        private java.util.LinkedHashSet<String> resolveFinancialPrefixes(Set<String> roles) {
                java.util.LinkedHashSet<String> prefixes = new java.util.LinkedHashSet<>();
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

        private long nextFinancialSuffix(String prefix, Set<String> knownAccounts) {
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

        private String normalizeFinancialRole(String role) {
                if (role == null || role.isBlank()) {
                        return "BENEFICIARY";
                }
                return role.trim().toUpperCase();
        }

        private String resolveFinancialReferenceCode(String referenceCode, UUID actorId, String normalizedRole) {
                if (referenceCode != null && !referenceCode.isBlank()) {
                        return referenceCode.trim();
                }
                String prefix = switch (normalizedRole) {
                        case "CUSTOMER" -> "CLI";
                        case "SUPPLIER" -> "FOU";
                        case "EMPLOYEE" -> "EMP";
                        case "PAYEE", "BENEFICIARY" -> "BEN";
                        default -> "TP";
                };
                return prefix + "-" + actorId.toString().substring(0, 8).toUpperCase();
        }

        private String resolveFinancialDisplayName(String displayName, UUID actorId) {
                if (displayName != null && !displayName.isBlank()) {
                        return displayName.trim();
                }
                return "Actor " + actorId.toString().substring(0, 8).toUpperCase();
        }
}
