package yowyob.comops.api.accounting.extension.service;

import jakarta.annotation.PostConstruct;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionAttachmentStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionFixedAssetStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionItemStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionTaxDeclarationStore;
import yowyob.comops.api.accounting.extension.web.AccountingOperationsViews;
import yowyob.comops.api.accounting.extension.web.AccountingReferenceDataView;
import yowyob.comops.api.accounting.extension.web.AccountingRequests;
import yowyob.comops.api.accounting.extension.web.InvoiceSummaryView;
import yowyob.comops.api.accounting.extension.web.OpenItemSummaryView;
import yowyob.comops.api.accounting.extension.web.ThirdPartySummaryView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.r2dbc.BadSqlGrammarException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountingOperationsService {
    private static final String SCOPE = "OPERATIONS";

    private final AccountingClosingWorkflowService accountingClosingWorkflowService;
    private final AccountingKernelFacade accountingKernelFacade;
    private final AccountingExtensionItemStore itemStore;
    private final AccountingExtensionFixedAssetStore fixedAssetStore;
    private final AccountingExtensionTaxDeclarationStore taxDeclarationStore;
    private final AccountingExtensionAttachmentStore attachmentStore;
    private volatile boolean restoringState;

    private final Map<UUID, ClosingRun> closingRuns;
    private final Map<UUID, ReportExport> reportExports;
    private final Map<UUID, FiscalYear> fiscalYears;
    private final Map<UUID, AccountingPeriod> periods;
    private final Map<UUID, FixedAsset> fixedAssets;
    private final Map<UUID, TaxDeclaration> taxDeclarations;
    private final Map<UUID, Attachment> attachments;
    private final Map<UUID, NotificationMessage> notifications;
    private final Map<UUID, SynchronizationJob> synchronizationJobs;

    public AccountingOperationsService(AccountingClosingWorkflowService accountingClosingWorkflowService,
            AccountingKernelFacade accountingKernelFacade,
            AccountingExtensionItemStore itemStore,
            AccountingExtensionFixedAssetStore fixedAssetStore,
            AccountingExtensionTaxDeclarationStore taxDeclarationStore,
            AccountingExtensionAttachmentStore attachmentStore) {
        this.accountingClosingWorkflowService = accountingClosingWorkflowService;
        this.accountingKernelFacade = accountingKernelFacade;
        this.itemStore = itemStore;
        this.fixedAssetStore = fixedAssetStore;
        this.taxDeclarationStore = taxDeclarationStore;
        this.attachmentStore = attachmentStore;
        this.closingRuns = newPersistingMap("CLOSING_RUN", ClosingRun.class, ClosingRun::id, ClosingRun::organizationId);
        this.reportExports = newPersistingMap("REPORT_EXPORT", ReportExport.class, ReportExport::id, ReportExport::organizationId);
        this.fiscalYears = newPersistingMap("FISCAL_YEAR", FiscalYear.class, FiscalYear::id, FiscalYear::organizationId);
        this.periods = newPersistingMap("ACCOUNTING_PERIOD", AccountingPeriod.class, AccountingPeriod::id, AccountingPeriod::organizationId);
        this.fixedAssets = newFixedAssetPersistingMap();
        this.taxDeclarations = newTaxDeclarationPersistingMap();
        this.attachments = newAttachmentPersistingMap();
        this.notifications = newPersistingMap("NOTIFICATION", NotificationMessage.class, NotificationMessage::id, NotificationMessage::organizationId);
        this.synchronizationJobs = newPersistingMap("SYNCHRONIZATION_JOB", SynchronizationJob.class, SynchronizationJob::id, SynchronizationJob::organizationId);
    }

    @PostConstruct
    void loadState() {
        restoringState = true;
        try {
            restoreFromStore(closingRuns, "CLOSING_RUN", ClosingRun.class, ClosingRun::id);
            restoreFromStore(reportExports, "REPORT_EXPORT", ReportExport.class, ReportExport::id);
            restoreFromStore(fiscalYears, "FISCAL_YEAR", FiscalYear.class, FiscalYear::id);
            restoreFromStore(periods, "ACCOUNTING_PERIOD", AccountingPeriod.class, AccountingPeriod::id);
            restoreFixedAssets();
            restoreTaxDeclarations();
            restoreAttachments();
            restoreFromStore(notifications, "NOTIFICATION", NotificationMessage.class, NotificationMessage::id);
            restoreFromStore(synchronizationJobs, "SYNCHRONIZATION_JOB", SynchronizationJob.class, SynchronizationJob::id);
        } finally {
            restoringState = false;
        }
    }

    public Mono<AccountingOperationsViews.ClosingRunView> startClosingRun(AccountingRequests.StartClosingRunRequest request,
            AccountingExtensionRequestContext context) {
        return accountingClosingWorkflowService.preview(context)
                .flatMap(preview -> {
                    if (!preview.ready()) {
                        return Mono.error(new IllegalStateException("closing run cannot start: " + preview.blockingIssues()));
                    }
                    ClosingRun run = new ClosingRun(
                            UUID.randomUUID(),
                            context.requireOrganizationId(),
                            request.periodLabel().trim(),
                            "STARTED",
                            preview.totalReceivables(),
                            preview.totalPayables(),
                            preview.blockingIssues(),
                            Instant.now(),
                            null);
                    closingRuns.put(run.id(), run);
                    createSystemNotification(context.requireOrganizationId(), "CLOSING_RUN",
                            "Closing run started for " + run.periodLabel());
                    return Mono.just(toView(run));
                });
    }

    public Mono<AccountingOperationsViews.ClosingRunView> startMonthlyClosing(UUID periodId, AccountingExtensionRequestContext context) {
        AccountingPeriod period = requireOwnedPeriod(periodId, context.requireOrganizationId());
        return startClosingRun(new AccountingRequests.StartClosingRunRequest(period.code()), context);
    }

    public Flux<AccountingOperationsViews.ClosingRunView> listClosingRuns(AccountingExtensionRequestContext context) {
        return Flux.fromStream(closingRuns.values().stream()
                .filter(run -> run.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(ClosingRun::startedAt).reversed()))
                .map(this::toView);
    }

    public Mono<Map<String, Object>> closingStatus(UUID periodId, AccountingExtensionRequestContext context) {
        AccountingPeriod period = requireOwnedPeriod(periodId, context.requireOrganizationId());
        return listClosingRuns(context)
                .filter(run -> run.periodLabel().equalsIgnoreCase(period.code()))
                .next()
                .map(run -> {
                    Map<String, Object> payload = new java.util.LinkedHashMap<>();
                    payload.put("periodId", periodId);
                    payload.put("periodCode", period.code());
                    payload.put("status", run.status());
                    payload.put("startedAt", run.startedAt());
                    payload.put("completedAt", run.completedAt());
                    return payload;
                })
                .switchIfEmpty(Mono.just(Map.of(
                        "periodId", periodId,
                        "periodCode", period.code(),
                        "status", "NOT_STARTED")));
    }

    public Mono<AccountingOperationsViews.ClosingRunView> completeClosingRun(UUID runId, AccountingExtensionRequestContext context) {
        ClosingRun run = requireOwnedRun(runId, context.requireOrganizationId());
        ClosingRun completed = run.complete();
        closingRuns.put(runId, completed);
        createSystemNotification(context.requireOrganizationId(), "CLOSING_RUN",
                "Closing run completed for " + run.periodLabel());
        return Mono.just(toView(completed));
    }

    public Mono<AccountingOperationsViews.ClosingRunView> cancelClosing(UUID periodId, AccountingExtensionRequestContext context) {
        AccountingPeriod period = requireOwnedPeriod(periodId, context.requireOrganizationId());
        return Mono.fromSupplier(() -> closingRuns.values().stream()
                        .filter(run -> run.organizationId().equals(context.requireOrganizationId()))
                        .filter(run -> run.periodLabel().equalsIgnoreCase(period.code()))
                        .sorted(Comparator.comparing(ClosingRun::startedAt).reversed())
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("closing run not found for period")))
                .map(ClosingRun::cancel)
                .doOnNext(run -> closingRuns.put(run.id(), run))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.ReportExportView> generateReport(AccountingRequests.GenerateReportExportRequest request,
            AccountingExtensionRequestContext context) {
        return accountingKernelFacade.loadReportingReferenceData(context)
                .map(referenceData -> {
                    ReportExport export = new ReportExport(
                            UUID.randomUUID(),
                            context.requireOrganizationId(),
                            request.reportType().trim().toUpperCase(java.util.Locale.ROOT),
                            request.format().trim().toUpperCase(java.util.Locale.ROOT),
                            "GENERATED",
                            Instant.now(),
                            render(referenceData, request.reportType()));
                    reportExports.put(export.id(), export);
                    createSystemNotification(context.requireOrganizationId(), "REPORT_EXPORT",
                            "Report generated: " + export.reportType());
                    return toView(export);
                });
    }

    public Flux<AccountingOperationsViews.ReportExportView> listReportExports(AccountingExtensionRequestContext context) {
        return Flux.fromStream(reportExports.values().stream()
                .filter(export -> export.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(ReportExport::generatedAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.ReportExportView> generateNamedReport(String reportType,
            String format,
            AccountingExtensionRequestContext context) {
        return generateReport(new AccountingRequests.GenerateReportExportRequest(reportType, format), context);
    }

    public Mono<AccountingOperationsViews.FiscalYearView> createFiscalYear(AccountingRequests.CreateFiscalYearRequest request,
            AccountingExtensionRequestContext context) {
        validateDateRange(request.startDate(), request.endDate(), "fiscal year");
        UUID organizationId = context.requireOrganizationId();
        boolean duplicateLabel = fiscalYears.values().stream()
                .anyMatch(year -> year.organizationId().equals(organizationId)
                        && year.label().equalsIgnoreCase(request.label().trim()));
        if (duplicateLabel) {
            return Mono.error(new IllegalArgumentException("fiscal year label already exists for organization"));
        }
        FiscalYear year = new FiscalYear(UUID.randomUUID(), organizationId, request.label().trim(), request.startDate(),
                request.endDate(), "OPEN", Instant.now(), null);
        fiscalYears.put(year.id(), year);
        return Mono.just(toView(year));
    }

    public Flux<AccountingOperationsViews.FiscalYearView> listFiscalYears(AccountingExtensionRequestContext context) {
        return Flux.fromStream(fiscalYears.values().stream()
                .filter(year -> year.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(FiscalYear::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.FiscalYearView> getFiscalYear(UUID fiscalYearId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedFiscalYear(fiscalYearId, context.requireOrganizationId())));
    }

    public Mono<AccountingOperationsViews.FiscalYearView> getActiveFiscalYear(AccountingExtensionRequestContext context) {
        return listFiscalYears(context)
                .filter(year -> "OPEN".equalsIgnoreCase(year.status()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("active fiscal year not found")));
    }

    public Mono<AccountingOperationsViews.FiscalYearView> updateFiscalYear(UUID fiscalYearId,
            AccountingRequests.CreateFiscalYearRequest request,
            AccountingExtensionRequestContext context) {
        validateDateRange(request.startDate(), request.endDate(), "fiscal year");
        FiscalYear current = requireOwnedFiscalYear(fiscalYearId, context.requireOrganizationId());
        FiscalYear updated = new FiscalYear(current.id(), current.organizationId(), request.label().trim(),
                request.startDate(), request.endDate(), current.status(), current.createdAt(), current.closedAt());
        fiscalYears.put(updated.id(), updated);
        return Mono.just(toView(updated));
    }

    public Flux<AccountingOperationsViews.AccountingPeriodView> listPeriodsByFiscalYear(UUID fiscalYearId,
            AccountingExtensionRequestContext context) {
        requireOwnedFiscalYear(fiscalYearId, context.requireOrganizationId());
        return listPeriods(context)
                .filter(period -> period.fiscalYearId().equals(fiscalYearId));
    }

    public Mono<AccountingOperationsViews.FiscalYearView> closeFiscalYear(UUID fiscalYearId, AccountingExtensionRequestContext context) {
        FiscalYear fiscalYear = requireOwnedFiscalYear(fiscalYearId, context.requireOrganizationId());
        boolean allPeriodsClosed = periods.values().stream()
                .filter(period -> period.organizationId().equals(context.requireOrganizationId()))
                .filter(period -> period.fiscalYearId().equals(fiscalYearId))
                .allMatch(period -> "CLOSED".equals(period.status()));
        if (!allPeriodsClosed) {
            return Mono.error(new IllegalStateException("all accounting periods must be closed before closing fiscal year"));
        }
        FiscalYear closed = fiscalYear.close();
        fiscalYears.put(closed.id(), closed);
        createSystemNotification(context.requireOrganizationId(), "FISCAL_YEAR", "Fiscal year closed: " + closed.label());
        return Mono.just(toView(closed));
    }

    public Mono<Void> deleteFiscalYear(UUID fiscalYearId, AccountingExtensionRequestContext context) {
        requireOwnedFiscalYear(fiscalYearId, context.requireOrganizationId());
        periods.values().removeIf(period -> period.organizationId().equals(context.requireOrganizationId())
                && period.fiscalYearId().equals(fiscalYearId));
        fiscalYears.remove(fiscalYearId);
        syncType("ACCOUNTING_PERIOD", periods, AccountingPeriod::id, AccountingPeriod::organizationId);
        return Mono.empty();
    }

    public Mono<AccountingOperationsViews.AccountingPeriodView> createPeriod(AccountingRequests.CreatePeriodRequest request,
            AccountingExtensionRequestContext context) {
        validateDateRange(request.startDate(), request.endDate(), "accounting period");
        FiscalYear fiscalYear = requireOwnedFiscalYear(request.fiscalYearId(), context.requireOrganizationId());
        if ("CLOSED".equals(fiscalYear.status())) {
            return Mono.error(new IllegalStateException("cannot add period to closed fiscal year"));
        }
        if (request.startDate().isBefore(fiscalYear.startDate()) || request.endDate().isAfter(fiscalYear.endDate())) {
            return Mono.error(new IllegalArgumentException("period must fit inside fiscal year"));
        }
        AccountingPeriod period = new AccountingPeriod(UUID.randomUUID(), context.requireOrganizationId(), fiscalYear.id(),
                request.code().trim(), request.startDate(), request.endDate(), "OPEN", Instant.now(), null);
        periods.put(period.id(), period);
        return Mono.just(toView(period));
    }

    public Flux<AccountingOperationsViews.AccountingPeriodView> listPeriods(AccountingExtensionRequestContext context) {
        return Flux.fromStream(periods.values().stream()
                .filter(period -> period.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(AccountingPeriod::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.AccountingPeriodView> getPeriod(UUID periodId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedPeriod(periodId, context.requireOrganizationId())));
    }

    public Mono<AccountingOperationsViews.AccountingPeriodView> getPeriodByCode(String code, AccountingExtensionRequestContext context) {
        return listPeriods(context)
                .filter(period -> period.code().equalsIgnoreCase(code.trim()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("accounting period not found")));
    }

    public Mono<AccountingOperationsViews.AccountingPeriodView> getPeriodByDate(LocalDate date, AccountingExtensionRequestContext context) {
        return listPeriods(context)
                .filter(period -> !date.isBefore(period.startDate()) && !date.isAfter(period.endDate()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("accounting period not found")));
    }

    public Flux<AccountingOperationsViews.AccountingPeriodView> listNonClosedPeriods(AccountingExtensionRequestContext context) {
        return listPeriods(context)
                .filter(period -> !"CLOSED".equalsIgnoreCase(period.status()));
    }

    public Flux<AccountingOperationsViews.AccountingPeriodView> listPeriodsByRange(LocalDate startDate,
            LocalDate endDate,
            AccountingExtensionRequestContext context) {
        return listPeriods(context)
                .filter(period -> !period.endDate().isBefore(startDate) && !period.startDate().isAfter(endDate));
    }

    public Mono<AccountingOperationsViews.AccountingPeriodView> updatePeriod(UUID periodId,
            AccountingRequests.CreatePeriodRequest request,
            AccountingExtensionRequestContext context) {
        validateDateRange(request.startDate(), request.endDate(), "accounting period");
        requireOwnedFiscalYear(request.fiscalYearId(), context.requireOrganizationId());
        AccountingPeriod current = requireOwnedPeriod(periodId, context.requireOrganizationId());
        AccountingPeriod updated = new AccountingPeriod(current.id(), current.organizationId(), request.fiscalYearId(),
                request.code().trim(), request.startDate(), request.endDate(), current.status(), current.createdAt(),
                current.closedAt());
        periods.put(updated.id(), updated);
        return Mono.just(toView(updated));
    }

    public Mono<AccountingOperationsViews.AccountingPeriodView> closePeriod(UUID periodId, AccountingExtensionRequestContext context) {
        AccountingPeriod period = requireOwnedPeriod(periodId, context.requireOrganizationId());
        AccountingPeriod closed = period.close();
        periods.put(closed.id(), closed);
        createSystemNotification(context.requireOrganizationId(), "ACCOUNTING_PERIOD",
                "Accounting period closed: " + closed.code());
        return Mono.just(toView(closed));
    }

    public Mono<Void> deletePeriod(UUID periodId, AccountingExtensionRequestContext context) {
        requireOwnedPeriod(periodId, context.requireOrganizationId());
        periods.remove(periodId);
        return Mono.empty();
    }

    public Mono<AccountingOperationsViews.FixedAssetView> createFixedAsset(AccountingRequests.CreateFixedAssetRequest request,
            AccountingExtensionRequestContext context) {
        if (request.usefulLifeMonths() == null || request.usefulLifeMonths() <= 0) {
            return Mono.error(new IllegalArgumentException("usefulLifeMonths must be greater than zero"));
        }
        FixedAsset asset = new FixedAsset(UUID.randomUUID(), context.requireOrganizationId(), request.reference().trim(),
                request.label().trim(), request.acquisitionCost(), request.usefulLifeMonths(), BigDecimal.ZERO, "ACTIVE",
                request.acquiredAt() == null ? Instant.now() : request.acquiredAt(), null);
        fixedAssets.put(asset.id(), asset);
        return Mono.just(toView(asset));
    }

    public Flux<AccountingOperationsViews.FixedAssetView> listFixedAssets(AccountingExtensionRequestContext context) {
        return Flux.fromStream(fixedAssets.values().stream()
                .filter(asset -> asset.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(FixedAsset::acquiredAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.FixedAssetView> getFixedAsset(UUID fixedAssetId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedFixedAsset(fixedAssetId, context.requireOrganizationId())));
    }

    public Mono<AccountingOperationsViews.FixedAssetView> depreciateFixedAsset(UUID fixedAssetId,
            AccountingRequests.DepreciateFixedAssetRequest request,
            AccountingExtensionRequestContext context) {
        if (request.months() == null || request.months() <= 0) {
            return Mono.error(new IllegalArgumentException("months must be greater than zero"));
        }
        FixedAsset asset = requireOwnedFixedAsset(fixedAssetId, context.requireOrganizationId());
        FixedAsset depreciated = asset.depreciate(request.months());
        fixedAssets.put(depreciated.id(), depreciated);
        return Mono.just(toView(depreciated));
    }

    public Mono<Map<String, Object>> generateDepreciationSchedule(UUID fixedAssetId, AccountingExtensionRequestContext context) {
        FixedAsset asset = requireOwnedFixedAsset(fixedAssetId, context.requireOrganizationId());
        BigDecimal monthlyDepreciation = asset.acquisitionCost()
                .divide(BigDecimal.valueOf(asset.usefulLifeMonths()), 2, RoundingMode.HALF_UP);
        List<Map<String, Object>> schedule = java.util.stream.IntStream.rangeClosed(1, asset.usefulLifeMonths())
                .mapToObj(month -> Map.<String, Object>of(
                        "period", month,
                        "amount", monthlyDepreciation,
                        "remainingValue", asset.acquisitionCost()
                                .subtract(monthlyDepreciation.multiply(BigDecimal.valueOf(month)))
                                .max(BigDecimal.ZERO)))
                .toList();
        return Mono.just(Map.of(
                "fixedAssetId", fixedAssetId,
                "reference", asset.reference(),
                "schedule", schedule));
    }

    public Flux<AccountingOperationsViews.FixedAssetView> postDepreciation(AccountingExtensionRequestContext context) {
        return Flux.fromStream(fixedAssets.values().stream()
                        .filter(asset -> asset.organizationId().equals(context.requireOrganizationId()))
                        .filter(asset -> !"FULLY_DEPRECIATED".equals(asset.status()))
                        .map(asset -> asset.depreciate(1)))
                .doOnNext(asset -> fixedAssets.put(asset.id(), asset))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.TaxDeclarationView> createTaxDeclaration(
            AccountingRequests.CreateTaxDeclarationRequest request,
            AccountingExtensionRequestContext context) {
        return accountingKernelFacade.loadReportingReferenceData(context)
                .map(referenceData -> {
                    BigDecimal taxableBase = sumPostedInvoices(referenceData.invoices());
                    BigDecimal taxAmount = taxableBase.multiply(request.rate())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    TaxDeclaration declaration = new TaxDeclaration(UUID.randomUUID(), context.requireOrganizationId(),
                            request.taxType().trim().toUpperCase(java.util.Locale.ROOT), request.periodLabel().trim(),
                            taxableBase, taxAmount, "DRAFT", Instant.now(), null);
                    taxDeclarations.put(declaration.id(), declaration);
                    return toView(declaration);
                });
    }

    public Flux<AccountingOperationsViews.TaxDeclarationView> listTaxDeclarations(AccountingExtensionRequestContext context) {
        return Flux.fromStream(taxDeclarations.values().stream()
                .filter(declaration -> declaration.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(TaxDeclaration::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.TaxDeclarationView> getTaxDeclaration(UUID declarationId,
            AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedTaxDeclaration(declarationId, context.requireOrganizationId())));
    }

    public Flux<AccountingOperationsViews.TaxDeclarationView> listTaxDeclarationsByType(String type,
            AccountingExtensionRequestContext context) {
        return listTaxDeclarations(context)
                .filter(declaration -> declaration.taxType().equalsIgnoreCase(type.trim()));
    }

    public Flux<AccountingOperationsViews.TaxDeclarationView> searchTaxDeclarations(String query,
            AccountingExtensionRequestContext context) {
        String normalized = query.trim().toLowerCase(java.util.Locale.ROOT);
        return listTaxDeclarations(context)
                .filter(declaration -> declaration.taxType().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                        || declaration.periodLabel().toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }

    public Mono<Void> deleteTaxDeclaration(UUID declarationId, AccountingExtensionRequestContext context) {
        requireOwnedTaxDeclaration(declarationId, context.requireOrganizationId());
        taxDeclarations.remove(declarationId);
        return Mono.empty();
    }

    public Mono<AccountingOperationsViews.TaxDeclarationView> submitTaxDeclaration(UUID declarationId,
            AccountingExtensionRequestContext context) {
        TaxDeclaration declaration = requireOwnedTaxDeclaration(declarationId, context.requireOrganizationId());
        TaxDeclaration submitted = declaration.submit();
        taxDeclarations.put(submitted.id(), submitted);
        createSystemNotification(context.requireOrganizationId(), "TAX_DECLARATION",
                "Tax declaration submitted: " + submitted.taxType() + " " + submitted.periodLabel());
        return Mono.just(toView(submitted));
    }

    public Mono<AccountingOperationsViews.TaxDeclarationView> generateTaxDeclaration(
            AccountingRequests.CreateTaxDeclarationRequest request,
            AccountingExtensionRequestContext context) {
        return createTaxDeclaration(request, context);
    }

    public Mono<AccountingOperationsViews.AttachmentView> createAttachment(AccountingRequests.CreateAttachmentRequest request,
            AccountingExtensionRequestContext context) {
        Attachment attachment = new Attachment(UUID.randomUUID(), context.requireOrganizationId(),
                request.targetType().trim().toUpperCase(java.util.Locale.ROOT), request.targetId(), request.filename().trim(),
                request.contentType().trim(), request.sizeBytes(), syntheticAttachmentContent(request.filename(),
                        request.targetType(), request.targetId(), request.contentType(), request.sizeBytes()),
                Instant.now());
        attachments.put(attachment.id(), attachment);
        return Mono.just(toView(attachment));
    }

    public Mono<AccountingOperationsViews.AttachmentView> createAttachmentFromFile(String targetType,
            UUID targetId,
            FilePart file,
            AccountingExtensionRequestContext context) {
        return DataBufferUtils.join(file.content())
                .map(buffer -> {
                    byte[] content = new byte[buffer.readableByteCount()];
                    buffer.read(content);
                    DataBufferUtils.release(buffer);
                    Attachment attachment = new Attachment(UUID.randomUUID(), context.requireOrganizationId(),
                            targetType.trim().toUpperCase(java.util.Locale.ROOT), targetId, file.filename(),
                            file.headers().getContentType() == null
                                    ? "application/octet-stream"
                                    : file.headers().getContentType().toString(),
                            content.length, content, Instant.now());
                    attachments.put(attachment.id(), attachment);
                    return toView(attachment);
                });
    }

    public Flux<AccountingOperationsViews.AttachmentView> listAttachments(AccountingExtensionRequestContext context) {
        return Flux.fromStream(attachments.values().stream()
                .filter(attachment -> attachment.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Attachment::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingOperationsViews.AttachmentView> downloadAttachment(String filename, AccountingExtensionRequestContext context) {
        return listAttachments(context)
                .filter(attachment -> attachment.filename().equalsIgnoreCase(filename.trim()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("attachment not found")));
    }

    public Mono<byte[]> downloadAttachmentBytes(String filename, AccountingExtensionRequestContext context) {
        return Mono.fromSupplier(() -> attachments.values().stream()
                        .filter(attachment -> attachment.organizationId().equals(context.requireOrganizationId()))
                        .filter(attachment -> attachment.filename().equalsIgnoreCase(filename.trim()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("attachment not found")))
                .map(Attachment::content);
    }

    public Mono<AccountingOperationsViews.NotificationView> createNotification(
            AccountingRequests.CreateNotificationRequest request,
            AccountingExtensionRequestContext context) {
        NotificationMessage notification = new NotificationMessage(UUID.randomUUID(), context.requireOrganizationId(),
                request.category().trim().toUpperCase(java.util.Locale.ROOT), request.message().trim(), "PENDING",
                Instant.now(), null);
        notifications.put(notification.id(), notification);
        return Mono.just(toView(notification));
    }

    public Flux<AccountingOperationsViews.NotificationView> listNotifications(AccountingExtensionRequestContext context) {
        return Flux.fromStream(notifications.values().stream()
                .filter(notification -> notification.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(NotificationMessage::createdAt).reversed()))
                .map(this::toView);
    }

    public Flux<AccountingOperationsViews.NotificationView> listUnreadNotifications(AccountingExtensionRequestContext context) {
        return listNotifications(context)
                .filter(notification -> "PENDING".equalsIgnoreCase(notification.status()));
    }

    public Mono<AccountingOperationsViews.NotificationView> acknowledgeNotification(UUID notificationId,
            AccountingExtensionRequestContext context) {
        NotificationMessage notification = requireOwnedNotification(notificationId, context.requireOrganizationId());
        NotificationMessage acknowledged = notification.acknowledge();
        notifications.put(acknowledged.id(), acknowledged);
        return Mono.just(toView(acknowledged));
    }

    public Mono<AccountingOperationsViews.NotificationView> markNotificationRead(UUID notificationId,
            AccountingExtensionRequestContext context) {
        return acknowledgeNotification(notificationId, context);
    }

    public Mono<AccountingOperationsViews.SynchronizationJobView> startSynchronizationJob(
            AccountingRequests.StartSynchronizationJobRequest request,
            AccountingExtensionRequestContext context) {
        SynchronizationJob job = new SynchronizationJob(UUID.randomUUID(), context.requireOrganizationId(),
                request.domain().trim().toUpperCase(java.util.Locale.ROOT), "STARTED", Instant.now(), null,
                "Synchronization started");
        synchronizationJobs.put(job.id(), job);
        return Mono.just(toView(job));
    }

    public Flux<AccountingOperationsViews.SynchronizationJobView> listSynchronizationJobs(AccountingExtensionRequestContext context) {
        return Flux.fromStream(synchronizationJobs.values().stream()
                .filter(job -> job.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(SynchronizationJob::startedAt).reversed()))
                .map(this::toView);
    }

    public Mono<Map<String, Object>> synchronizationStatus(AccountingExtensionRequestContext context) {
        return listSynchronizationJobs(context).collectList()
                .map(jobs -> Map.of(
                        "jobsCount", jobs.size(),
                        "activeJobsCount", jobs.stream().filter(job -> "STARTED".equals(job.status())).count(),
                        "lastJob", jobs.isEmpty() ? "NONE" : jobs.getFirst().domain()));
    }

    public Mono<AccountingOperationsViews.SynchronizationJobView> completeSynchronizationJob(UUID jobId,
            AccountingRequests.CompleteSynchronizationJobRequest request,
            AccountingExtensionRequestContext context) {
        SynchronizationJob job = requireOwnedSynchronizationJob(jobId, context.requireOrganizationId());
        SynchronizationJob completed = job.complete(request.summary().trim());
        synchronizationJobs.put(completed.id(), completed);
        createSystemNotification(context.requireOrganizationId(), "SYNCHRONIZATION",
                "Synchronization completed: " + completed.domain());
        return Mono.just(toView(completed));
    }

    public Mono<AccountingOperationsViews.SynchronizationJobView> triggerSynchronization(String domain,
            AccountingExtensionRequestContext context) {
        return startSynchronizationJob(new AccountingRequests.StartSynchronizationJobRequest(domain), context);
    }

    public Mono<Map<String, Object>> clearSynchronizationDomain(String domain, AccountingExtensionRequestContext context) {
        String normalized = domain.trim().toUpperCase(java.util.Locale.ROOT);
        long removed = synchronizationJobs.values().removeIf(job -> job.organizationId().equals(context.requireOrganizationId())
                && job.domain().equalsIgnoreCase(normalized)) ? 1L : 0L;
        syncType("SYNCHRONIZATION_JOB", synchronizationJobs, SynchronizationJob::id, SynchronizationJob::organizationId);
        createSystemNotification(context.requireOrganizationId(), "SYNCHRONIZATION",
                "Synchronization cache cleared for " + normalized);
        return Mono.just(Map.of(
                "domain", normalized,
                "cleared", true,
                "removedJobs", removed));
    }

    public Mono<AccountingOperationsViews.AccountingDashboardView> dashboard(AccountingExtensionRequestContext context) {
        UUID organizationId = context.requireOrganizationId();
        return accountingClosingWorkflowService.preview(context)
                .map(preview -> new AccountingOperationsViews.AccountingDashboardView(
                        organizationId,
                        preview.ready(),
                        (int) fiscalYears.values().stream().filter(year -> year.organizationId().equals(organizationId)).count(),
                        periods.values().stream()
                                .filter(period -> period.organizationId().equals(organizationId))
                                .filter(period -> "OPEN".equals(period.status()))
                                .count(),
                        taxDeclarations.values().stream()
                                .filter(declaration -> declaration.organizationId().equals(organizationId))
                                .filter(declaration -> "DRAFT".equals(declaration.status()))
                                .count(),
                        (int) fixedAssets.values().stream().filter(asset -> asset.organizationId().equals(organizationId)).count(),
                        (int) attachments.values().stream()
                                .filter(attachment -> attachment.organizationId().equals(organizationId))
                                .count(),
                        (int) notifications.values().stream()
                                .filter(notification -> notification.organizationId().equals(organizationId))
                                .filter(notification -> "PENDING".equals(notification.status()))
                                .count(),
                        (int) synchronizationJobs.values().stream()
                                .filter(job -> job.organizationId().equals(organizationId))
                                .filter(job -> "STARTED".equals(job.status()))
                                .count()));
    }

    private void createSystemNotification(UUID organizationId, String category, String message) {
        NotificationMessage notification = new NotificationMessage(UUID.randomUUID(), organizationId, category, message,
                "PENDING", Instant.now(), null);
        notifications.put(notification.id(), notification);
    }

    private String render(AccountingReferenceDataView referenceData, String requestedType) {
        String reportType = requestedType.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (reportType) {
            case "EXECUTIVE_SUMMARY" -> executiveSummary(referenceData);
            case "AGED_RECEIVABLES" -> agedItems("AGED_RECEIVABLES", referenceData, referenceData.openReceivables());
            case "AGED_PAYABLES" -> agedItems("AGED_PAYABLES", referenceData, referenceData.openPayables());
            case "INVOICE_REGISTER" -> invoiceRegister(referenceData);
            case "BALANCE_SHEET" -> balanceSheet(referenceData);
            case "INCOME_STATEMENT" -> incomeStatement(referenceData);
            case "CASH_FLOW" -> cashFlow(referenceData);
            case "GENERAL_LEDGER" -> generalLedger(referenceData);
            case "TRIAL_BALANCE" -> trialBalance(referenceData);
            default -> throw new IllegalArgumentException("unsupported report type " + reportType);
        };
    }

    private String executiveSummary(AccountingReferenceDataView referenceData) {
        BigDecimal receivables = sum(referenceData.openReceivables());
        BigDecimal payables = sum(referenceData.openPayables());
        return """
                reportType=EXECUTIVE_SUMMARY
                organization=%s
                invoices=%d
                journals=%d
                totalReceivables=%s
                totalPayables=%s
                """.formatted(
                referenceData.currentOrganization().shortName(),
                referenceData.invoices().size(),
                referenceData.journals().size(),
                receivables,
                payables);
    }

    private String agedItems(String reportType, AccountingReferenceDataView referenceData, List<OpenItemSummaryView> items) {
        String lines = items.stream()
                .map(item -> {
                    ThirdPartySummaryView thirdParty = referenceData.findThirdParty(item.counterpartyThirdPartyId());
                    return "%s|%s|%s|%s|%s|%s".formatted(
                            item.reference(),
                            item.counterpartyThirdPartyId(),
                            thirdPartyAccount(thirdParty),
                            thirdPartyLabel(thirdParty),
                            item.balanceDue(),
                            item.currency());
                })
                .collect(java.util.stream.Collectors.joining("\n"));
        return "reportType=" + reportType + "\n" + lines;
    }

    private String invoiceRegister(AccountingReferenceDataView referenceData) {
        String lines = referenceData.invoices().stream()
                .map(invoice -> {
                    ThirdPartySummaryView customer = referenceData.findThirdParty(invoice.customerThirdPartyId());
                    return "%s|%s|%s|%s|%s|%s|%s".formatted(
                            invoice.invoiceNumber(),
                            invoice.customerThirdPartyId(),
                            thirdPartyAccount(customer),
                            thirdPartyLabel(customer),
                            invoice.totalAmount(),
                            invoice.status(),
                            invoice.paymentStatus());
                })
                .collect(java.util.stream.Collectors.joining("\n"));
        return "reportType=INVOICE_REGISTER\n" + lines;
    }

    private String thirdPartyAccount(ThirdPartySummaryView thirdParty) {
        if (thirdParty == null) {
            return "UNRESOLVED";
        }
        if (thirdParty.accountingAccount() != null && !thirdParty.accountingAccount().isBlank()) {
            return thirdParty.accountingAccount();
        }
        if (thirdParty.accountingAccountNumbers() != null && !thirdParty.accountingAccountNumbers().isEmpty()) {
            return thirdParty.accountingAccountNumbers().getFirst();
        }
        return "PENDING_ACCOUNT";
    }

    private String thirdPartyLabel(ThirdPartySummaryView thirdParty) {
        if (thirdParty == null) {
            return "UNRESOLVED";
        }
        return "%s[%s]".formatted(thirdParty.name(), thirdParty.code());
    }

    private String balanceSheet(AccountingReferenceDataView referenceData) {
        BigDecimal receivables = sum(referenceData.openReceivables());
        BigDecimal payables = sum(referenceData.openPayables());
        return """
                reportType=BALANCE_SHEET
                assets=%s
                liabilities=%s
                equity=%s
                """.formatted(receivables, payables, receivables.subtract(payables));
    }

    private String incomeStatement(AccountingReferenceDataView referenceData) {
        BigDecimal revenues = referenceData.invoices().stream()
                .filter(invoice -> "POSTED".equals(invoice.status()) || "SETTLED".equals(invoice.status()))
                .map(InvoiceSummaryView::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = sum(referenceData.openPayables());
        return """
                reportType=INCOME_STATEMENT
                revenues=%s
                expenses=%s
                netIncome=%s
                """.formatted(revenues, expenses, revenues.subtract(expenses));
    }

    private String cashFlow(AccountingReferenceDataView referenceData) {
        BigDecimal inflow = sum(referenceData.openReceivables());
        BigDecimal outflow = sum(referenceData.openPayables());
        return """
                reportType=CASH_FLOW
                inflow=%s
                outflow=%s
                netCash=%s
                """.formatted(inflow, outflow, inflow.subtract(outflow));
    }

    private String generalLedger(AccountingReferenceDataView referenceData) {
        String journals = referenceData.journals().stream()
                .map(journal -> "%s|%s|%s".formatted(journal.code(), journal.label(), journal.type()))
                .collect(java.util.stream.Collectors.joining("\n"));
        return "reportType=GENERAL_LEDGER\n" + journals;
    }

    private String trialBalance(AccountingReferenceDataView referenceData) {
        return """
                reportType=TRIAL_BALANCE
                journals=%d
                invoices=%d
                receivables=%s
                payables=%s
                """.formatted(
                referenceData.journals().size(),
                referenceData.invoices().size(),
                sum(referenceData.openReceivables()),
                sum(referenceData.openPayables()));
    }

    private BigDecimal sum(List<OpenItemSummaryView> items) {
        return items.stream()
                .map(OpenItemSummaryView::balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumPostedInvoices(List<InvoiceSummaryView> invoices) {
        return invoices.stream()
                .filter(invoice -> "POSTED".equals(invoice.status()) || "SETTLED".equals(invoice.status()))
                .map(InvoiceSummaryView::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <V> void restoreFromStore(Map<UUID, V> target,
            String itemType,
            Class<V> type,
            Function<V, UUID> idExtractor) {
        target.clear();
        restoreInto(target, itemStore.loadAll(SCOPE, itemType, type), idExtractor);
    }

    private <V> void restoreInto(Map<UUID, V> target,
            Flux<V> source,
            Function<V, UUID> idExtractor) {
        try {
            source.doOnNext(value -> target.put(idExtractor.apply(value), value))
                    .blockLast();
        } catch (BadSqlGrammarException exception) {
            target.clear();
        }
    }

    private <V> PersistingMap<V> newPersistingMap(String itemType,
            Class<V> valueType,
            Function<V, UUID> idExtractor,
            Function<V, UUID> organizationIdExtractor) {
        return new PersistingMap<>(
                itemStore,
                SCOPE,
                itemType,
                valueType,
                idExtractor,
                organizationIdExtractor,
                () -> restoringState);
    }

    private PersistingFixedAssetMap newFixedAssetPersistingMap() {
        return new PersistingFixedAssetMap(fixedAssetStore, () -> restoringState);
    }

    private PersistingTaxDeclarationMap newTaxDeclarationPersistingMap() {
        return new PersistingTaxDeclarationMap(taxDeclarationStore, () -> restoringState);
    }

    private PersistingAttachmentMap newAttachmentPersistingMap() {
        return new PersistingAttachmentMap(attachmentStore, () -> restoringState);
    }

    private void restoreFixedAssets() {
        fixedAssets.clear();
        restoreInto(fixedAssets, fixedAssetStore.loadAll(), FixedAsset::id);
    }

    private void restoreTaxDeclarations() {
        taxDeclarations.clear();
        restoreInto(taxDeclarations, taxDeclarationStore.loadAll(), TaxDeclaration::id);
    }

    private void restoreAttachments() {
        attachments.clear();
        restoreInto(attachments, attachmentStore.loadAll(), Attachment::id);
    }

    private <V> void syncType(String itemType,
            Map<UUID, V> source,
            Function<V, UUID> idExtractor,
            Function<V, UUID> organizationIdExtractor) {
        if (restoringState) {
            return;
        }
        itemStore.replaceAll(SCOPE, itemType, List.copyOf(source.values()), idExtractor, organizationIdExtractor).block();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, String label) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(label + " endDate must be on or after startDate");
        }
    }

    private ClosingRun requireOwnedRun(UUID runId, UUID organizationId) {
        ClosingRun run = closingRuns.get(runId);
        if (run == null || !run.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("closing run not found for organization");
        }
        return run;
    }

    private FiscalYear requireOwnedFiscalYear(UUID fiscalYearId, UUID organizationId) {
        FiscalYear fiscalYear = fiscalYears.get(fiscalYearId);
        if (fiscalYear == null || !fiscalYear.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("fiscal year not found for organization");
        }
        return fiscalYear;
    }

    private AccountingPeriod requireOwnedPeriod(UUID periodId, UUID organizationId) {
        AccountingPeriod period = periods.get(periodId);
        if (period == null || !period.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("accounting period not found for organization");
        }
        return period;
    }

    private FixedAsset requireOwnedFixedAsset(UUID fixedAssetId, UUID organizationId) {
        FixedAsset asset = fixedAssets.get(fixedAssetId);
        if (asset == null || !asset.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("fixed asset not found for organization");
        }
        return asset;
    }

    private TaxDeclaration requireOwnedTaxDeclaration(UUID declarationId, UUID organizationId) {
        TaxDeclaration declaration = taxDeclarations.get(declarationId);
        if (declaration == null || !declaration.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("tax declaration not found for organization");
        }
        return declaration;
    }

    private NotificationMessage requireOwnedNotification(UUID notificationId, UUID organizationId) {
        NotificationMessage notification = notifications.get(notificationId);
        if (notification == null || !notification.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("notification not found for organization");
        }
        return notification;
    }

    private SynchronizationJob requireOwnedSynchronizationJob(UUID jobId, UUID organizationId) {
        SynchronizationJob job = synchronizationJobs.get(jobId);
        if (job == null || !job.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("synchronization job not found for organization");
        }
        return job;
    }

    private AccountingOperationsViews.ClosingRunView toView(ClosingRun run) {
        return new AccountingOperationsViews.ClosingRunView(run.id(), run.organizationId(), run.periodLabel(),
                run.status(), run.totalReceivables(), run.totalPayables(), run.startedAt(), run.completedAt(),
                run.blockingIssues());
    }

    private AccountingOperationsViews.ReportExportView toView(ReportExport export) {
        return new AccountingOperationsViews.ReportExportView(export.id(), export.organizationId(), export.reportType(),
                export.format(), export.status(), export.generatedAt(), export.content());
    }

    private AccountingOperationsViews.FiscalYearView toView(FiscalYear fiscalYear) {
        return new AccountingOperationsViews.FiscalYearView(fiscalYear.id(), fiscalYear.organizationId(),
                fiscalYear.label(), fiscalYear.startDate(), fiscalYear.endDate(), fiscalYear.status(),
                fiscalYear.createdAt(), fiscalYear.closedAt());
    }

    private AccountingOperationsViews.AccountingPeriodView toView(AccountingPeriod period) {
        return new AccountingOperationsViews.AccountingPeriodView(period.id(), period.organizationId(), period.fiscalYearId(),
                period.code(), period.startDate(), period.endDate(), period.status(), period.createdAt(), period.closedAt());
    }

    private AccountingOperationsViews.FixedAssetView toView(FixedAsset asset) {
        return new AccountingOperationsViews.FixedAssetView(asset.id(), asset.organizationId(), asset.reference(),
                asset.label(), asset.acquisitionCost(), asset.usefulLifeMonths(), asset.accumulatedDepreciation(),
                asset.netBookValue(), asset.status(), asset.acquiredAt(), asset.lastDepreciatedAt());
    }

    private AccountingOperationsViews.TaxDeclarationView toView(TaxDeclaration declaration) {
        return new AccountingOperationsViews.TaxDeclarationView(declaration.id(), declaration.organizationId(),
                declaration.taxType(), declaration.periodLabel(), declaration.taxableBase(), declaration.taxAmount(),
                declaration.status(), declaration.createdAt(), declaration.submittedAt());
    }

    private AccountingOperationsViews.AttachmentView toView(Attachment attachment) {
        return new AccountingOperationsViews.AttachmentView(attachment.id(), attachment.organizationId(),
                attachment.targetType(), attachment.targetId(), attachment.filename(), attachment.contentType(),
                attachment.sizeBytes(), attachment.createdAt());
    }

    private byte[] syntheticAttachmentContent(String filename, String targetType, UUID targetId, String contentType,
            long sizeBytes) {
        return """
                filename=%s
                targetType=%s
                targetId=%s
                contentType=%s
                sizeBytes=%d
                """.formatted(filename, targetType, targetId, contentType, sizeBytes)
                .getBytes(StandardCharsets.UTF_8);
    }

    private AccountingOperationsViews.NotificationView toView(NotificationMessage notification) {
        return new AccountingOperationsViews.NotificationView(notification.id(), notification.organizationId(),
                notification.category(), notification.message(), notification.status(), notification.createdAt(),
                notification.acknowledgedAt());
    }

    private AccountingOperationsViews.SynchronizationJobView toView(SynchronizationJob job) {
        return new AccountingOperationsViews.SynchronizationJobView(job.id(), job.organizationId(), job.domain(),
                job.status(), job.startedAt(), job.completedAt(), job.summary());
    }

    record ClosingRun(UUID id, UUID organizationId, String periodLabel, String status, BigDecimal totalReceivables,
            BigDecimal totalPayables, List<String> blockingIssues, Instant startedAt, Instant completedAt) {

        ClosingRun complete() {
            return new ClosingRun(id, organizationId, periodLabel, "COMPLETED", totalReceivables, totalPayables,
                    List.copyOf(blockingIssues), startedAt, Instant.now());
        }

        ClosingRun cancel() {
            return new ClosingRun(id, organizationId, periodLabel, "CANCELLED", totalReceivables, totalPayables,
                    List.copyOf(blockingIssues), startedAt, completedAt);
        }
    }

    record ReportExport(UUID id, UUID organizationId, String reportType, String format, String status, Instant generatedAt,
            String content) {
    }

    record FiscalYear(UUID id, UUID organizationId, String label, LocalDate startDate, LocalDate endDate, String status,
            Instant createdAt, Instant closedAt) {

        FiscalYear close() {
            return new FiscalYear(id, organizationId, label, startDate, endDate, "CLOSED", createdAt, Instant.now());
        }
    }

    record AccountingPeriod(UUID id, UUID organizationId, UUID fiscalYearId, String code, LocalDate startDate,
            LocalDate endDate, String status, Instant createdAt, Instant closedAt) {

        AccountingPeriod close() {
            return new AccountingPeriod(id, organizationId, fiscalYearId, code, startDate, endDate, "CLOSED", createdAt,
                    Instant.now());
        }
    }

    public record FixedAsset(UUID id, UUID organizationId, String reference, String label, BigDecimal acquisitionCost,
            int usefulLifeMonths, BigDecimal accumulatedDepreciation, String status, Instant acquiredAt,
            Instant lastDepreciatedAt) {

        FixedAsset depreciate(int months) {
            BigDecimal monthlyDepreciation = acquisitionCost
                    .divide(BigDecimal.valueOf(usefulLifeMonths), 2, RoundingMode.HALF_UP);
            BigDecimal depreciationAmount = monthlyDepreciation.multiply(BigDecimal.valueOf(months));
            BigDecimal nextAccumulated = accumulatedDepreciation.add(depreciationAmount);
            if (nextAccumulated.compareTo(acquisitionCost) > 0) {
                nextAccumulated = acquisitionCost;
            }
            String nextStatus = nextAccumulated.compareTo(acquisitionCost) >= 0 ? "FULLY_DEPRECIATED" : status;
            return new FixedAsset(id, organizationId, reference, label, acquisitionCost, usefulLifeMonths,
                    nextAccumulated, nextStatus, acquiredAt, Instant.now());
        }

        BigDecimal netBookValue() {
            BigDecimal value = acquisitionCost.subtract(accumulatedDepreciation);
            return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
        }
    }

    public record TaxDeclaration(UUID id, UUID organizationId, String taxType, String periodLabel, BigDecimal taxableBase,
            BigDecimal taxAmount, String status, Instant createdAt, Instant submittedAt) {

        TaxDeclaration submit() {
            return new TaxDeclaration(id, organizationId, taxType, periodLabel, taxableBase, taxAmount, "SUBMITTED",
                    createdAt, Instant.now());
        }
    }

    public record Attachment(UUID id, UUID organizationId, String targetType, UUID targetId, String filename,
            String contentType, long sizeBytes, byte[] content, Instant createdAt) {
    }

    record NotificationMessage(UUID id, UUID organizationId, String category, String message, String status,
            Instant createdAt, Instant acknowledgedAt) {

        NotificationMessage acknowledge() {
            return new NotificationMessage(id, organizationId, category, message, "ACKNOWLEDGED", createdAt,
                    Instant.now());
        }
    }

    record SynchronizationJob(UUID id, UUID organizationId, String domain, String status, Instant startedAt,
            Instant completedAt, String summary) {

        SynchronizationJob complete(String nextSummary) {
            return new SynchronizationJob(id, organizationId, domain, "COMPLETED", startedAt, Instant.now(), nextSummary);
        }
    }

    static final class PersistingMap<V> extends ConcurrentHashMap<UUID, V> {
        private final AccountingExtensionItemStore itemStore;
        private final String scope;
        private final String itemType;
        private final Function<V, UUID> idExtractor;
        private final Function<V, UUID> organizationIdExtractor;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingMap(AccountingExtensionItemStore itemStore,
                String scope,
                String itemType,
                Class<V> valueType,
                Function<V, UUID> idExtractor,
                Function<V, UUID> organizationIdExtractor,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.itemStore = itemStore;
            this.scope = scope;
            this.itemType = itemType;
            this.idExtractor = idExtractor;
            this.organizationIdExtractor = organizationIdExtractor;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public V put(UUID key, V value) {
            V result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                itemStore.save(scope, itemType, idExtractor.apply(value), organizationIdExtractor.apply(value), value).block();
            }
            return result;
        }

        @Override
        public V remove(Object key) {
            V result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                itemStore.delete(scope, itemType, (UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                itemStore.delete(scope, itemType, (UUID) key).block();
            }
            return removed;
        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public void putAll(Map<? extends UUID, ? extends V> map) {
            map.forEach(this::put);
        }
    }

    static final class PersistingFixedAssetMap extends ConcurrentHashMap<UUID, FixedAsset> {
        private final AccountingExtensionFixedAssetStore store;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingFixedAssetMap(AccountingExtensionFixedAssetStore store,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.store = store;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public FixedAsset put(UUID key, FixedAsset value) {
            FixedAsset result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                store.save(value).block();
            }
            return result;
        }

        @Override
        public FixedAsset remove(Object key) {
            FixedAsset result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                store.delete((UUID) key).block();
            }
            return result;
        }
    }

    static final class PersistingTaxDeclarationMap extends ConcurrentHashMap<UUID, TaxDeclaration> {
        private final AccountingExtensionTaxDeclarationStore store;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingTaxDeclarationMap(AccountingExtensionTaxDeclarationStore store,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.store = store;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public TaxDeclaration put(UUID key, TaxDeclaration value) {
            TaxDeclaration result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                store.save(value).block();
            }
            return result;
        }

        @Override
        public TaxDeclaration remove(Object key) {
            TaxDeclaration result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                store.delete((UUID) key).block();
            }
            return result;
        }
    }

    static final class PersistingAttachmentMap extends ConcurrentHashMap<UUID, Attachment> {
        private final AccountingExtensionAttachmentStore store;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingAttachmentMap(AccountingExtensionAttachmentStore store,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.store = store;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public Attachment put(UUID key, Attachment value) {
            Attachment result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                store.save(value).block();
            }
            return result;
        }

        @Override
        public Attachment remove(Object key) {
            Attachment result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                store.delete((UUID) key).block();
            }
            return result;
        }
    }
}
