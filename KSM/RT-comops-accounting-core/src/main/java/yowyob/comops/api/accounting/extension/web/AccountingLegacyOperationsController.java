package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.service.AccountingOperationsService;
import yowyob.comops.api.accounting.extension.web.AccountingOperationsViews;
import yowyob.comops.api.accounting.extension.web.AccountingRequests;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AccountingLegacyOperationsController {

    private final AccountingOperationsService accountingOperationsService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingLegacyOperationsController(AccountingOperationsService accountingOperationsService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.accountingOperationsService = accountingOperationsService;
        this.contextResolver = contextResolver;
    }

    @PostMapping(value = {"/api/accounting-service/attachments/upload", "/api/accounting/attachments/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<AccountingOperationsViews.AttachmentView>> uploadAttachmentMultipart(ServerHttpRequest request,
            @RequestPart("file") FilePart file,
            @RequestParam(name = "targetType", required = false) String targetType,
            @RequestParam(name = "targetId", required = false) UUID targetId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.createAttachmentFromFile(
                        targetType == null || targetType.isBlank() ? "GENERIC" : targetType,
                        targetId == null ? UUID.randomUUID() : targetId,
                        file,
                        context))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping(value = {"/api/accounting-service/attachments/upload", "/api/accounting/attachments/upload"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AccountingOperationsViews.AttachmentView>> uploadAttachment(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateAttachmentRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createAttachment(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/attachments/download/{fileName:.+}", "/api/accounting/attachments/download/{fileName:.+}"})
    public Mono<ResponseEntity<byte[]>> downloadAttachment(ServerHttpRequest request,
            @PathVariable("fileName") String fileName) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.downloadAttachment(fileName, context)
                        .zipWith(accountingOperationsService.downloadAttachmentBytes(fileName, context)))
                .map(tuple -> ResponseEntity.ok()
                        .header("Content-Type", tuple.getT1().contentType())
                        .header("Content-Disposition", "inline; filename=\"" + tuple.getT1().filename() + "\"")
                        .body(tuple.getT2()));
    }

    @PostMapping("/api/comptable/cloture/mensuelle/{periodeId}")
    public Mono<ResponseEntity<AccountingOperationsViews.ClosingRunView>> closeMonthly(ServerHttpRequest request,
            @PathVariable UUID periodeId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.startMonthlyClosing(periodeId, context))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/comptable/cloture/status/{periodeId}")
    public Mono<Map<String, Object>> closingStatus(ServerHttpRequest request, @PathVariable UUID periodeId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.closingStatus(periodeId, context));
    }

    @PostMapping("/api/comptable/cloture/annuler/{periodeId}")
    public Mono<AccountingOperationsViews.ClosingRunView> cancelClosing(ServerHttpRequest request,
            @PathVariable UUID periodeId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.cancelClosing(periodeId, context));
    }

    @GetMapping({"/api/accounting-service/tax-declarations/{declarationId}", "/api/accounting/tax-declarations/{declarationId}"})
    public Mono<AccountingOperationsViews.TaxDeclarationView> getTaxDeclaration(ServerHttpRequest request,
            @PathVariable UUID declarationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.getTaxDeclaration(declarationId, context));
    }

    @GetMapping({"/api/accounting-service/tax-declarations/type/{type}", "/api/accounting/tax-declarations/type/{type}"})
    public Mono<List<AccountingOperationsViews.TaxDeclarationView>> listTaxDeclarationsByType(ServerHttpRequest request,
            @PathVariable String type) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingOperationsService.listTaxDeclarationsByType(type, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/tax-declarations/search", "/api/accounting/tax-declarations/search"})
    public Mono<List<AccountingOperationsViews.TaxDeclarationView>> searchTaxDeclarations(ServerHttpRequest request,
            @RequestParam("query") String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingOperationsService.searchTaxDeclarations(query, context))
                .collectList();
    }

    @DeleteMapping({"/api/accounting-service/tax-declarations/{declarationId}", "/api/accounting/tax-declarations/{declarationId}"})
    public Mono<ResponseEntity<Void>> deleteTaxDeclaration(ServerHttpRequest request, @PathVariable UUID declarationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.deleteTaxDeclaration(declarationId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/tax-declarations/generate", "/api/accounting/tax-declarations/generate"})
    public Mono<ResponseEntity<AccountingOperationsViews.TaxDeclarationView>> generateTaxDeclaration(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateTaxDeclarationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.generateTaxDeclaration(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping({"/api/accounting-service/exercices", "/api/accounting/exercices"})
    public Mono<ResponseEntity<AccountingOperationsViews.FiscalYearView>> createExercice(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateFiscalYearRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createFiscalYear(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/exercices", "/api/accounting/exercices"})
    public Mono<List<AccountingOperationsViews.FiscalYearView>> listExercices(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listFiscalYears)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/exercices/{fiscalYearId}", "/api/accounting/exercices/{fiscalYearId}"})
    public Mono<AccountingOperationsViews.FiscalYearView> getExercice(ServerHttpRequest request,
            @PathVariable UUID fiscalYearId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.getFiscalYear(fiscalYearId, context));
    }

    @GetMapping({"/api/accounting-service/exercices/{fiscalYearId}/periodes", "/api/accounting/exercices/{fiscalYearId}/periodes"})
    public Mono<List<AccountingOperationsViews.AccountingPeriodView>> listExercicePeriodes(ServerHttpRequest request,
            @PathVariable UUID fiscalYearId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingOperationsService.listPeriodsByFiscalYear(fiscalYearId, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/exercices/active", "/api/accounting/exercices/active"})
    public Mono<AccountingOperationsViews.FiscalYearView> activeExercice(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingOperationsService::getActiveFiscalYear);
    }

    @PutMapping({"/api/accounting-service/exercices/{fiscalYearId}", "/api/accounting/exercices/{fiscalYearId}"})
    public Mono<AccountingOperationsViews.FiscalYearView> updateExercice(ServerHttpRequest request,
            @PathVariable UUID fiscalYearId,
            @Valid @RequestBody Mono<AccountingRequests.CreateFiscalYearRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.updateFiscalYear(fiscalYearId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping({"/api/accounting-service/exercices/{fiscalYearId}/close", "/api/accounting/exercices/{fiscalYearId}/close"})
    public Mono<AccountingOperationsViews.FiscalYearView> closeExercice(ServerHttpRequest request,
            @PathVariable UUID fiscalYearId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.closeFiscalYear(fiscalYearId, context));
    }

    @DeleteMapping({"/api/accounting-service/exercices/{fiscalYearId}", "/api/accounting/exercices/{fiscalYearId}"})
    public Mono<ResponseEntity<Void>> deleteExercice(ServerHttpRequest request, @PathVariable UUID fiscalYearId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.deleteFiscalYear(fiscalYearId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/periodes", "/api/accounting/periodes"})
    public Mono<ResponseEntity<AccountingOperationsViews.AccountingPeriodView>> createPeriode(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreatePeriodRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createPeriod(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/periodes", "/api/accounting/periodes"})
    public Mono<List<AccountingOperationsViews.AccountingPeriodView>> listPeriodes(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listPeriods)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/periodes/{periodId}", "/api/accounting/periodes/{periodId}"})
    public Mono<AccountingOperationsViews.AccountingPeriodView> getPeriode(ServerHttpRequest request,
            @PathVariable UUID periodId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.getPeriod(periodId, context));
    }

    @GetMapping({"/api/accounting-service/periodes/code/{code}", "/api/accounting/periodes/code/{code}"})
    public Mono<AccountingOperationsViews.AccountingPeriodView> getPeriodeByCode(ServerHttpRequest request,
            @PathVariable String code) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.getPeriodByCode(code, context));
    }

    @GetMapping({"/api/accounting-service/periodes/by-date", "/api/accounting/periodes/by-date"})
    public Mono<AccountingOperationsViews.AccountingPeriodView> getPeriodeByDate(ServerHttpRequest request,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.getPeriodByDate(date, context));
    }

    @GetMapping({"/api/accounting-service/periodes/non-closed", "/api/accounting/periodes/non-closed"})
    public Mono<List<AccountingOperationsViews.AccountingPeriodView>> nonClosedPeriodes(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listNonClosedPeriods)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/periodes/range", "/api/accounting/periodes/range"})
    public Mono<List<AccountingOperationsViews.AccountingPeriodView>> periodesByRange(ServerHttpRequest request,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingOperationsService.listPeriodsByRange(startDate, endDate, context))
                .collectList();
    }

    @PutMapping({"/api/accounting-service/periodes/{periodId}", "/api/accounting/periodes/{periodId}"})
    public Mono<AccountingOperationsViews.AccountingPeriodView> updatePeriode(ServerHttpRequest request,
            @PathVariable UUID periodId,
            @Valid @RequestBody Mono<AccountingRequests.CreatePeriodRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.updatePeriod(periodId, tuple.getT2(), tuple.getT1()));
    }

    @PutMapping({"/api/accounting-service/periodes/{periodId}/close", "/api/accounting/periodes/{periodId}/close"})
    public Mono<AccountingOperationsViews.AccountingPeriodView> closePeriode(ServerHttpRequest request,
            @PathVariable UUID periodId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.closePeriod(periodId, context));
    }

    @DeleteMapping({"/api/accounting-service/periodes/{periodId}", "/api/accounting/periodes/{periodId}"})
    public Mono<ResponseEntity<Void>> deletePeriode(ServerHttpRequest request, @PathVariable UUID periodId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.deletePeriod(periodId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/immobilisations", "/api/accounting/immobilisations"})
    public Mono<ResponseEntity<AccountingOperationsViews.FixedAssetView>> createImmobilisation(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateFixedAssetRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createFixedAsset(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/immobilisations", "/api/accounting/immobilisations"})
    public Mono<List<AccountingOperationsViews.FixedAssetView>> listImmobilisations(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listFixedAssets)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/immobilisations/{fixedAssetId}", "/api/accounting/immobilisations/{fixedAssetId}"})
    public Mono<AccountingOperationsViews.FixedAssetView> getImmobilisation(ServerHttpRequest request,
            @PathVariable UUID fixedAssetId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.getFixedAsset(fixedAssetId, context));
    }

    @PostMapping({"/api/accounting-service/immobilisations/{fixedAssetId}/generate-schedule", "/api/accounting/immobilisations/{fixedAssetId}/generate-schedule"})
    public Mono<Map<String, Object>> generateDepreciationSchedule(ServerHttpRequest request,
            @PathVariable UUID fixedAssetId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.generateDepreciationSchedule(fixedAssetId, context));
    }

    @PostMapping({"/api/accounting-service/immobilisations/post-depreciation", "/api/accounting/immobilisations/post-depreciation"})
    public Mono<List<AccountingOperationsViews.FixedAssetView>> postDepreciation(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::postDepreciation)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/notifications/unread", "/api/accounting/notifications/unread"})
    public Mono<List<AccountingOperationsViews.NotificationView>> unreadNotifications(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listUnreadNotifications)
                .collectList();
    }

    @PostMapping({"/api/accounting-service/notifications/{notificationId}/read", "/api/accounting/notifications/{notificationId}/read"})
    public Mono<AccountingOperationsViews.NotificationView> readNotification(ServerHttpRequest request,
            @PathVariable UUID notificationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.markNotificationRead(notificationId, context));
    }

    @GetMapping("/api/accounting-service/rapport/bilan")
    public Mono<AccountingOperationsViews.ReportExportView> bilan(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return report(request, "BALANCE_SHEET", "TXT", startDate, endDate);
    }

    @GetMapping({"/api/accounting-service/rapport/bilan/pdf", "/api/accounting-service/rapport/bilan/export/pdf"})
    public Mono<ResponseEntity<byte[]>> bilanPdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportPdf(request, "BALANCE_SHEET", "bilan", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/compte-resultat")
    public Mono<AccountingOperationsViews.ReportExportView> compteResultat(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return report(request, "INCOME_STATEMENT", "TXT", startDate, endDate);
    }

    @GetMapping({"/api/accounting-service/rapport/compte-resultat/pdf", "/api/accounting-service/rapport/compte-resultat/export/pdf"})
    public Mono<ResponseEntity<byte[]>> compteResultatPdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportPdf(request, "INCOME_STATEMENT", "compte_resultat", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/flux-tresorerie")
    public Mono<AccountingOperationsViews.ReportExportView> fluxTresorerie(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return report(request, "CASH_FLOW", "TXT", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/flux-tresorerie/pdf")
    public Mono<ResponseEntity<byte[]>> fluxTresoreriePdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportPdf(request, "CASH_FLOW", "flux_tresorerie", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/resume-executif")
    public Mono<AccountingOperationsViews.ReportExportView> resumeExecutif(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return report(request, "EXECUTIVE_SUMMARY", "TXT", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/resume-executif/pdf")
    public Mono<ResponseEntity<byte[]>> resumeExecutifPdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportPdf(request, "EXECUTIVE_SUMMARY", "resume_executif", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/grand-livre")
    public Mono<AccountingOperationsViews.ReportExportView> grandLivre(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return report(request, "GENERAL_LEDGER", "TXT", startDate, endDate);
    }

    @GetMapping({"/api/accounting-service/rapport/grand-livre/pdf", "/api/accounting-service/rapport/grand-livre/export/pdf"})
    public Mono<ResponseEntity<byte[]>> grandLivrePdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportPdf(request, "GENERAL_LEDGER", "grand_livre", startDate, endDate);
    }

    @GetMapping("/api/accounting-service/rapport/balance")
    public Mono<AccountingOperationsViews.ReportExportView> balance(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return report(request, "TRIAL_BALANCE", "TXT", startDate, endDate);
    }

    @GetMapping({"/api/accounting-service/rapport/balance/pdf", "/api/accounting-service/rapport/balance/export/pdf"})
    public Mono<ResponseEntity<byte[]>> balancePdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportPdf(request, "TRIAL_BALANCE", "balance", startDate, endDate);
    }

    @PostMapping("/api/comptable/sync/elasticsearch")
    public Mono<ResponseEntity<AccountingOperationsViews.SynchronizationJobView>> syncElasticsearch(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.triggerSynchronization("ELASTICSEARCH", context))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/api/comptable/sync/redis/clear")
    public Mono<Map<String, Object>> clearRedis(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.clearSynchronizationDomain("REDIS", context));
    }

    @GetMapping("/api/comptable/sync/status")
    public Mono<Map<String, Object>> syncStatus(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingOperationsService::synchronizationStatus);
    }

    @PostMapping("/api/debug/kafka/test")
    public Mono<Map<String, Object>> debugKafka(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .map(context -> Map.of(
                        "channel", "kafka",
                        "status", "OK",
                        "organizationId", context.requireOrganizationId()));
    }

    @PostMapping("/api/debug/redis/test")
    public Mono<Map<String, Object>> debugRedisPost(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.synchronizationStatus(context))
                .map(status -> Map.of(
                        "channel", "redis",
                        "status", "OK",
                        "details", status));
    }

    @GetMapping("/api/debug/redis/test")
    public Mono<Map<String, Object>> debugRedisGet(ServerHttpRequest request) {
        return debugRedisPost(request);
    }

    @PostMapping("/api/debug/sync/test")
    public Mono<ResponseEntity<AccountingOperationsViews.SynchronizationJobView>> debugSync(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.triggerSynchronization("DEBUG_SYNC", context))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/debug/organization/info")
    public Mono<Map<String, Object>> organizationInfo(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .map(context -> Map.of(
                        "tenantId", context.tenantId(),
                        "organizationId", context.requireOrganizationId()));
    }

    @DeleteMapping("/api/debug/redis/clear/{key}")
    public Mono<Map<String, Object>> clearRedisKey(ServerHttpRequest request, @PathVariable String key) {
        return contextResolver.resolve(request)
                .map(context -> Map.of(
                        "status", "OK",
                        "key", key,
                        "organizationId", context.requireOrganizationId()));
    }

    private Mono<AccountingOperationsViews.ReportExportView> report(ServerHttpRequest request,
            String reportType,
            String format,
            LocalDate startDate,
            LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.generateNamedReport(reportType, format, context))
                .map(export -> new AccountingOperationsViews.ReportExportView(
                        export.id(),
                        export.organizationId(),
                        export.reportType(),
                        export.format(),
                        export.status(),
                        export.generatedAt(),
                        export.content() + System.lineSeparator()
                                + "date_debut=" + startDate + System.lineSeparator()
                                + "date_fin=" + endDate));
    }

    private Mono<ResponseEntity<byte[]>> reportPdf(ServerHttpRequest request,
            String reportType,
            String filenamePrefix,
            LocalDate startDate,
            LocalDate endDate) {
        return report(request, reportType, "PDF", startDate, endDate)
                .map(export -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=" + filenamePrefix + "_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf(export.reportType(), export.content())));
    }

    private byte[] renderSimplePdf(String title, String content) {
        String text = title + System.lineSeparator() + System.lineSeparator() + content;
        String escaped = text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", "");
        String stream = "BT /F1 12 Tf 14 TL 50 780 Td (" + escaped.replace("\n", ") Tj T* (") + ") Tj ET";
        byte[] streamBytes = stream.getBytes(StandardCharsets.UTF_8);
        String[] objects = new String[] {
                "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n",
                "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n",
                "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n",
                "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n",
                "5 0 obj << /Length " + streamBytes.length + " >> stream\n" + stream + "\nendstream endobj\n"
        };
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length + 1];
        for (int index = 0; index < objects.length; index++) {
            offsets[index + 1] = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
            pdf.append(objects[index]);
        }
        int xrefOffset = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
        pdf.append("xref\n0 ").append(objects.length + 1).append('\n');
        pdf.append("0000000000 65535 f \n");
        for (int index = 1; index <= objects.length; index++) {
            pdf.append(String.format("%010d 00000 n \n", offsets[index]));
        }
        pdf.append("trailer << /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append('\n');
        pdf.append("%%EOF");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }
}
