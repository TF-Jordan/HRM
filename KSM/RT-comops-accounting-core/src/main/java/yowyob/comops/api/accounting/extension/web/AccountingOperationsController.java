package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.service.AccountingOperationsService;
import yowyob.comops.api.accounting.extension.web.AccountingOperationsViews;
import yowyob.comops.api.accounting.extension.web.AccountingRequests;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounting-service")
public class AccountingOperationsController {

    private final AccountingOperationsService accountingOperationsService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingOperationsController(AccountingOperationsService accountingOperationsService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.accountingOperationsService = accountingOperationsService;
        this.contextResolver = contextResolver;
    }

    @PostMapping("/closing-runs")
    public Mono<ResponseEntity<AccountingOperationsViews.ClosingRunView>> startClosingRun(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.StartClosingRunRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.startClosingRun(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/closing-runs")
    public Mono<List<AccountingOperationsViews.ClosingRunView>> listClosingRuns(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listClosingRuns)
                .collectList();
    }

    @PostMapping("/closing-runs/{runId}/complete")
    public Mono<AccountingOperationsViews.ClosingRunView> completeClosingRun(ServerHttpRequest request,
            @PathVariable("runId") UUID runId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.completeClosingRun(runId, context));
    }

    @PostMapping("/report-exports")
    public Mono<ResponseEntity<AccountingOperationsViews.ReportExportView>> generateReport(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.GenerateReportExportRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.generateReport(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/report-exports")
    public Mono<List<AccountingOperationsViews.ReportExportView>> listReportExports(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listReportExports)
                .collectList();
    }

    @PostMapping("/fiscal-years")
    public Mono<ResponseEntity<AccountingOperationsViews.FiscalYearView>> createFiscalYear(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateFiscalYearRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createFiscalYear(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/fiscal-years")
    public Mono<List<AccountingOperationsViews.FiscalYearView>> listFiscalYears(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listFiscalYears)
                .collectList();
    }

    @PostMapping("/fiscal-years/{fiscalYearId}/close")
    public Mono<AccountingOperationsViews.FiscalYearView> closeFiscalYear(ServerHttpRequest request,
            @PathVariable("fiscalYearId") UUID fiscalYearId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.closeFiscalYear(fiscalYearId, context));
    }

    @PostMapping("/periods")
    public Mono<ResponseEntity<AccountingOperationsViews.AccountingPeriodView>> createPeriod(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreatePeriodRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createPeriod(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/periods")
    public Mono<List<AccountingOperationsViews.AccountingPeriodView>> listPeriods(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listPeriods)
                .collectList();
    }

    @PostMapping("/periods/{periodId}/close")
    public Mono<AccountingOperationsViews.AccountingPeriodView> closePeriod(ServerHttpRequest request,
            @PathVariable("periodId") UUID periodId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.closePeriod(periodId, context));
    }

    @PostMapping("/fixed-assets")
    public Mono<ResponseEntity<AccountingOperationsViews.FixedAssetView>> createFixedAsset(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateFixedAssetRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createFixedAsset(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/fixed-assets")
    public Mono<List<AccountingOperationsViews.FixedAssetView>> listFixedAssets(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listFixedAssets)
                .collectList();
    }

    @PostMapping("/fixed-assets/{fixedAssetId}/depreciate")
    public Mono<AccountingOperationsViews.FixedAssetView> depreciateFixedAsset(ServerHttpRequest request,
            @PathVariable("fixedAssetId") UUID fixedAssetId,
            @Valid @RequestBody Mono<AccountingRequests.DepreciateFixedAssetRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.depreciateFixedAsset(fixedAssetId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/tax-declarations")
    public Mono<ResponseEntity<AccountingOperationsViews.TaxDeclarationView>> createTaxDeclaration(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateTaxDeclarationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createTaxDeclaration(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/tax-declarations")
    public Mono<List<AccountingOperationsViews.TaxDeclarationView>> listTaxDeclarations(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listTaxDeclarations)
                .collectList();
    }

    @PostMapping("/tax-declarations/{declarationId}/submit")
    public Mono<AccountingOperationsViews.TaxDeclarationView> submitTaxDeclaration(ServerHttpRequest request,
            @PathVariable("declarationId") UUID declarationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.submitTaxDeclaration(declarationId, context));
    }

    @PostMapping("/attachments")
    public Mono<ResponseEntity<AccountingOperationsViews.AttachmentView>> createAttachment(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateAttachmentRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createAttachment(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/attachments")
    public Mono<List<AccountingOperationsViews.AttachmentView>> listAttachments(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listAttachments)
                .collectList();
    }

    @PostMapping("/notifications")
    public Mono<ResponseEntity<AccountingOperationsViews.NotificationView>> createNotification(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.CreateNotificationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.createNotification(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/notifications")
    public Mono<List<AccountingOperationsViews.NotificationView>> listNotifications(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listNotifications)
                .collectList();
    }

    @PostMapping("/notifications/{notificationId}/acknowledge")
    public Mono<AccountingOperationsViews.NotificationView> acknowledgeNotification(ServerHttpRequest request,
            @PathVariable("notificationId") UUID notificationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingOperationsService.acknowledgeNotification(notificationId, context));
    }

    @PostMapping("/synchronization-jobs")
    public Mono<ResponseEntity<AccountingOperationsViews.SynchronizationJobView>> startSynchronizationJob(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingRequests.StartSynchronizationJobRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.startSynchronizationJob(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/synchronization-jobs")
    public Mono<List<AccountingOperationsViews.SynchronizationJobView>> listSynchronizationJobs(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingOperationsService::listSynchronizationJobs)
                .collectList();
    }

    @PostMapping("/synchronization-jobs/{jobId}/complete")
    public Mono<AccountingOperationsViews.SynchronizationJobView> completeSynchronizationJob(ServerHttpRequest request,
            @PathVariable("jobId") UUID jobId,
            @Valid @RequestBody Mono<AccountingRequests.CompleteSynchronizationJobRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingOperationsService.completeSynchronizationJob(jobId, tuple.getT2(), tuple.getT1()));
    }

    @GetMapping("/dashboard")
    public Mono<AccountingOperationsViews.AccountingDashboardView> dashboard(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingOperationsService::dashboard);
    }
}
