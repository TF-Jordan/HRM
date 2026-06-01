package yowyob.comops.api.accounting.extension.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;
import yowyob.comops.api.accounting.extension.web.AccountingLegacyDtos;
import yowyob.comops.api.accounting.extension.web.AccountingOperationsViews;

@Service
public class AccountingLegacyReportingService {

    private final AccountingBookkeepingService accountingBookkeepingService;
    private final AccountingOperationsService accountingOperationsService;

    public AccountingLegacyReportingService(AccountingBookkeepingService accountingBookkeepingService,
            AccountingOperationsService accountingOperationsService) {
        this.accountingBookkeepingService = accountingBookkeepingService;
        this.accountingOperationsService = accountingOperationsService;
    }

    public Mono<AccountingLegacyDtos.BilanDto> generateBilan(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        validateDateRange(startDate, endDate);
        return loadSnapshot(context, startDate, endDate)
                .map(snapshot -> {
                    List<AccountingLegacyDtos.ReportItemDto> actifs = snapshot.accounts().values().stream()
                            .map(account -> toAccountAggregate(account, snapshot.periodLines().getOrDefault(account.id(), List.of())))
                            .filter(aggregate -> aggregate.hasActivity())
                            .filter(aggregate -> isAsset(aggregate.account()))
                            .map(this::toReportItem)
                            .toList();
                    List<AccountingLegacyDtos.ReportItemDto> passifs = snapshot.accounts().values().stream()
                            .map(account -> toAccountAggregate(account, snapshot.periodLines().getOrDefault(account.id(), List.of())))
                            .filter(aggregate -> aggregate.hasActivity())
                            .filter(aggregate -> isLiability(aggregate.account()))
                            .map(this::toReportItem)
                            .toList();
                    List<AccountingLegacyDtos.ReportItemDto> capitauxPropres = snapshot.accounts().values().stream()
                            .map(account -> toAccountAggregate(account, snapshot.periodLines().getOrDefault(account.id(), List.of())))
                            .filter(aggregate -> aggregate.hasActivity())
                            .filter(aggregate -> isEquity(aggregate.account()))
                            .map(this::toReportItem)
                            .toList();
                    return new AccountingLegacyDtos.BilanDto(actifs, passifs, capitauxPropres);
                });
    }

    public Mono<AccountingLegacyDtos.CompteResultatDto> generateCompteResultat(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        validateDateRange(startDate, endDate);
        return loadSnapshot(context, startDate, endDate)
                .map(snapshot -> {
                    List<AccountingLegacyDtos.ReportItemDto> produits = snapshot.accounts().values().stream()
                            .map(account -> toAccountAggregate(account, snapshot.periodLines().getOrDefault(account.id(), List.of())))
                            .filter(aggregate -> aggregate.hasActivity())
                            .filter(aggregate -> isRevenue(aggregate.account()))
                            .map(this::toPositiveReportItem)
                            .toList();
                    List<AccountingLegacyDtos.ReportItemDto> charges = snapshot.accounts().values().stream()
                            .map(account -> toAccountAggregate(account, snapshot.periodLines().getOrDefault(account.id(), List.of())))
                            .filter(aggregate -> aggregate.hasActivity())
                            .filter(aggregate -> isExpense(aggregate.account()))
                            .map(this::toPositiveReportItem)
                            .toList();
                    return new AccountingLegacyDtos.CompteResultatDto(produits, charges);
                });
    }

    public Mono<AccountingLegacyDtos.CashFlowDto> generateCashFlow(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        validateDateRange(startDate, endDate);
        return loadSnapshot(context, startDate, endDate)
                .flatMap(snapshot -> accountingOperationsService.listFixedAssets(context).collectList()
                        .map(fixedAssets -> {
                            BigDecimal produits = sumSolde(snapshot, this::isRevenue);
                            BigDecimal charges = sumSolde(snapshot, this::isExpense);
                            BigDecimal investmentOutflow = fixedAssets.stream()
                                    .map(AccountingOperationsViews.FixedAssetView::acquisitionCost)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .negate();
                            BigDecimal financing = sumSolde(snapshot, this::isEquity);
                            List<AccountingLegacyDtos.CashFlowItemDto> operationnel = List.of(
                                    new AccountingLegacyDtos.CashFlowItemDto("OP01", "Produits", produits, "operationnel"),
                                    new AccountingLegacyDtos.CashFlowItemDto("OP02", "Charges", charges.negate(), "operationnel"),
                                    new AccountingLegacyDtos.CashFlowItemDto("OP03", "Flux opérationnel net",
                                            produits.subtract(charges), "operationnel"));
                            List<AccountingLegacyDtos.CashFlowItemDto> investissement = List.of(
                                    new AccountingLegacyDtos.CashFlowItemDto("INV01",
                                            "Immobilisations et investissements",
                                            investmentOutflow,
                                            "investissement"));
                            List<AccountingLegacyDtos.CashFlowItemDto> financement = List.of(
                                    new AccountingLegacyDtos.CashFlowItemDto("FIN01",
                                            "Capitaux et financement",
                                            financing,
                                            "financement"));
                            return new AccountingLegacyDtos.CashFlowDto(operationnel, investissement, financement);
                        }));
    }

    public Mono<AccountingLegacyDtos.ExecutiveSummaryDto> generateExecutiveSummary(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        return Mono.zip(
                generateBilan(startDate, endDate, context),
                generateCompteResultat(startDate, endDate, context),
                generateCashFlow(startDate, endDate, context))
                .map(tuple -> {
                    AccountingLegacyDtos.BilanDto bilan = tuple.getT1();
                    AccountingLegacyDtos.CompteResultatDto resultat = tuple.getT2();
                    AccountingLegacyDtos.CashFlowDto cashFlow = tuple.getT3();
                    List<AccountingLegacyDtos.ExecutiveSummaryItemDto> bilanSummary = List.of(
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto(
                                    "Actifs",
                                    sumReportItems(bilan.actifs()),
                                    "Total des actifs"),
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto(
                                    "Passifs",
                                    sumReportItems(bilan.passifs()),
                                    "Total des passifs"),
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto(
                                    "Capitaux Propres",
                                    sumReportItems(bilan.capitauxPropres()),
                                    "Total des capitaux propres"));
                    BigDecimal totalProduits = sumReportItems(resultat.produits());
                    BigDecimal totalCharges = sumReportItems(resultat.charges());
                    List<AccountingLegacyDtos.ExecutiveSummaryItemDto> resultatSummary = List.of(
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto("Chiffre d'Affaires", totalProduits,
                                    "Total des produits"),
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto("Dépenses", totalCharges,
                                    "Total des charges"),
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto("Marge Nette",
                                    totalProduits.subtract(totalCharges),
                                    "Bénéfice ou perte"));
                    BigDecimal totalCashFlowOp = cashFlow.operationnel().stream()
                            .map(AccountingLegacyDtos.CashFlowItemDto::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalCashFlowInv = cashFlow.investissement().stream()
                            .map(AccountingLegacyDtos.CashFlowItemDto::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalCashFlowFin = cashFlow.financement().stream()
                            .map(AccountingLegacyDtos.CashFlowItemDto::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    List<AccountingLegacyDtos.ExecutiveSummaryItemDto> cashFlowSummary = List.of(
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto("Flux Opérationnel", totalCashFlowOp,
                                    "Généré par les opérations"),
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto("Flux d'Investissement", totalCashFlowInv,
                                    "Lié aux investissements"),
                            new AccountingLegacyDtos.ExecutiveSummaryItemDto("Trésorerie Nette",
                                    totalCashFlowOp.add(totalCashFlowInv).add(totalCashFlowFin),
                                    "Evolution de la trésorerie"));
                    return new AccountingLegacyDtos.ExecutiveSummaryDto(bilanSummary, resultatSummary, cashFlowSummary);
                });
    }

    public Mono<List<AccountingLegacyDtos.GrandLivreDto>> generateGrandLivre(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        validateDateRange(startDate, endDate);
        return loadSnapshot(context, startDate, endDate)
                .map(snapshot -> snapshot.accounts().values().stream()
                        .map(account -> {
                            List<EntryLineOccurrence> openingLines = snapshot.openingLines().getOrDefault(account.id(), List.of());
                            List<EntryLineOccurrence> periodLines = snapshot.periodLines().getOrDefault(account.id(), List.of());
                            BigDecimal openingBalance = balanceFor(account, openingLines);
                            BigDecimal totalDebit = sumDebit(periodLines);
                            BigDecimal totalCredit = sumCredit(periodLines);
                            BigDecimal closingBalance = openingBalance.add(balanceFor(account, periodLines));
                            List<AccountingLegacyDtos.LigneGrandLivreDto> lines = periodLines.stream()
                                    .sorted(Comparator.comparing(EntryLineOccurrence::entryDate))
                                    .map(line -> new AccountingLegacyDtos.LigneGrandLivreDto(
                                            line.entryId(),
                                            toLocalDateTime(line.entryDate()),
                                            journalCode(snapshot.journals(), line.journalId()),
                                            line.reference(),
                                            line.label(),
                                            line.debit(),
                                            line.credit()))
                                    .toList();
                            return new AccountingLegacyDtos.GrandLivreDto(
                                    account.accountNumber(),
                                    account.label(),
                                    openingBalance,
                                    totalDebit,
                                    totalCredit,
                                    closingBalance,
                                    lines);
                        })
                        .filter(dto -> dto.soldeOuverture().compareTo(BigDecimal.ZERO) != 0
                                || dto.totalDebit().compareTo(BigDecimal.ZERO) != 0
                                || dto.totalCredit().compareTo(BigDecimal.ZERO) != 0)
                        .toList());
    }

    public Mono<AccountingLegacyDtos.BalanceDesComptesDto> generateBalanceDesComptes(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        validateDateRange(startDate, endDate);
        return loadSnapshot(context, startDate, endDate)
                .map(snapshot -> {
                    List<AccountingLegacyDtos.LigneBalanceDto> lignes = snapshot.accounts().values().stream()
                            .map(account -> {
                                List<EntryLineOccurrence> openingLines = snapshot.openingLines().getOrDefault(account.id(), List.of());
                                List<EntryLineOccurrence> periodLines = snapshot.periodLines().getOrDefault(account.id(), List.of());
                                BigDecimal openingBalance = balanceFor(account, openingLines);
                                BigDecimal openingDebit = openingBalance.signum() >= 0 ? openingBalance : BigDecimal.ZERO;
                                BigDecimal openingCredit = openingBalance.signum() < 0 ? openingBalance.abs() : BigDecimal.ZERO;
                                BigDecimal movementDebit = sumDebit(periodLines);
                                BigDecimal movementCredit = sumCredit(periodLines);
                                BigDecimal closingBalance = openingBalance.add(balanceFor(account, periodLines));
                                BigDecimal closingDebit = closingBalance.signum() >= 0 ? closingBalance : BigDecimal.ZERO;
                                BigDecimal closingCredit = closingBalance.signum() < 0 ? closingBalance.abs() : BigDecimal.ZERO;
                                return new AccountingLegacyDtos.LigneBalanceDto(
                                        account.accountNumber(),
                                        account.label(),
                                        openingDebit,
                                        openingCredit,
                                        movementDebit,
                                        movementCredit,
                                        closingDebit,
                                        closingCredit);
                            })
                            .filter(line -> line.soldeOuvertureDebit().compareTo(BigDecimal.ZERO) != 0
                                    || line.soldeOuvertureCredit().compareTo(BigDecimal.ZERO) != 0
                                    || line.mouvementDebit().compareTo(BigDecimal.ZERO) != 0
                                    || line.mouvementCredit().compareTo(BigDecimal.ZERO) != 0
                                    || line.soldeClotureDebit().compareTo(BigDecimal.ZERO) != 0
                                    || line.soldeClotureCredit().compareTo(BigDecimal.ZERO) != 0)
                            .toList();
                    return new AccountingLegacyDtos.BalanceDesComptesDto(
                            lignes.stream().map(AccountingLegacyDtos.LigneBalanceDto::soldeOuvertureDebit).reduce(BigDecimal.ZERO, BigDecimal::add),
                            lignes.stream().map(AccountingLegacyDtos.LigneBalanceDto::soldeOuvertureCredit).reduce(BigDecimal.ZERO, BigDecimal::add),
                            lignes.stream().map(AccountingLegacyDtos.LigneBalanceDto::mouvementDebit).reduce(BigDecimal.ZERO, BigDecimal::add),
                            lignes.stream().map(AccountingLegacyDtos.LigneBalanceDto::mouvementCredit).reduce(BigDecimal.ZERO, BigDecimal::add),
                            lignes.stream().map(AccountingLegacyDtos.LigneBalanceDto::soldeClotureDebit).reduce(BigDecimal.ZERO, BigDecimal::add),
                            lignes.stream().map(AccountingLegacyDtos.LigneBalanceDto::soldeClotureCredit).reduce(BigDecimal.ZERO, BigDecimal::add),
                            lignes);
                });
    }

    private Mono<ReportingSnapshot> loadSnapshot(AccountingExtensionRequestContext context,
            LocalDate startDate,
            LocalDate endDate) {
        return Mono.zip(
                accountingBookkeepingService.listAccounts(context).collectList(),
                accountingBookkeepingService.listJournals(context).collectList(),
                accountingBookkeepingService.listEntries(context).collectList())
                .map(tuple -> {
                    Map<UUID, AccountingBookkeepingViews.AccountView> accounts = tuple.getT1().stream()
                            .collect(java.util.stream.Collectors.toMap(AccountingBookkeepingViews.AccountView::id, account -> account,
                                    (left, right) -> left, LinkedHashMap::new));
                    Map<UUID, AccountingBookkeepingViews.JournalView> journals = tuple.getT2().stream()
                            .collect(java.util.stream.Collectors.toMap(AccountingBookkeepingViews.JournalView::id, journal -> journal,
                                    (left, right) -> left, LinkedHashMap::new));
                    List<AccountingBookkeepingViews.AccountingEntryView> entries = tuple.getT3().stream()
                            .filter(entry -> "VALIDATED".equalsIgnoreCase(entry.status()))
                            .toList();
                    Map<UUID, List<EntryLineOccurrence>> openingLines = new LinkedHashMap<>();
                    Map<UUID, List<EntryLineOccurrence>> periodLines = new LinkedHashMap<>();
                    for (AccountingBookkeepingViews.AccountingEntryView entry : entries) {
                        for (AccountingBookkeepingViews.EntryLineView line : entry.lines()) {
                            EntryLineOccurrence occurrence = new EntryLineOccurrence(
                                    entry.id(),
                                    entry.journalId(),
                                    entry.reference(),
                                    entry.entryDate(),
                                    line.accountId(),
                                    line.label(),
                                    line.debit(),
                                    line.credit());
                            openingLines.computeIfAbsent(line.accountId(), ignored -> new java.util.ArrayList<>());
                            periodLines.computeIfAbsent(line.accountId(), ignored -> new java.util.ArrayList<>());
                            LocalDate entryDate = toLocalDate(entry.entryDate());
                            if (entryDate.isBefore(startDate)) {
                                openingLines.get(line.accountId()).add(occurrence);
                            }
                            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)) {
                                periodLines.get(line.accountId()).add(occurrence);
                            }
                        }
                    }
                    return new ReportingSnapshot(accounts, journals, openingLines, periodLines);
                });
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must precede end date");
        }
    }

    private AccountAggregate toAccountAggregate(AccountingBookkeepingViews.AccountView account, List<EntryLineOccurrence> lines) {
        BigDecimal totalDebit = sumDebit(lines);
        BigDecimal totalCredit = sumCredit(lines);
        return new AccountAggregate(account, totalDebit, totalCredit, balanceFor(account, lines));
    }

    private AccountingLegacyDtos.ReportItemDto toReportItem(AccountAggregate aggregate) {
        return new AccountingLegacyDtos.ReportItemDto(
                aggregate.account().accountNumber(),
                aggregate.account().label(),
                aggregate.totalDebit(),
                aggregate.totalCredit(),
                aggregate.solde());
    }

    private AccountingLegacyDtos.ReportItemDto toPositiveReportItem(AccountAggregate aggregate) {
        return new AccountingLegacyDtos.ReportItemDto(
                aggregate.account().accountNumber(),
                aggregate.account().label(),
                aggregate.totalDebit(),
                aggregate.totalCredit(),
                aggregate.solde().abs());
    }

    private BigDecimal sumSolde(ReportingSnapshot snapshot,
            java.util.function.Predicate<AccountingBookkeepingViews.AccountView> filter) {
        return snapshot.accounts().values().stream()
                .filter(filter)
                .map(account -> toAccountAggregate(account, snapshot.periodLines().getOrDefault(account.id(), List.of())).solde())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumReportItems(List<AccountingLegacyDtos.ReportItemDto> items) {
        return items.stream().map(AccountingLegacyDtos.ReportItemDto::solde).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDebit(List<EntryLineOccurrence> lines) {
        return lines.stream().map(EntryLineOccurrence::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCredit(List<EntryLineOccurrence> lines) {
        return lines.stream().map(EntryLineOccurrence::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal balanceFor(AccountingBookkeepingViews.AccountView account, List<EntryLineOccurrence> lines) {
        return lines.stream()
                .map(line -> debitNormal(account) ? line.debit().subtract(line.credit()) : line.credit().subtract(line.debit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean debitNormal(AccountingBookkeepingViews.AccountView account) {
        String type = account.accountType() == null ? "" : account.accountType().toUpperCase(java.util.Locale.ROOT);
        if (type.contains("CLIENT") || type.contains("CUSTOMER") || type.contains("ACTIF") || type.contains("ASSET")
                || type.contains("CHARGE") || type.contains("EXPENSE") || type.contains("BANK")
                || type.contains("CASH") || type.contains("INVENTORY") || type.contains("RECEIVABLE")) {
            return true;
        }
        if (type.contains("SUPPLIER") || type.contains("FOURNISSEUR") || type.contains("PASSIF")
                || type.contains("LIABILITY") || type.contains("REVENUE") || type.contains("PRODUCT")
                || type.contains("INCOME") || type.contains("CAPITAL") || type.contains("EQUITY")) {
            return false;
        }
        String accountNumber = account.accountNumber();
        if (accountNumber == null || accountNumber.isBlank()) {
            return true;
        }
        if (accountNumber.startsWith("40")) {
            return false;
        }
        if (accountNumber.startsWith("41")) {
            return true;
        }
        return switch (accountNumber.charAt(0)) {
            case '2', '3', '5', '6' -> true;
            case '1', '7' -> false;
            default -> true;
        };
    }

    private boolean isEquity(AccountingBookkeepingViews.AccountView account) {
        return startsWithClass(account, '1') || account.accountNumber().startsWith("10");
    }

    private boolean isAsset(AccountingBookkeepingViews.AccountView account) {
        if (isEquity(account)) {
            return false;
        }
        if (startsWithClass(account, '2') || startsWithClass(account, '3') || startsWithClass(account, '5')) {
            return true;
        }
        return startsWithClass(account, '4') && debitNormal(account);
    }

    private boolean isLiability(AccountingBookkeepingViews.AccountView account) {
        if (isEquity(account)) {
            return false;
        }
        return startsWithClass(account, '4') && !debitNormal(account);
    }

    private boolean isRevenue(AccountingBookkeepingViews.AccountView account) {
        return startsWithClass(account, '7');
    }

    private boolean isExpense(AccountingBookkeepingViews.AccountView account) {
        return startsWithClass(account, '6');
    }

    private boolean startsWithClass(AccountingBookkeepingViews.AccountView account, char accountClass) {
        return account.accountNumber() != null
                && !account.accountNumber().isBlank()
                && account.accountNumber().charAt(0) == accountClass;
    }

    private String journalCode(Map<UUID, AccountingBookkeepingViews.JournalView> journals, UUID journalId) {
        AccountingBookkeepingViews.JournalView journal = journals.get(journalId);
        return journal == null ? null : journal.code();
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private record AccountAggregate(
            AccountingBookkeepingViews.AccountView account,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            BigDecimal solde) {
        boolean hasActivity() {
            return totalDebit.compareTo(BigDecimal.ZERO) != 0
                    || totalCredit.compareTo(BigDecimal.ZERO) != 0
                    || solde.compareTo(BigDecimal.ZERO) != 0;
        }
    }

    private record EntryLineOccurrence(
            UUID entryId,
            UUID journalId,
            String reference,
            Instant entryDate,
            UUID accountId,
            String label,
            BigDecimal debit,
            BigDecimal credit) {
    }

    private record ReportingSnapshot(
            Map<UUID, AccountingBookkeepingViews.AccountView> accounts,
            Map<UUID, AccountingBookkeepingViews.JournalView> journals,
            Map<UUID, List<EntryLineOccurrence>> openingLines,
            Map<UUID, List<EntryLineOccurrence>> periodLines) {
    }
}
