package yowyob.comops.api.accounting.extension.web;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import yowyob.comops.api.accounting.extension.service.AccountingLegacyReportingService;
import yowyob.comops.api.common.domain.model.ApiResponse;

@RestController
@RequestMapping("/api/accounting/rapport")
public class AccountingLegacyRapportController {

    private final AccountingLegacyReportingService reportingService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingLegacyRapportController(AccountingLegacyReportingService reportingService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.reportingService = reportingService;
        this.contextResolver = contextResolver;
    }

    @GetMapping("/bilan")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.BilanDto>>> bilan(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> reportingService.generateBilan(startDate, endDate, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Balance sheet generated successfully")));
    }

    @GetMapping(value = {"/bilan/pdf", "/bilan/export/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> bilanPdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return bilan(request, startDate, endDate)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=bilan_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf("BILAN", String.valueOf(response.getBody().data()))));
    }

    @GetMapping("/compte-resultat")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.CompteResultatDto>>> compteResultat(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> reportingService.generateCompteResultat(startDate, endDate, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Income statement generated successfully")));
    }

    @GetMapping(value = {"/compte-resultat/pdf", "/compte-resultat/export/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> compteResultatPdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return compteResultat(request, startDate, endDate)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=compte_resultat_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf("COMPTE_RESULTAT", String.valueOf(response.getBody().data()))));
    }

    @GetMapping("/flux-tresorerie")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.CashFlowDto>>> fluxTresorerie(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> reportingService.generateCashFlow(startDate, endDate, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Cash flow generated successfully")));
    }

    @GetMapping(value = {"/flux-tresorerie/pdf", "/flux-tresorerie/export/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> fluxTresoreriePdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fluxTresorerie(request, startDate, endDate)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=flux_tresorerie_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf("FLUX_TRESORERIE", String.valueOf(response.getBody().data()))));
    }

    @GetMapping("/resume-executif")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.ExecutiveSummaryDto>>> resumeExecutif(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> reportingService.generateExecutiveSummary(startDate, endDate, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Executive summary generated successfully")));
    }

    @GetMapping(value = {"/resume-executif/pdf", "/resume-executif/export/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> resumeExecutifPdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return resumeExecutif(request, startDate, endDate)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=resume_executif_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf("RESUME_EXECUTIF", String.valueOf(response.getBody().data()))));
    }

    @GetMapping("/grand-livre")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AccountingLegacyDtos.GrandLivreDto>>>> grandLivre(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> reportingService.generateGrandLivre(startDate, endDate, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "General Ledger generated successfully")));
    }

    @GetMapping(value = {"/grand-livre/pdf", "/grand-livre/export/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> grandLivrePdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return grandLivre(request, startDate, endDate)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=grand_livre_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf("GRAND_LIVRE", String.valueOf(response.getBody().data()))));
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<ApiResponse<AccountingLegacyDtos.BalanceDesComptesDto>>> balance(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return contextResolver.resolve(request)
                .flatMap(context -> reportingService.generateBalanceDesComptes(startDate, endDate, context))
                .map(body -> ResponseEntity.ok(ApiResponse.success(body, "Trial Balance generated successfully")));
    }

    @GetMapping(value = {"/balance/pdf", "/balance/export/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> balancePdf(ServerHttpRequest request,
            @RequestParam("date_debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("date_fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return balance(request, startDate, endDate)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition",
                                "attachment; filename=balance_" + startDate + "_" + endDate + ".pdf")
                        .body(renderSimplePdf("BALANCE", String.valueOf(response.getBody().data()))));
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
