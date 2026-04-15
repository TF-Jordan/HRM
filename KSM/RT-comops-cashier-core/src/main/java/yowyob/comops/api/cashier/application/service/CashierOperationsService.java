package yowyob.comops.api.cashier.application.service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContext;
import yowyob.comops.api.cashier.persistence.AuditEntryEntity;
import yowyob.comops.api.cashier.persistence.AuditEntryRepository;
import yowyob.comops.api.cashier.persistence.BillEntity;
import yowyob.comops.api.cashier.persistence.BillRepository;
import yowyob.comops.api.cashier.persistence.CashDocumentEntity;
import yowyob.comops.api.cashier.persistence.CashDocumentRepository;
import yowyob.comops.api.cashier.persistence.CashMovementEntity;
import yowyob.comops.api.cashier.persistence.CashMovementRepository;
import yowyob.comops.api.cashier.persistence.CashNotificationEntity;
import yowyob.comops.api.cashier.persistence.CashNotificationRepository;
import yowyob.comops.api.cashier.persistence.CashReconciliationEntity;
import yowyob.comops.api.cashier.persistence.CashReconciliationRepository;
import yowyob.comops.api.cashier.persistence.CashRegisterEntity;
import yowyob.comops.api.cashier.persistence.CashRegisterRepository;
import yowyob.comops.api.cashier.persistence.CashSessionEntity;
import yowyob.comops.api.cashier.persistence.CashSessionRepository;
import yowyob.comops.api.cashier.persistence.CashierAssignmentEntity;
import yowyob.comops.api.cashier.persistence.CashierAssignmentRepository;
import yowyob.comops.api.cashier.persistence.CashierProfileEntity;
import yowyob.comops.api.cashier.persistence.CashierProfileRepository;
import yowyob.comops.api.cashier.persistence.FundRequestEntity;
import yowyob.comops.api.cashier.persistence.FundRequestRepository;
import yowyob.comops.api.cashier.persistence.WalletAccountEntity;
import yowyob.comops.api.cashier.persistence.WalletAccountRepository;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileCommand;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.cashier.web.CashierRequests;
import yowyob.comops.api.cashier.web.CashierViews;

@Service
public class CashierOperationsService {

    public static final String KIND_CASHIER = "CASHIER";
    public static final String KIND_ORGANIZATION_ADMIN = "ORGANIZATION_ADMIN";
    public static final String KIND_AGENCY_ADMIN = "AGENCY_ADMIN";
    public static final String KIND_USER_ADMIN = "USER_ADMIN";
    public static final String KIND_SELF = "SELF";

    
    private final Map<UUID, CashRegister> cashRegisters = new ConcurrentHashMap<>();
    private final Map<UUID, CashierProfile> profiles = new ConcurrentHashMap<>();
    private final Map<UUID, CashierAssignment> assignments = new ConcurrentHashMap<>();
    private final Map<UUID, CashSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, WalletAccount> accounts = new ConcurrentHashMap<>();
    private final Map<UUID, FundRequest> fundRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Bill> bills = new ConcurrentHashMap<>();
    private final Map<UUID, CashMovement> movements = new ConcurrentHashMap<>();
    private final Map<UUID, CashReconciliation> reconciliations = new ConcurrentHashMap<>();
    private final Map<UUID, AuditEntry> auditEntries = new ConcurrentHashMap<>();
    private final Map<UUID, CashNotification> notifications = new ConcurrentHashMap<>();
    private final Map<UUID, CashDocument> documents = new ConcurrentHashMap<>();

    private final CashRegisterRepository cashRegisterRepository;
    private final CashierProfileRepository cashierProfileRepository;
    private final CashierAssignmentRepository cashierAssignmentRepository;
    private final CashSessionRepository cashSessionRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final FundRequestRepository fundRequestRepository;
    private final BillRepository billRepository;
    private final CashMovementRepository cashMovementRepository;
    private final CashReconciliationRepository cashReconciliationRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final CashNotificationRepository cashNotificationRepository;
    private final CashDocumentRepository cashDocumentRepository;
    private final AccountingBookkeepingService accountingBookkeepingService;
    private final GetThirdPartyUseCase getThirdPartyUseCase;
    private final EnsureActorFinancialProfileUseCase ensureActorFinancialProfileUseCase;

    public CashierOperationsService(CashRegisterRepository cashRegisterRepository,
            CashierProfileRepository cashierProfileRepository,
            CashierAssignmentRepository cashierAssignmentRepository,
            CashSessionRepository cashSessionRepository,
            WalletAccountRepository walletAccountRepository,
            FundRequestRepository fundRequestRepository,
            BillRepository billRepository,
            CashMovementRepository cashMovementRepository,
            CashReconciliationRepository cashReconciliationRepository,
            AuditEntryRepository auditEntryRepository,
            CashNotificationRepository cashNotificationRepository,
            CashDocumentRepository cashDocumentRepository,
            AccountingBookkeepingService accountingBookkeepingService,
            GetThirdPartyUseCase getThirdPartyUseCase,
            EnsureActorFinancialProfileUseCase ensureActorFinancialProfileUseCase) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashierProfileRepository = cashierProfileRepository;
        this.cashierAssignmentRepository = cashierAssignmentRepository;
        this.cashSessionRepository = cashSessionRepository;
        this.walletAccountRepository = walletAccountRepository;
        this.fundRequestRepository = fundRequestRepository;
        this.billRepository = billRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.cashReconciliationRepository = cashReconciliationRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.cashNotificationRepository = cashNotificationRepository;
        this.cashDocumentRepository = cashDocumentRepository;
        this.accountingBookkeepingService = accountingBookkeepingService;
        this.getThirdPartyUseCase = getThirdPartyUseCase;
        this.ensureActorFinancialProfileUseCase = ensureActorFinancialProfileUseCase;
    }

    @PostConstruct
    void loadState() {
        restoreInto(cashRegisters, cashRegisterRepository.findAll().map(this::toDomain), CashRegister::id);
        restoreInto(profiles, cashierProfileRepository.findAll().map(this::toDomain), CashierProfile::id);
        restoreInto(assignments, cashierAssignmentRepository.findAll().map(this::toDomain), CashierAssignment::id);
        restoreInto(sessions, cashSessionRepository.findAll().map(this::toDomain), CashSession::id);
        restoreInto(accounts, walletAccountRepository.findAll().map(this::toDomain), WalletAccount::id);
        restoreInto(fundRequests, fundRequestRepository.findAll().map(this::toDomain), FundRequest::id);
        restoreInto(bills, billRepository.findAll().map(this::toDomain), Bill::id);
        restoreInto(movements, cashMovementRepository.findAll().map(this::toDomain), CashMovement::id);
        restoreInto(reconciliations, cashReconciliationRepository.findAll().map(this::toDomain), CashReconciliation::id);
        restoreInto(auditEntries, auditEntryRepository.findAll().map(this::toDomain), AuditEntry::id);
        restoreInto(notifications, cashNotificationRepository.findAll().map(this::toDomain), CashNotification::id);
        restoreInto(documents, cashDocumentRepository.findAll().map(this::toDomain), CashDocument::id);
    }

    private <V> void restoreInto(Map<UUID, V> target, Flux<V> source, Function<V, UUID> idExtractor) {
        target.clear();
        try {
            source.collectList()
                    .blockOptional()
                    .ifPresent(list -> list.forEach(value -> target.put(idExtractor.apply(value), value)));
        } catch (BadSqlGrammarException exception) {
            target.clear();
        }
    }

    public Mono<CashierViews.CashRegisterView> createCashRegister(CashierRequests.CreateCashRegisterRequest request,
            CashierRequestContext context) {
        UUID registerId = UUID.randomUUID();
        return accountingBookkeepingService.ensureCashRegisterAccount(registerId, request.label(), toAccountingContext(context))
                .map(account -> new CashRegister(
                        registerId,
                        context.requireOrganizationId(),
                        request.agencyId(),
                        request.code().trim(),
                        request.label().trim(),
                        "ACTIVE",
                        null,
                        account.id(),
                        account.accountNumber(),
                        Instant.now()))
                .map(register -> {
                    storeRegister(register);
                    ensureAccount(context.requireOrganizationId(), register.id(), register.label(), "REGISTER_CASH", "XAF", null);
                    audit(context.requireOrganizationId(), "CASH_REGISTER_CREATED", "CASH_REGISTER", register.id(),
                            register.code());
                    return toView(register);
                });
    }

    public Flux<CashierViews.CashRegisterView> listCashRegisters(CashierRequestContext context) {
        return Flux.fromStream(cashRegisters.values().stream()
                .filter(register -> register.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashRegister::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.CashRegisterView> getCashRegister(UUID registerId, CashierRequestContext context) {
        return Mono.just(toView(requireRegister(registerId, context.requireOrganizationId())));
    }

    public Mono<CashierViews.CashRegisterView> updateCashRegister(UUID registerId,
            CashierRequests.UpdateCashRegisterRequest request,
            CashierRequestContext context) {
        CashRegister updated = requireRegister(registerId, context.requireOrganizationId())
                .withUpdate(request.code(), request.label(), request.agencyId(), request.status());
        storeRegister(updated);
        audit(context.requireOrganizationId(), "CASH_REGISTER_UPDATED", "CASH_REGISTER", updated.id(), updated.code());
        return Mono.just(toView(updated));
    }

    public Mono<Void> deleteCashRegister(UUID registerId, CashierRequestContext context) {
        requireRegister(registerId, context.requireOrganizationId());
        deleteRegister(registerId);
        sessions.values().stream()
                .filter(session -> session.registerId().equals(registerId))
                .map(CashSession::id)
                .toList()
                .forEach(this::deleteSession);
        audit(context.requireOrganizationId(), "CASH_REGISTER_DELETED", "CASH_REGISTER", registerId, "deleted");
        return Mono.empty();
    }

    public Mono<CashierViews.CashRegisterView> assignCashRegister(UUID registerId, UUID cashierId, CashierRequestContext context) {
        requireProfile(cashierId, context.requireOrganizationId());
        CashRegister updated = requireRegister(registerId, context.requireOrganizationId()).withAssignedCashier(cashierId);
        storeRegister(updated);
        audit(context.requireOrganizationId(), "CASH_REGISTER_ASSIGNED", "CASH_REGISTER", updated.id(), cashierId.toString());
        return Mono.just(toView(updated));
    }

    public Mono<CashierViews.CashierProfileView> createProfile(CashierRequests.CreateCashierProfileRequest request,
            String forcedKind,
            CashierRequestContext context) {
        String kind = normalizeKind(forcedKind != null ? forcedKind : request.kind());
        CashierProfile profile = new CashierProfile(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.agencyId(),
                request.kernelUserId(),
                request.email(),
                request.fullName().trim(),
                kind,
                true,
                Instant.now());
        storeProfile(profile);
        audit(context.requireOrganizationId(), "PROFILE_CREATED", kind, profile.id(), profile.email());
        return Mono.just(toView(profile));
    }

    public Mono<CashierViews.CashierProfileView> updateProfile(UUID profileId,
            CashierRequests.UpdateCashierProfileRequest request,
            CashierRequestContext context) {
        CashierProfile updated = requireProfile(profileId, context.requireOrganizationId()).withUpdate(
                request.kernelUserId(),
                request.email(),
                request.fullName(),
                request.agencyId(),
                normalizeKind(request.kind()),
                request.active());
        storeProfile(updated);
        audit(context.requireOrganizationId(), "PROFILE_UPDATED", updated.kind(), updated.id(), updated.email());
        return Mono.just(toView(updated));
    }

    public Mono<Void> deleteProfile(UUID profileId, CashierRequestContext context) {
        requireProfile(profileId, context.requireOrganizationId());
        deleteProfile(profileId);
        assignments.values().stream()
                .filter(assignment -> assignment.cashierId().equals(profileId))
                .map(CashierAssignment::id)
                .toList()
                .forEach(this::deleteAssignmentInternal);
        audit(context.requireOrganizationId(), "PROFILE_DELETED", "PROFILE", profileId, "deleted");
        return Mono.empty();
    }

    public Mono<CashierViews.CashierProfileView> getProfile(UUID profileId, CashierRequestContext context) {
        return Mono.just(toView(requireProfile(profileId, context.requireOrganizationId())));
    }

    public Flux<CashierViews.CashierProfileView> listCashiers(CashierRequestContext context) {
        return listProfilesByKind(KIND_CASHIER, context);
    }

    public Flux<CashierViews.CashierProfileView> listProfilesByKind(String kind, CashierRequestContext context) {
        String normalized = normalizeKind(kind);
        return Flux.fromStream(profiles.values().stream()
                .filter(profile -> profile.organizationId().equals(context.requireOrganizationId()))
                .filter(profile -> profile.kind().equals(normalized))
                .sorted(Comparator.comparing(CashierProfile::createdAt).reversed()))
                .map(this::toView);
    }

    public Flux<CashierViews.CashierProfileView> listAvailableCashiers(CashierRequestContext context) {
        return listCashiers(context)
                .filter(profile -> cashRegisters.values().stream()
                        .noneMatch(register -> profile.id().equals(register.assignedCashierId())));
    }

    public Flux<CashierViews.CashierProfileView> listCashiersWithProfile(CashierRequestContext context) {
        return listCashiers(context);
    }

    public Mono<CashierViews.CashierAssignmentView> createAssignment(CashierRequests.CreateAssignmentRequest request,
            CashierRequestContext context) {
        requireProfile(request.cashierId(), context.requireOrganizationId());
        CashierAssignment assignment = new CashierAssignment(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.agencyId(),
                request.cashierId(),
                Instant.now());
        storeAssignment(assignment);
        audit(context.requireOrganizationId(), "ASSIGNMENT_CREATED", "ASSIGNMENT", assignment.id(),
                assignment.cashierId().toString());
        return Mono.just(toView(assignment));
    }

    public Flux<CashierViews.CashierAssignmentView> listAssignments(CashierRequestContext context) {
        return Flux.fromStream(assignments.values().stream()
                .filter(assignment -> assignment.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashierAssignment::assignedAt).reversed()))
                .map(this::toView);
    }

    public Mono<Void> deleteAssignment(UUID assignmentId, CashierRequestContext context) {
        CashierAssignment assignment = assignments.get(assignmentId);
        if (assignment == null || !assignment.organizationId().equals(context.requireOrganizationId())) {
            return Mono.error(new IllegalArgumentException("assignment not found"));
        }
        deleteAssignmentInternal(assignmentId);
        audit(context.requireOrganizationId(), "ASSIGNMENT_DELETED", "ASSIGNMENT", assignmentId, "deleted");
        return Mono.empty();
    }

    public Mono<CashierViews.CashierSessionView> openSession(CashierRequests.CreateSessionRequest request,
            CashierRequestContext context) {
        CashRegister register = requireRegister(request.registerId(), context.requireOrganizationId());
        requireProfile(request.cashierId(), context.requireOrganizationId());
        sessions.values().stream()
                .filter(existing -> existing.organizationId().equals(context.requireOrganizationId()))
                .filter(existing -> existing.registerId().equals(register.id()))
                .filter(existing -> "OPEN".equals(existing.status()))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("an open session already exists for this register");
                });
        CashSession session = new CashSession(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                register.agencyId(),
                register.id(),
                request.cashierId(),
                "OPEN",
                request.openingAmount(),
                BigDecimal.ZERO,
                request.currency().trim().toUpperCase(Locale.ROOT),
                Instant.now(),
                null,
                false,
                null);
        WalletAccount registerAccount = ensureAccount(context.requireOrganizationId(), register.id(), register.label(),
                "REGISTER_CASH", session.currency(), null).withBalance(session.openingAmount());
        storeAccount(registerAccount);
        storeSession(session);
        return accountingBookkeepingService.createCashRegisterPosting(
                        new yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests.CreateCashRegisterPostingRequest(
                                register.code(),
                                register.id(),
                                register.accountingAccountId(),
                                register.accountingAccountNumber(),
                                request.openingAmount(),
                                session.currency(),
                                "OPENING",
                                session.id(),
                                null,
                                null,
                                "Opening amount for session " + session.id()),
                        toAccountingContext(context))
                .map(posting -> {
                    CashMovement openingMovement = recordMovement(
                            context.requireOrganizationId(),
                            session.id(),
                            register.id(),
                            registerAccount.id(),
                            "OPENING_FLOAT",
                            request.openingAmount(),
                            session.currency(),
                            register.code() + "-OPEN-" + session.id().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                            "POSTED",
                            posting.id(),
                            posting.postingType(),
                            posting.registerAccountNumber(),
                            posting.counterpartyAccountNumber());
                    audit(context.requireOrganizationId(), "SESSION_OPENED", "SESSION", session.id(), register.code());
                    return toView(session);
                });
    }

    public Flux<CashierViews.CashierSessionView> listSessions(CashierRequestContext context) {
        return Flux.fromStream(sessions.values().stream()
                .filter(session -> session.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashSession::openedAt).reversed()))
                .map(this::toView);
    }

    public Flux<CashierViews.CashierSessionView> listCashierSessions(CashierRequestContext context) {
        return listSessions(context).filter(session -> "OPEN".equals(session.status()));
    }

    public Mono<CashierViews.CashierSessionView> closeSession(UUID sessionId,
            CashierRequests.CloseSessionRequest request,
            CashierRequestContext context) {
        CashSession current = requireSession(sessionId, context.requireOrganizationId());
        CashRegister register = requireRegister(current.registerId(), context.requireOrganizationId());
        WalletAccount registerAccount = ensureAccount(context.requireOrganizationId(), register.id(), register.label(),
                "REGISTER_CASH", current.currency(), null);
        BigDecimal expectedAmount = registerAccount.balance();
        BigDecimal discrepancy = request.closingAmount().subtract(expectedAmount);
        Mono<?> discrepancyPosting = Mono.empty();
        if (discrepancy.compareTo(BigDecimal.ZERO) > 0) {
            discrepancyPosting = accountingBookkeepingService.createCashRegisterPosting(
                    new yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests.CreateCashRegisterPostingRequest(
                            register.code(),
                            register.id(),
                            register.accountingAccountId(),
                            register.accountingAccountNumber(),
                            discrepancy,
                            current.currency(),
                            "CLOSING_OVERAGE",
                            current.id(),
                            null,
                            null,
                            "Closing overage for session " + current.id()),
                    toAccountingContext(context));
        } else if (discrepancy.compareTo(BigDecimal.ZERO) < 0) {
            discrepancyPosting = accountingBookkeepingService.createCashRegisterPosting(
                    new yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests.CreateCashRegisterPostingRequest(
                            register.code(),
                            register.id(),
                            register.accountingAccountId(),
                            register.accountingAccountNumber(),
                            discrepancy.abs(),
                            current.currency(),
                            "CLOSING_SHORTAGE",
                            current.id(),
                            null,
                            null,
                            "Closing shortage for session " + current.id()),
                    toAccountingContext(context));
        }
        return Mono.from(discrepancyPosting)
                .onErrorResume(ex -> Mono.error(ex))
                .then(Mono.fromSupplier(() -> {
                    WalletAccount adjustedRegisterAccount = registerAccount.withBalance(request.closingAmount());
                    storeAccount(adjustedRegisterAccount);
                    CashSession updated = current.withClose(request.closingAmount(), request.note());
                    storeSession(updated);
                    CashReconciliation reconciliation = new CashReconciliation(
                            UUID.randomUUID(),
                            context.requireOrganizationId(),
                            updated.id(),
                            updated.registerId(),
                            discrepancy.compareTo(BigDecimal.ZERO) == 0 ? "MATCHED" : "PENDING",
                            null,
                            request.note(),
                            Instant.now(),
                            null);
                    storeReconciliation(reconciliation);
                    audit(context.requireOrganizationId(), "SESSION_CLOSED", "SESSION", updated.id(),
                            request.note() == null ? "closed" : request.note());
                    return toView(updated);
                }));
    }

    public Mono<CashierViews.CashierSessionView> lockSession(UUID sessionId, CashierRequestContext context) {
        CashSession updated = requireSession(sessionId, context.requireOrganizationId()).withLock(true);
        storeSession(updated);
        audit(context.requireOrganizationId(), "SESSION_LOCKED", "SESSION", updated.id(), "locked");
        return Mono.just(toView(updated));
    }

    public Mono<CashierViews.CashierSessionView> unlockSession(UUID sessionId, CashierRequestContext context) {
        CashSession updated = requireSession(sessionId, context.requireOrganizationId()).withLock(false);
        storeSession(updated);
        audit(context.requireOrganizationId(), "SESSION_UNLOCKED", "SESSION", updated.id(), "unlocked");
        return Mono.just(toView(updated));
    }

    public Mono<CashierViews.CashierSessionView> findActiveSession(CashierRequestContext context) {
        return listCashierSessions(context).next();
    }

    public Flux<CashierViews.WalletAccountView> listAccounts(CashierRequestContext context) {
        return Flux.fromStream(accounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(WalletAccount::number)))
                .map(this::toView);
    }

    public Mono<CashierViews.WalletAccountView> transfer(CashierRequests.TransferRequest request, CashierRequestContext context) {
        WalletAccount source = requireAccount(request.sourceAccountId(), context.requireOrganizationId());
        WalletAccount target = requireAccount(request.targetAccountId(), context.requireOrganizationId());
        if (source.balance().compareTo(request.amount()) < 0) {
            return Mono.error(new IllegalArgumentException("insufficient balance"));
        }
        WalletAccount updatedSource = source.withBalance(source.balance().subtract(request.amount()));
        WalletAccount updatedTarget = target.withBalance(target.balance().add(request.amount()));
        storeAccount(updatedSource);
        storeAccount(updatedTarget);
        recordMovement(context.requireOrganizationId(), null, null, updatedTarget.id(), "TRANSFER",
                request.amount(), updatedTarget.currency(), request.reference(), "POSTED",
                null, null, null, null);
        audit(context.requireOrganizationId(), "ACCOUNT_TRANSFER", "ACCOUNT", updatedTarget.id(), request.reference());
        return Mono.just(toView(updatedTarget));
    }

    public Mono<CashierViews.WalletAccountView> withdraw(CashierRequests.WithdrawRequest request, CashierRequestContext context) {
        WalletAccount account = requireAccount(request.accountId(), context.requireOrganizationId());
        if (account.balance().compareTo(request.amount()) < 0) {
            return Mono.error(new IllegalArgumentException("insufficient balance"));
        }
        WalletAccount updated = account.withBalance(account.balance().subtract(request.amount()));
        storeAccount(updated);
        createMovement(new CashierRequests.CreateMovementRequest(
                request.sessionId(), request.registerId(), updated.id(), "WITHDRAW", request.amount(), updated.currency(),
                request.reference(), request.counterpartyActorId(), request.counterpartyThirdPartyId(), null), context).subscribe();
        return Mono.just(toView(updated));
    }

    public Mono<CashierViews.WalletAccountView> transferP2P(CashierRequests.P2PTransferRequest request,
            CashierRequestContext context) {
        String fromCustomerName = request.fromCustomerId().toString();
        String toCustomerName = request.toCustomerId().toString();
        return Mono.zip(
                        ensureCounterpartyThirdPartyId(request.fromCustomerId(), fromCustomerName, "CUSTOMER", context),
                        ensureCounterpartyThirdPartyId(request.toCustomerId(), toCustomerName, "CUSTOMER", context))
                .map(tuple -> {
                    UUID sourceThirdPartyId = tuple.getT1();
                    UUID targetThirdPartyId = tuple.getT2();
                    WalletAccount source = ensureAccount(context.requireOrganizationId(), request.fromCustomerId(),
                            fromCustomerName,
                            "CUSTOMER_WALLET", "XAF", sourceThirdPartyId);
                    WalletAccount target = ensureAccount(context.requireOrganizationId(), request.toCustomerId(),
                            toCustomerName,
                            "CUSTOMER_WALLET", "XAF", targetThirdPartyId);
                    if (source.balance().compareTo(request.amount()) < 0) {
                        throw new IllegalArgumentException("insufficient balance");
                    }
                    storeAccount(source.withBalance(source.balance().subtract(request.amount())));
                    WalletAccount updatedTarget = target.withBalance(target.balance().add(request.amount()));
                    storeAccount(updatedTarget);
                    CashMovement movement = recordMovement(context.requireOrganizationId(), null, null, updatedTarget.id(),
                            "P2P_TRANSFER", request.amount(), updatedTarget.currency(), request.reference(), "POSTED",
                            null, null, null, null);
                    audit(context.requireOrganizationId(), "P2P_TRANSFER", "ACCOUNT", updatedTarget.id(), request.reference());
                    return toView(updatedTarget);
                });
    }

    public Mono<CashierViews.FundRequestView> createFundRequest(CashierRequests.CreateFundRequest request,
            CashierRequestContext context) {
        requireRegister(request.registerId(), context.requireOrganizationId());
        requireProfile(request.cashierId(), context.requireOrganizationId());
        FundRequest fundRequest = new FundRequest(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.registerId(),
                request.cashierId(),
                request.amount(),
                "PENDING",
                request.reason(),
                Instant.now());
        storeFundRequest(fundRequest);
        audit(context.requireOrganizationId(), "FUND_REQUEST_CREATED", "FUND_REQUEST", fundRequest.id(), fundRequest.reason());
        return Mono.just(toView(fundRequest));
    }

    public Flux<CashierViews.FundRequestView> listFundRequests(CashierRequestContext context) {
        return Flux.fromStream(fundRequests.values().stream()
                .filter(request -> request.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(FundRequest::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.BillView> createBill(CashierRequests.CreateBillRequest request, CashierRequestContext context) {
        Bill bill = new Bill(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.customerId(),
                request.reference().trim(),
                request.totalAmount(),
                BigDecimal.ZERO,
                request.currency().trim(),
                "UNPAID",
                Instant.now());
        storeBill(bill);
        audit(context.requireOrganizationId(), "BILL_CREATED", "BILL", bill.id(), bill.reference());
        return Mono.just(toView(bill));
    }

    public Flux<CashierViews.BillView> listBills(CashierRequestContext context) {
        return Flux.fromStream(bills.values().stream()
                .filter(bill -> bill.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Bill::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.BillView> getBill(UUID billId, CashierRequestContext context) {
        return Mono.just(toView(requireBill(billId, context.requireOrganizationId())));
    }

    public Mono<CashierViews.BillView> payBill(UUID billId, CashierRequests.PayBillRequest request, CashierRequestContext context) {
        Bill bill = requireBill(billId, context.requireOrganizationId());
        ResolvedCashContext resolved = resolveCashContext(request.sessionId(), request.registerId(), context);
        return ensureCounterpartyThirdPartyId(bill.customerId(), "Customer " + bill.customerId(), "CUSTOMER", context)
                .flatMap(customerThirdPartyId -> resolveThirdPartyAccountingAccount(customerThirdPartyId, context)
                        .switchIfEmpty(Mono.just(""))
                        .flatMap(customerAccountingAccount -> {
                            WalletAccount registerAccount = ensureAccount(context.requireOrganizationId(), resolved.register().id(),
                                    resolved.register().label(), "REGISTER_CASH", bill.currency(), null);
                            WalletAccount updatedRegisterAccount = registerAccount.withBalance(registerAccount.balance().add(request.amount()));
                            storeAccount(updatedRegisterAccount);
                            BigDecimal paidAmount = bill.paidAmount().add(request.amount());
                            Bill updatedBill = bill.withPayment(paidAmount,
                                    paidAmount.compareTo(bill.totalAmount()) >= 0 ? "PAID" : "PARTIAL");
                            storeBill(updatedBill);
                            return accountingBookkeepingService.createCashRegisterPosting(
                        new yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests.CreateCashRegisterPostingRequest(
                                resolved.register().code(),
                                resolved.register().id(),
                                resolved.register().accountingAccountId(),
                                resolved.register().accountingAccountNumber(),
                                request.amount(),
                                bill.currency(),
                                "BILL_PAYMENT",
                                resolved.session() == null ? null : resolved.session().id(),
                                null,
                                customerAccountingAccount,
                                "Bill payment " + bill.reference()),
                        toAccountingContext(context))
                                    .map(posting -> {
                                        recordMovement(context.requireOrganizationId(),
                                                resolved.session() == null ? null : resolved.session().id(),
                                                resolved.register().id(),
                                                updatedRegisterAccount.id(),
                                                "BILL_PAYMENT",
                                                request.amount(),
                                                bill.currency(),
                                                bill.reference(),
                                                "POSTED",
                                                posting.id(),
                                                posting.postingType(),
                                                posting.registerAccountNumber(),
                                                posting.counterpartyAccountNumber());
                                        audit(context.requireOrganizationId(), "BILL_PAID", "BILL", updatedBill.id(), bill.reference());
                                        return toView(updatedBill);
                                    });
                        }));
    }

    public Mono<CashierViews.CashMovementView> createMovement(CashierRequests.CreateMovementRequest request,
            CashierRequestContext context) {
        WalletAccount explicitAccount = request.accountId() == null ? null : requireAccount(request.accountId(),
                context.requireOrganizationId());
        CashSession session = request.sessionId() == null ? null : requireSession(request.sessionId(), context.requireOrganizationId());
        CashRegister register = request.registerId() == null ? null : requireRegister(request.registerId(), context.requireOrganizationId());
        if (register == null && session != null) {
            register = requireRegister(session.registerId(), context.requireOrganizationId());
        }
        String normalizedType = request.type().trim().toUpperCase(Locale.ROOT);
        if (register == null || (!isCashInType(normalizedType) && !isCashOutType(normalizedType))) {
            CashMovement movement = recordMovement(
                    context.requireOrganizationId(),
                    session == null ? null : session.id(),
                    register == null ? null : register.id(),
                    explicitAccount == null ? null : explicitAccount.id(),
                    normalizedType,
                    request.amount(),
                    request.currency(),
                    request.reference(),
                    "POSTED",
                    null,
                    null,
                    null,
                    null);
            audit(context.requireOrganizationId(), "MOVEMENT_CREATED", "MOVEMENT", movement.id(), movement.reference());
            return Mono.just(toView(movement));
        }

        WalletAccount registerWallet = ensureAccount(context.requireOrganizationId(), register.id(), register.label(),
                "REGISTER_CASH", request.currency(), null);
        WalletAccount updatedRegisterWallet = registerWallet;
        if (isCashInType(normalizedType)) {
            updatedRegisterWallet = registerWallet.withBalance(registerWallet.balance().add(request.amount()));
        } else if (registerWallet.balance().compareTo(request.amount()) < 0) {
            return Mono.error(new IllegalArgumentException("insufficient register cash balance"));
        } else {
            updatedRegisterWallet = registerWallet.withBalance(registerWallet.balance().subtract(request.amount()));
        }
        storeAccount(updatedRegisterWallet);
        String postingType = isCashInType(normalizedType) ? "ENCASHMENT" : "DISBURSEMENT";
        CashRegister resolvedRegister = register;
        CashSession resolvedSession = session;
        WalletAccount finalRegisterWallet = updatedRegisterWallet;
        Mono<UUID> counterpartyThirdPartyIdMono;
        if (request.counterpartyThirdPartyId() != null) {
            counterpartyThirdPartyIdMono = Mono.just(request.counterpartyThirdPartyId());
        } else if (request.counterpartyActorId() != null) {
            counterpartyThirdPartyIdMono = ensureCounterpartyThirdPartyId(request.counterpartyActorId(),
                    "Actor " + request.counterpartyActorId(), isCashInType(normalizedType) ? "CUSTOMER" : "BENEFICIARY",
                    context);
        } else {
            counterpartyThirdPartyIdMono = Mono.just(explicitAccount == null ? null : explicitAccount.linkedThirdPartyId());
        }
        return counterpartyThirdPartyIdMono
                .flatMap(counterpartyThirdPartyId -> resolveThirdPartyAccountingAccount(counterpartyThirdPartyId, context))
                .switchIfEmpty(Mono.just(""))
                .flatMap(counterpartyAccountingAccount -> accountingBookkeepingService.createCashRegisterPosting(
                        new yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests.CreateCashRegisterPostingRequest(
                                resolvedRegister.code(),
                                resolvedRegister.id(),
                                resolvedRegister.accountingAccountId(),
                                resolvedRegister.accountingAccountNumber(),
                                request.amount(),
                                request.currency(),
                                postingType,
                                resolvedSession == null ? null : resolvedSession.id(),
                                null,
                                counterpartyAccountingAccount,
                                request.note()),
                        toAccountingContext(context)))
                .map(posting -> {
                    CashMovement movement = recordMovement(
                            context.requireOrganizationId(),
                            resolvedSession == null ? null : resolvedSession.id(),
                            resolvedRegister.id(),
                            finalRegisterWallet.id(),
                            normalizedType,
                            request.amount(),
                            request.currency(),
                            request.reference(),
                            "POSTED",
                            posting.id(),
                            posting.postingType(),
                            posting.registerAccountNumber(),
                            posting.counterpartyAccountNumber());
                    audit(context.requireOrganizationId(), "MOVEMENT_CREATED", "MOVEMENT", movement.id(),
                            movement.reference());
                    return toView(movement);
                });
    }

    public Flux<CashierViews.CashMovementView> listMovements(CashierRequestContext context) {
        return Flux.fromStream(movements.values().stream()
                .filter(movement -> movement.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashMovement::createdAt).reversed()))
                .map(this::toView);
    }

    public Flux<CashierViews.CashMovementView> listRecentTransactions(CashierRequestContext context) {
        return listMovements(context).take(10);
    }

    public Mono<CashierViews.CashMovementView> attachMovementAccount(UUID movementId, UUID accountId, CashierRequestContext context) {
        requireAccount(accountId, context.requireOrganizationId());
        CashMovement updated = requireMovement(movementId, context.requireOrganizationId()).withAccount(accountId);
        storeMovement(updated);
        audit(context.requireOrganizationId(), "MOVEMENT_ACCOUNT_ATTACHED", "MOVEMENT", updated.id(), accountId.toString());
        return Mono.just(toView(updated));
    }

    public Flux<CashierViews.CashReconciliationView> listReconciliations(CashierRequestContext context) {
        return Flux.fromStream(reconciliations.values().stream()
                .filter(reconciliation -> reconciliation.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashReconciliation::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.CashReconciliationView> reviewReconciliation(UUID reconciliationId,
            CashierRequests.ReviewReconciliationRequest request,
            CashierRequestContext context) {
        CashReconciliation updated = requireReconciliation(reconciliationId, context.requireOrganizationId())
                .withReview(request.review());
        storeReconciliation(updated);
        audit(context.requireOrganizationId(), "RECONCILIATION_REVIEWED", "RECONCILIATION", updated.id(), request.review());
        return Mono.just(toView(updated));
    }

    public Mono<CashierViews.CashReconciliationView> justifyReconciliation(UUID reconciliationId,
            CashierRequests.JustifyReconciliationRequest request,
            CashierRequestContext context) {
        CashReconciliation updated = requireReconciliation(reconciliationId, context.requireOrganizationId())
                .withJustification(request.justification());
        storeReconciliation(updated);
        audit(context.requireOrganizationId(), "RECONCILIATION_JUSTIFIED", "RECONCILIATION", updated.id(),
                request.justification());
        return Mono.just(toView(updated));
    }

    public Flux<CashierViews.CashAuditEntryView> listAuditEntries(CashierRequestContext context) {
        return Flux.fromStream(auditEntries.values().stream()
                .filter(entry -> entry.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(AuditEntry::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.CashAuditEntryView> createAuditEntry(CashierRequests.CreateAuditEntryRequest request,
            CashierRequestContext context) {
        AuditEntry entry = new AuditEntry(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.action(),
                request.targetType(),
                request.targetId(),
                request.details(),
                Instant.now());
        storeAuditEntry(entry);
        return Mono.just(toView(entry));
    }

    public Flux<CashierViews.CashNotificationView> listNotifications(CashierRequestContext context) {
        return Flux.fromStream(notifications.values().stream()
                .filter(notification -> notification.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashNotification::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.CashNotificationView> createNotification(CashierRequests.CreateNotificationRequest request,
            CashierRequestContext context) {
        CashNotification notification = new CashNotification(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.channel(),
                request.subject(),
                request.recipient(),
                "SENT",
                Instant.now());
        storeNotification(notification);
        audit(context.requireOrganizationId(), "NOTIFICATION_SENT", "NOTIFICATION", notification.id(), notification.subject());
        return Mono.just(toView(notification));
    }

    public Mono<CashierViews.CashNotificationView> notifyUnauthorized(CashierRequests.NotifyUnauthorizedRequest request,
            CashierRequestContext context) {
        return createNotification(new CashierRequests.CreateNotificationRequest(
                "SECURITY",
                request.message(),
                request.endpoint() == null ? "unknown-endpoint" : request.endpoint()), context);
    }

    public Flux<CashierViews.CashDocumentView> listDocuments(CashierRequestContext context) {
        return Flux.fromStream(documents.values().stream()
                .filter(document -> document.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashDocument::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<CashierViews.CashDocumentView> createDocument(CashierRequests.CreateDocumentRequest request,
            CashierRequestContext context) {
        CashDocument document = new CashDocument(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.type(),
                request.targetId(),
                request.reference(),
                Instant.now());
        storeDocument(document);
        audit(context.requireOrganizationId(), "DOCUMENT_CREATED", "DOCUMENT", document.id(), document.reference());
        return Mono.just(toView(document));
    }

    public Mono<CashierViews.CashDashboardView> dashboard(CashierRequestContext context) {
        long registerCount = cashRegisters.values().stream()
                .filter(register -> register.organizationId().equals(context.requireOrganizationId()))
                .count();
        long activeSessionCount = sessions.values().stream()
                .filter(session -> session.organizationId().equals(context.requireOrganizationId()))
                .filter(session -> "OPEN".equals(session.status()))
                .count();
        long pendingFundRequestCount = fundRequests.values().stream()
                .filter(request -> request.organizationId().equals(context.requireOrganizationId()))
                .filter(request -> "PENDING".equals(request.status()))
                .count();
        long pendingBillCount = bills.values().stream()
                .filter(bill -> bill.organizationId().equals(context.requireOrganizationId()))
                .filter(bill -> !"PAID".equals(bill.status()))
                .count();
        BigDecimal globalBalance = accounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .map(WalletAccount::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        long movementCountToday = movements.values().stream()
                .filter(movement -> movement.organizationId().equals(context.requireOrganizationId()))
                .filter(movement -> movement.createdAt().isAfter(todayStart))
                .count();
        return Mono.just(new CashierViews.CashDashboardView(
                registerCount,
                activeSessionCount,
                pendingFundRequestCount,
                pendingBillCount,
                globalBalance,
                movementCountToday));
    }

    public Mono<CashierViews.CashReportView> reportTransactions(CashierRequestContext context) {
        return listMovements(context).collectList()
                .flatMap(movements -> persistReport("TRANSACTIONS", Map.of("movements", movements), context));
    }

    public Mono<CashierViews.CashReportView> reportRegister(UUID registerId, CashierRequestContext context) {
        return listMovements(context)
                .filter(movement -> registerId.equals(movement.registerId()))
                .collectList()
                .flatMap(movementViews -> getCashRegister(registerId, context)
                        .flatMap(register -> persistReport("REGISTER", Map.of(
                                "register", register,
                                "movements", movementViews), context)));
    }

    public Mono<CashierViews.CashReportView> reportSession(UUID sessionId, CashierRequestContext context) {
        return Mono.zip(
                Mono.just(toView(requireSession(sessionId, context.requireOrganizationId()))),
                listMovements(context).filter(movement -> sessionId.equals(movement.sessionId())).collectList(),
                listReconciliations(context).filter(reconciliation -> sessionId.equals(reconciliation.sessionId())).collectList())
                .flatMap(tuple -> persistReport("SESSION", Map.of(
                        "session", tuple.getT1(),
                        "movements", tuple.getT2(),
                        "reconciliations", tuple.getT3()), context));
    }

    public Mono<CashierViews.CashReportView> reportAudit(CashierRequestContext context) {
        return listAuditEntries(context).collectList()
                .flatMap(entries -> persistReport("AUDIT", Map.of("entries", entries), context));
    }

    public List<CashierViews.DenominationView> denominations() {
        return List.of(
                new CashierViews.DenominationView("XAF", new BigDecimal("10000"), 0),
                new CashierViews.DenominationView("XAF", new BigDecimal("5000"), 0),
                new CashierViews.DenominationView("XAF", new BigDecimal("2000"), 0),
                new CashierViews.DenominationView("XAF", new BigDecimal("1000"), 0),
                new CashierViews.DenominationView("XAF", new BigDecimal("500"), 0),
                new CashierViews.DenominationView("XAF", new BigDecimal("100"), 0));
    }

    public Mono<CashierViews.CashierLookupView> adminLookup(CashierRequestContext context) {
        return Mono.zip(
                listCashiers(context).collectList(),
                listAccounts(context).collectList())
                .map(tuple -> new CashierViews.CashierLookupView(
                        List.of(),
                        List.of(),
                        tuple.getT1(),
                        tuple.getT2(),
                        List.of()));
    }

    public Mono<CashierViews.CashierLookupView> cashierLookup(CashierRequestContext context) {
        return adminLookup(context);
    }

    public Mono<Map<String, Object>> customerLookup(CashierRequestContext context) {
        return Mono.just(Map.of("customers", List.of()));
    }

    public Mono<Map<String, Object>> organizationLookup(CashierRequestContext context) {
        return Mono.just(Map.of(
                "organizations", List.of(),
                "agencies", List.of()));
    }

    public Mono<CashierViews.CashierProfileView> upsertSelfProfile(String email,
            CashierRequests.UpdateMyProfileRequest request,
            CashierRequestContext context) {
        CashierProfile existing = profiles.values().stream()
                .filter(profile -> profile.organizationId().equals(context.requireOrganizationId()))
                .filter(profile -> email != null && email.equalsIgnoreCase(profile.email()))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            CashierProfile created = new CashierProfile(
                    UUID.randomUUID(),
                    context.requireOrganizationId(),
                    null,
                    null,
                    request.email() == null || request.email().isBlank() ? email : request.email(),
                    request.displayName(),
                    KIND_SELF,
                    true,
                    Instant.now());
            storeProfile(created);
            audit(context.requireOrganizationId(), "SELF_PROFILE_CREATED", "PROFILE", created.id(), created.email());
            return Mono.just(toView(created));
        }
        CashierProfile updated = existing.withUpdate(existing.kernelUserId(),
                request.email() == null || request.email().isBlank() ? existing.email() : request.email(),
                request.displayName(),
                existing.agencyId(),
                existing.kind(),
                existing.active());
        storeProfile(updated);
        audit(context.requireOrganizationId(), "SELF_PROFILE_UPDATED", "PROFILE", updated.id(), updated.email());
        return Mono.just(toView(updated));
    }

    public Mono<CashierViews.CashierProfileView> findProfileByEmail(String email, CashierRequestContext context) {
        return Flux.fromStream(profiles.values().stream()
                .filter(profile -> profile.organizationId().equals(context.requireOrganizationId()))
                .filter(profile -> email != null && email.equalsIgnoreCase(profile.email())))
                .map(this::toView)
                .next();
    }

    private Mono<CashierViews.CashReportView> persistReport(String reportType, Map<String, Object> payload,
            CashierRequestContext context) {
        CashierViews.CashReportView report = new CashierViews.CashReportView(UUID.randomUUID(), reportType, payload, Instant.now());
        storeDocument(new CashDocument(report.id(), context.requireOrganizationId(), "REPORT", report.id(),
                reportType + "-" + report.generatedAt().toEpochMilli(), report.generatedAt()));
        audit(context.requireOrganizationId(), "REPORT_GENERATED", "REPORT", report.id(), reportType);
        return Mono.just(report);
    }

    private CashMovement recordMovement(UUID organizationId,
            UUID sessionId,
            UUID registerId,
            UUID accountId,
            String type,
            BigDecimal amount,
            String currency,
            String reference,
            String status,
            UUID accountingPostingId,
            String accountingEntryType,
            String registerAccountNumber,
            String counterpartyAccountNumber) {
        CashMovement movement = new CashMovement(
                UUID.randomUUID(),
                organizationId,
                sessionId,
                registerId,
                accountId,
                type,
                amount,
                currency,
                reference,
                status,
                accountingPostingId,
                accountingEntryType,
                registerAccountNumber,
                counterpartyAccountNumber,
                Instant.now());
        storeMovement(movement);
        return movement;
    }

    private void audit(UUID organizationId, String action, String targetType, UUID targetId, String details) {
        AuditEntry entry = new AuditEntry(
                UUID.randomUUID(),
                organizationId,
                action,
                targetType,
                targetId,
                details,
                Instant.now());
        storeAuditEntry(entry);
    }

    private CashRegister requireRegister(UUID registerId, UUID organizationId) {
        CashRegister register = cashRegisters.get(registerId);
        if (register == null || !register.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("cash register not found");
        }
        return register;
    }

    private CashierProfile requireProfile(UUID profileId, UUID organizationId) {
        CashierProfile profile = profiles.get(profileId);
        if (profile == null || !profile.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("profile not found");
        }
        return profile;
    }

    private CashSession requireSession(UUID sessionId, UUID organizationId) {
        CashSession session = sessions.get(sessionId);
        if (session == null || !session.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("session not found");
        }
        return session;
    }

    private WalletAccount requireAccount(UUID accountId, UUID organizationId) {
        WalletAccount account = accounts.get(accountId);
        if (account == null || !account.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("account not found");
        }
        return account;
    }

    private Bill requireBill(UUID billId, UUID organizationId) {
        Bill bill = bills.get(billId);
        if (bill == null || !bill.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("bill not found");
        }
        return bill;
    }

    private CashMovement requireMovement(UUID movementId, UUID organizationId) {
        CashMovement movement = movements.get(movementId);
        if (movement == null || !movement.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("movement not found");
        }
        return movement;
    }

    private CashReconciliation requireReconciliation(UUID reconciliationId, UUID organizationId) {
        CashReconciliation reconciliation = reconciliations.get(reconciliationId);
        if (reconciliation == null || !reconciliation.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("reconciliation not found");
        }
        return reconciliation;
    }

    private WalletAccount ensureAccount(UUID organizationId,
            UUID ownerId,
            String ownerName,
            String type,
            String currency,
            UUID linkedThirdPartyId) {
        WalletAccount existing = accounts.values().stream()
                .filter(account -> account.organizationId().equals(organizationId))
                .filter(account -> account.ownerId().equals(ownerId))
                .filter(account -> account.type().equals(type))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            if (existing.linkedThirdPartyId() == null && linkedThirdPartyId != null) {
                WalletAccount enriched = new WalletAccount(existing.id(), existing.organizationId(), existing.ownerId(),
                        existing.ownerName(), existing.number(), existing.balance(), existing.currency(), existing.type(),
                        linkedThirdPartyId);
                storeAccount(enriched);
                return enriched;
            }
            return existing;
        }
        WalletAccount account = new WalletAccount(
                UUID.randomUUID(),
                organizationId,
                ownerId,
                ownerName,
                "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                BigDecimal.ZERO,
                currency,
                type,
                linkedThirdPartyId);
        storeAccount(account);
        return account;
    }

    private ResolvedCashContext resolveCashContext(UUID sessionId, UUID registerId, CashierRequestContext context) {
        CashSession session = sessionId == null ? null : requireSession(sessionId, context.requireOrganizationId());
        CashRegister register = registerId == null ? null : requireRegister(registerId, context.requireOrganizationId());
        if (register == null && session != null) {
            register = requireRegister(session.registerId(), context.requireOrganizationId());
        }
        if (register == null) {
            register = sessions.values().stream()
                    .filter(existing -> existing.organizationId().equals(context.requireOrganizationId()))
                    .filter(existing -> "OPEN".equals(existing.status()))
                    .findFirst()
                    .map(existing -> requireRegister(existing.registerId(), context.requireOrganizationId()))
                    .orElseThrow(() -> new IllegalArgumentException("an active cash session or register is required"));
            if (session == null) {
                CashRegister resolvedRegister = register;
                session = sessions.values().stream()
                        .filter(existing -> existing.organizationId().equals(context.requireOrganizationId()))
                        .filter(existing -> existing.registerId().equals(resolvedRegister.id()))
                        .filter(existing -> "OPEN".equals(existing.status()))
                        .findFirst()
                        .orElse(null);
            }
        }
        return new ResolvedCashContext(register, session);
    }

    private boolean isCashInType(String type) {
        return "ENCASHMENT".equals(type)
                || "CASH_IN".equals(type)
                || "BILL_PAYMENT".equals(type)
                || "CUSTOMER_PAYMENT".equals(type)
                || "SALE".equals(type);
    }

    private boolean isCashOutType(String type) {
        return "DISBURSEMENT".equals(type)
                || "CASH_OUT".equals(type)
                || "WITHDRAW".equals(type)
                || "PAYOUT".equals(type)
                || "FUND_RELEASE".equals(type);
    }

    private Mono<UUID> ensureCounterpartyThirdPartyId(UUID candidateId, String defaultName, String role,
            CashierRequestContext context) {
        if (candidateId == null) {
            return Mono.empty();
        }
        return getThirdPartyUseCase.getThirdParty(candidateId)
                .map(tp -> tp.id())
                .onErrorResume(ex -> ensureActorFinancialProfileUseCase.ensureActorFinancialProfile(
                                new EnsureActorFinancialProfileCommand(
                                        context.tenantId(),
                                        context.requireOrganizationId(),
                                        candidateId,
                                        role,
                                        null,
                                        defaultName))
                        .map(tp -> tp.id()));
    }

    private Mono<String> resolveThirdPartyAccountingAccount(UUID thirdPartyId, CashierRequestContext context) {
        if (thirdPartyId == null) {
            return Mono.empty();
        }
        return getThirdPartyUseCase.getThirdParty(thirdPartyId)
                .map(thirdParty -> thirdParty.accountingAccount())
                .onErrorResume(ex -> Mono.empty());
    }

    private AccountingExtensionRequestContext toAccountingContext(CashierRequestContext context) {
        return new AccountingExtensionRequestContext(
                context.tenantId(),
                context.organizationId(),
                context.agencyId(),
                context.userId(),
                context.actorId());
    }

    private void storeRegister(CashRegister register) {
        cashRegisters.put(register.id(), register);
        cashRegisterRepository.save(toEntity(register)).subscribe();
    }

    private void deleteRegister(UUID registerId) {
        cashRegisters.remove(registerId);
        cashRegisterRepository.deleteById(registerId).subscribe();
    }

    private void storeProfile(CashierProfile profile) {
        profiles.put(profile.id(), profile);
        cashierProfileRepository.save(toEntity(profile)).subscribe();
    }

    private void deleteProfile(UUID profileId) {
        profiles.remove(profileId);
        cashierProfileRepository.deleteById(profileId).subscribe();
    }

    private void storeAssignment(CashierAssignment assignment) {
        assignments.put(assignment.id(), assignment);
        cashierAssignmentRepository.save(toEntity(assignment)).subscribe();
    }

    private void deleteAssignmentInternal(UUID assignmentId) {
        assignments.remove(assignmentId);
        cashierAssignmentRepository.deleteById(assignmentId).subscribe();
    }

    private void storeSession(CashSession session) {
        sessions.put(session.id(), session);
        cashSessionRepository.save(toEntity(session)).subscribe();
    }

    private void deleteSession(UUID sessionId) {
        sessions.remove(sessionId);
        cashSessionRepository.deleteById(sessionId).subscribe();
    }

    private void storeAccount(WalletAccount account) {
        accounts.put(account.id(), account);
        walletAccountRepository.save(toEntity(account)).subscribe();
    }

    private void storeFundRequest(FundRequest request) {
        fundRequests.put(request.id(), request);
        fundRequestRepository.save(toEntity(request)).subscribe();
    }

    private void storeBill(Bill bill) {
        bills.put(bill.id(), bill);
        billRepository.save(toEntity(bill)).subscribe();
    }

    private void storeMovement(CashMovement movement) {
        movements.put(movement.id(), movement);
        cashMovementRepository.save(toEntity(movement)).subscribe();
    }

    private void storeReconciliation(CashReconciliation reconciliation) {
        reconciliations.put(reconciliation.id(), reconciliation);
        cashReconciliationRepository.save(toEntity(reconciliation)).subscribe();
    }

    private void storeAuditEntry(AuditEntry entry) {
        auditEntries.put(entry.id(), entry);
        auditEntryRepository.save(toEntity(entry)).subscribe();
    }

    private void storeNotification(CashNotification notification) {
        notifications.put(notification.id(), notification);
        cashNotificationRepository.save(toEntity(notification)).subscribe();
    }

    private void storeDocument(CashDocument document) {
        documents.put(document.id(), document);
        cashDocumentRepository.save(toEntity(document)).subscribe();
    }

    private CashRegisterEntity toEntity(CashRegister register) {
        return new CashRegisterEntity(register.id(), register.organizationId(), register.agencyId(), register.code(),
                register.label(), register.status(), register.assignedCashierId(), register.accountingAccountId(),
                register.accountingAccountNumber(), register.createdAt());
    }

    private CashierProfileEntity toEntity(CashierProfile profile) {
        return new CashierProfileEntity(profile.id(), profile.organizationId(), profile.agencyId(),
                profile.kernelUserId(), profile.email(), profile.fullName(), profile.kind(), profile.active(),
                profile.createdAt());
    }

    private CashierAssignmentEntity toEntity(CashierAssignment assignment) {
        return new CashierAssignmentEntity(assignment.id(), assignment.organizationId(), assignment.agencyId(),
                assignment.cashierId(), assignment.assignedAt());
    }

    private CashSessionEntity toEntity(CashSession session) {
        return new CashSessionEntity(session.id(), session.organizationId(), session.agencyId(), session.registerId(),
                session.cashierId(), session.status(), session.openingAmount(), session.closingAmount(),
                session.currency(), session.openedAt(), session.closedAt(), session.locked(), session.note());
    }

    private WalletAccountEntity toEntity(WalletAccount account) {
        return new WalletAccountEntity(account.id(), account.organizationId(), account.ownerId(), account.ownerName(),
                account.number(), account.balance(), account.currency(), account.type(), account.linkedThirdPartyId());
    }

    private FundRequestEntity toEntity(FundRequest request) {
        return new FundRequestEntity(request.id(), request.organizationId(), request.registerId(), request.cashierId(),
                request.amount(), request.status(), request.reason(), request.createdAt());
    }

    private BillEntity toEntity(Bill bill) {
        return new BillEntity(bill.id(), bill.organizationId(), bill.customerId(), bill.reference(), bill.totalAmount(),
                bill.paidAmount(), bill.currency(), bill.status(), bill.createdAt());
    }

    private CashMovementEntity toEntity(CashMovement movement) {
        return new CashMovementEntity(movement.id(), movement.organizationId(), movement.sessionId(),
                movement.registerId(), movement.accountId(), movement.type(), movement.amount(), movement.currency(),
                movement.reference(), movement.status(), movement.accountingPostingId(), movement.accountingEntryType(),
                movement.registerAccountNumber(), movement.counterpartyAccountNumber(), movement.createdAt());
    }

    private CashReconciliationEntity toEntity(CashReconciliation reconciliation) {
        return new CashReconciliationEntity(reconciliation.id(), reconciliation.organizationId(),
                reconciliation.sessionId(), reconciliation.registerId(), reconciliation.status(),
                reconciliation.review(), reconciliation.justification(), reconciliation.createdAt(),
                reconciliation.reviewedAt());
    }

    private AuditEntryEntity toEntity(AuditEntry entry) {
        return new AuditEntryEntity(entry.id(), entry.organizationId(), entry.action(), entry.targetType(),
                entry.targetId(), entry.details(), entry.createdAt());
    }

    private CashNotificationEntity toEntity(CashNotification notification) {
        return new CashNotificationEntity(notification.id(), notification.organizationId(), notification.channel(),
                notification.subject(), notification.recipient(), notification.status(), notification.createdAt());
    }

    private CashDocumentEntity toEntity(CashDocument document) {
        return new CashDocumentEntity(document.id(), document.organizationId(), document.type(), document.targetId(),
                document.reference(), document.createdAt());
    }

    private CashRegister toDomain(CashRegisterEntity entity) {
        return new CashRegister(entity.id(), entity.organizationId(), entity.agencyId(), entity.code(), entity.label(),
                entity.status(), entity.assignedCashierId(), entity.accountingAccountId(),
                entity.accountingAccountNumber(), entity.createdAt());
    }

    private CashierProfile toDomain(CashierProfileEntity entity) {
        return new CashierProfile(entity.id(), entity.organizationId(), entity.agencyId(), entity.kernelUserId(),
                entity.email(), entity.fullName(), entity.kind(), entity.active(), entity.createdAt());
    }

    private CashierAssignment toDomain(CashierAssignmentEntity entity) {
        return new CashierAssignment(entity.id(), entity.organizationId(), entity.agencyId(), entity.cashierId(),
                entity.assignedAt());
    }

    private CashSession toDomain(CashSessionEntity entity) {
        return new CashSession(entity.id(), entity.organizationId(), entity.agencyId(), entity.registerId(),
                entity.cashierId(), entity.status(), entity.openingAmount(), entity.closingAmount(), entity.currency(),
                entity.openedAt(), entity.closedAt(), entity.locked(), entity.note());
    }

    private WalletAccount toDomain(WalletAccountEntity entity) {
        return new WalletAccount(entity.id(), entity.organizationId(), entity.ownerId(), entity.ownerName(),
                entity.number(), entity.balance(), entity.currency(), entity.type(), entity.linkedThirdPartyId());
    }

    private FundRequest toDomain(FundRequestEntity entity) {
        return new FundRequest(entity.id(), entity.organizationId(), entity.registerId(), entity.cashierId(),
                entity.amount(), entity.status(), entity.reason(), entity.createdAt());
    }

    private Bill toDomain(BillEntity entity) {
        return new Bill(entity.id(), entity.organizationId(), entity.customerId(), entity.reference(),
                entity.totalAmount(), entity.paidAmount(), entity.currency(), entity.status(), entity.createdAt());
    }

    private CashMovement toDomain(CashMovementEntity entity) {
        return new CashMovement(entity.id(), entity.organizationId(), entity.sessionId(), entity.registerId(),
                entity.accountId(), entity.type(), entity.amount(), entity.currency(), entity.reference(),
                entity.status(), entity.accountingPostingId(), entity.accountingEntryType(),
                entity.registerAccountNumber(), entity.counterpartyAccountNumber(), entity.createdAt());
    }

    private CashReconciliation toDomain(CashReconciliationEntity entity) {
        return new CashReconciliation(entity.id(), entity.organizationId(), entity.sessionId(), entity.registerId(),
                entity.status(), entity.review(), entity.justification(), entity.createdAt(), entity.reviewedAt());
    }

    private AuditEntry toDomain(AuditEntryEntity entity) {
        return new AuditEntry(entity.id(), entity.organizationId(), entity.action(), entity.targetType(),
                entity.targetId(), entity.details(), entity.createdAt());
    }

    private CashNotification toDomain(CashNotificationEntity entity) {
        return new CashNotification(entity.id(), entity.organizationId(), entity.channel(), entity.subject(),
                entity.recipient(), entity.status(), entity.createdAt());
    }

    private CashDocument toDomain(CashDocumentEntity entity) {
        return new CashDocument(entity.id(), entity.organizationId(), entity.type(), entity.targetId(),
                entity.reference(), entity.createdAt());
    }

    private String normalizeKind(String kind) {
        return kind == null || kind.isBlank()
                ? KIND_CASHIER
                : kind.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private CashierViews.CashRegisterView toView(CashRegister register) {
        return new CashierViews.CashRegisterView(
                register.id(),
                register.organizationId(),
                register.agencyId(),
                register.code(),
                register.label(),
                register.status(),
                register.assignedCashierId(),
                register.accountingAccountId(),
                register.accountingAccountNumber(),
                register.createdAt());
    }

    private CashierViews.CashierProfileView toView(CashierProfile profile) {
        return new CashierViews.CashierProfileView(
                profile.id(),
                profile.organizationId(),
                profile.agencyId(),
                profile.kernelUserId(),
                profile.email(),
                profile.fullName(),
                profile.kind(),
                profile.active(),
                profile.createdAt());
    }

    private CashierViews.CashierAssignmentView toView(CashierAssignment assignment) {
        return new CashierViews.CashierAssignmentView(
                assignment.id(),
                assignment.organizationId(),
                assignment.agencyId(),
                assignment.cashierId(),
                assignment.assignedAt());
    }

    private CashierViews.CashierSessionView toView(CashSession session) {
        return new CashierViews.CashierSessionView(
                session.id(),
                session.organizationId(),
                session.agencyId(),
                session.registerId(),
                session.cashierId(),
                session.status(),
                session.openingAmount(),
                session.closingAmount(),
                session.currency(),
                session.openedAt(),
                session.closedAt(),
                session.locked());
    }

    private CashierViews.WalletAccountView toView(WalletAccount account) {
        return new CashierViews.WalletAccountView(
                account.id(),
                account.organizationId(),
                account.ownerId(),
                account.ownerName(),
                account.number(),
                account.balance(),
                account.currency(),
                account.type(),
                account.linkedThirdPartyId());
    }

    private CashierViews.FundRequestView toView(FundRequest request) {
        return new CashierViews.FundRequestView(
                request.id(),
                request.organizationId(),
                request.registerId(),
                request.cashierId(),
                request.amount(),
                request.status(),
                request.reason(),
                request.createdAt());
    }

    private CashierViews.BillView toView(Bill bill) {
        return new CashierViews.BillView(
                bill.id(),
                bill.organizationId(),
                bill.customerId(),
                bill.reference(),
                bill.totalAmount(),
                bill.paidAmount(),
                bill.currency(),
                bill.status(),
                bill.createdAt());
    }

    private CashierViews.CashMovementView toView(CashMovement movement) {
        return new CashierViews.CashMovementView(
                movement.id(),
                movement.organizationId(),
                movement.sessionId(),
                movement.registerId(),
                movement.accountId(),
                movement.type(),
                movement.amount(),
                movement.currency(),
                movement.reference(),
                movement.status(),
                movement.accountingPostingId(),
                movement.accountingEntryType(),
                movement.registerAccountNumber(),
                movement.counterpartyAccountNumber(),
                movement.createdAt());
    }

    private CashierViews.CashReconciliationView toView(CashReconciliation reconciliation) {
        return new CashierViews.CashReconciliationView(
                reconciliation.id(),
                reconciliation.organizationId(),
                reconciliation.sessionId(),
                reconciliation.registerId(),
                reconciliation.status(),
                reconciliation.review(),
                reconciliation.justification(),
                reconciliation.createdAt(),
                reconciliation.reviewedAt());
    }

    private CashierViews.CashAuditEntryView toView(AuditEntry entry) {
        return new CashierViews.CashAuditEntryView(
                entry.id(),
                entry.organizationId(),
                entry.action(),
                entry.targetType(),
                entry.targetId(),
                entry.details(),
                entry.createdAt());
    }

    private CashierViews.CashNotificationView toView(CashNotification notification) {
        return new CashierViews.CashNotificationView(
                notification.id(),
                notification.organizationId(),
                notification.channel(),
                notification.subject(),
                notification.recipient(),
                notification.status(),
                notification.createdAt());
    }

    private CashierViews.CashDocumentView toView(CashDocument document) {
        return new CashierViews.CashDocumentView(
                document.id(),
                document.organizationId(),
                document.type(),
                document.targetId(),
                document.reference(),
                document.createdAt());
    }

    record CashRegister(UUID id, UUID organizationId, UUID agencyId, String code, String label, String status,
            UUID assignedCashierId, UUID accountingAccountId, String accountingAccountNumber, Instant createdAt) {
        CashRegister withUpdate(String nextCode, String nextLabel, UUID nextAgencyId, String nextStatus) {
            return new CashRegister(id, organizationId, nextAgencyId, nextCode.trim(), nextLabel.trim(),
                    nextStatus.trim(), assignedCashierId, accountingAccountId, accountingAccountNumber, createdAt);
        }

        CashRegister withAssignedCashier(UUID cashierId) {
            return new CashRegister(id, organizationId, agencyId, code, label, status, cashierId, accountingAccountId,
                    accountingAccountNumber, createdAt);
        }
    }

    record CashierProfile(UUID id, UUID organizationId, UUID agencyId, UUID kernelUserId, String email, String fullName,
            String kind, boolean active, Instant createdAt) {
        CashierProfile withUpdate(UUID nextKernelUserId, String nextEmail, String nextFullName, UUID nextAgencyId,
                String nextKind, boolean nextActive) {
            return new CashierProfile(id, organizationId, nextAgencyId, nextKernelUserId, nextEmail,
                    nextFullName.trim(), nextKind, nextActive, createdAt);
        }
    }

    record CashierAssignment(UUID id, UUID organizationId, UUID agencyId, UUID cashierId, Instant assignedAt) {
    }

    record CashSession(UUID id, UUID organizationId, UUID agencyId, UUID registerId, UUID cashierId, String status,
            BigDecimal openingAmount, BigDecimal closingAmount, String currency, Instant openedAt, Instant closedAt,
            boolean locked, String note) {
        CashSession withClose(BigDecimal nextClosingAmount, String nextNote) {
            return new CashSession(id, organizationId, agencyId, registerId, cashierId, "CLOSED", openingAmount,
                    nextClosingAmount, currency, openedAt, Instant.now(), locked, nextNote);
        }

        CashSession withLock(boolean nextLocked) {
            return new CashSession(id, organizationId, agencyId, registerId, cashierId, status, openingAmount,
                    closingAmount, currency, openedAt, closedAt, nextLocked, note);
        }
    }

    record WalletAccount(UUID id, UUID organizationId, UUID ownerId, String ownerName, String number,
            BigDecimal balance, String currency, String type, UUID linkedThirdPartyId) {
        WalletAccount withBalance(BigDecimal nextBalance) {
            return new WalletAccount(id, organizationId, ownerId, ownerName, number, nextBalance, currency, type,
                    linkedThirdPartyId);
        }
    }

    record FundRequest(UUID id, UUID organizationId, UUID registerId, UUID cashierId, BigDecimal amount, String status,
            String reason, Instant createdAt) {
    }

    record Bill(UUID id, UUID organizationId, UUID customerId, String reference, BigDecimal totalAmount,
            BigDecimal paidAmount, String currency, String status, Instant createdAt) {
        Bill withPayment(BigDecimal nextPaidAmount, String nextStatus) {
            return new Bill(id, organizationId, customerId, reference, totalAmount, nextPaidAmount, currency, nextStatus,
                    createdAt);
        }
    }

    record CashMovement(UUID id, UUID organizationId, UUID sessionId, UUID registerId, UUID accountId, String type,
            BigDecimal amount, String currency, String reference, String status, UUID accountingPostingId,
            String accountingEntryType, String registerAccountNumber, String counterpartyAccountNumber,
            Instant createdAt) {
        CashMovement withAccount(UUID nextAccountId) {
            return new CashMovement(id, organizationId, sessionId, registerId, nextAccountId, type, amount, currency,
                    reference, status, accountingPostingId, accountingEntryType, registerAccountNumber,
                    counterpartyAccountNumber, createdAt);
        }
    }

    record CashReconciliation(UUID id, UUID organizationId, UUID sessionId, UUID registerId, String status, String review,
            String justification, Instant createdAt, Instant reviewedAt) {
        CashReconciliation withReview(String nextReview) {
            return new CashReconciliation(id, organizationId, sessionId, registerId, "REVIEWED", nextReview,
                    justification, createdAt, Instant.now());
        }

        CashReconciliation withJustification(String nextJustification) {
            return new CashReconciliation(id, organizationId, sessionId, registerId, "JUSTIFIED", review,
                    nextJustification, createdAt, Instant.now());
        }
    }

    record AuditEntry(UUID id, UUID organizationId, String action, String targetType, UUID targetId, String details,
            Instant createdAt) {
    }

    record CashNotification(UUID id, UUID organizationId, String channel, String subject, String recipient, String status,
            Instant createdAt) {
    }

    record CashDocument(UUID id, UUID organizationId, String type, UUID targetId, String reference, Instant createdAt) {
    }

    private record ResolvedCashContext(CashRegister register, CashSession session) {
    }

}
