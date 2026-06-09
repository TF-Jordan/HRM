package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.adapter.out.document.PayrollPdfRenderer;
import yowyob.comops.api.payroll.adapter.out.document.PayrollPdfRenderer.PayslipView;
import yowyob.comops.api.payroll.adapter.out.document.PayrollPdfRenderer.WorkCertificateView;
import yowyob.comops.api.payroll.application.port.in.GeneratePayrollDocumentUseCase;
import yowyob.comops.api.payroll.application.port.out.DocumentSigningKeyProvider;
import yowyob.comops.api.payroll.application.port.out.DocumentStoragePort;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.EmployerInfo;
import yowyob.comops.api.payroll.application.port.out.EmployerInfoPort;
import yowyob.comops.api.payroll.application.port.out.FinalSettlementRepository;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.AnnualAccumulatorRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollDocumentRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.PayslipLineRepository;
import yowyob.comops.api.payroll.domain.model.DocumentSeal;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;
import yowyob.comops.api.payroll.domain.model.PayrollDocument;
import yowyob.comops.api.payroll.domain.model.PayrollDocumentType;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayslipLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates electronically-sealed payroll documents. For each document it assembles the data,
 * builds a deterministic canonical content, signs it with {@link DocumentSigner} (the legal
 * "signature électronique"), renders the PDF (with a visible seal block), stores the PDF in
 * file-core, and registers a {@link PayrollDocument} carrying the seal for later verification.
 */
@Service
@Profile("!test-memory")
public class PayrollDocumentService implements GeneratePayrollDocumentUseCase {

    private static final String PDF = "application/pdf";

    private final PayrollDocumentRepository documentRepository;
    private final PayrollEntryRepository payrollEntryRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayslipLineRepository payslipLineRepository;
    private final AnnualAccumulatorRepository annualAccumulatorRepository;
    private final FinalSettlementRepository finalSettlementRepository;
    private final HrmEmployeeDataPort hrmEmployeeDataPort;
    private final EmployerInfoPort employerInfoPort;
    private final DocumentStoragePort documentStoragePort;
    private final DocumentSigningKeyProvider signingKeyProvider;
    private final BusinessEventPublisher businessEventPublisher;

    public PayrollDocumentService(PayrollDocumentRepository documentRepository,
                                  PayrollEntryRepository payrollEntryRepository,
                                  PayrollRunRepository payrollRunRepository,
                                  PayslipLineRepository payslipLineRepository,
                                  AnnualAccumulatorRepository annualAccumulatorRepository,
                                  FinalSettlementRepository finalSettlementRepository,
                                  HrmEmployeeDataPort hrmEmployeeDataPort,
                                  EmployerInfoPort employerInfoPort,
                                  DocumentStoragePort documentStoragePort,
                                  DocumentSigningKeyProvider signingKeyProvider,
                                  BusinessEventPublisher businessEventPublisher) {
        this.documentRepository = documentRepository;
        this.payrollEntryRepository = payrollEntryRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payslipLineRepository = payslipLineRepository;
        this.annualAccumulatorRepository = annualAccumulatorRepository;
        this.finalSettlementRepository = finalSettlementRepository;
        this.hrmEmployeeDataPort = hrmEmployeeDataPort;
        this.employerInfoPort = employerInfoPort;
        this.documentStoragePort = documentStoragePort;
        this.signingKeyProvider = signingKeyProvider;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<PayrollDocument> generatePayslip(UUID payrollEntryId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                payrollEntryRepository.findById(ctx.tenantId(), payrollEntryId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Payroll entry not found")))
                        .flatMap(entry -> Mono.zip(
                                        payrollRunRepository.findById(ctx.tenantId(), entry.payrollRunId()),
                                        hrmEmployeeDataPort.findEmployee(ctx.tenantId(), entry.employeeId()),
                                        payslipLineRepository.findByEntry(ctx.tenantId(), entry.id())
                                                .collectList(),
                                        employerInfoPort.find(ctx.tenantId(), ctx.organizationId()))
                                .flatMap(t -> buildPayslip(ctx, entry, t.getT1(), t.getT2(), t.getT3(),
                                        t.getT4()))));
    }

    private Mono<PayrollDocument> buildPayslip(TenantContext ctx, PayrollEntry entry, PayrollRun run,
                                               EmployeePayrollView emp, List<PayslipLine> lines,
                                               EmployerInfo employer) {
        return cumuls(ctx, entry.employeeId(), run.period().year()).flatMap(cumuls -> {
            PayslipView view = new PayslipView(run.period().format(), emp.displayName(), emp.matricule(),
                    emp.socialSecurityNo(), String.valueOf(emp.categorie()), emp.echelon(), emp.hireDate(),
                    entry.brut(), entry.totalDeductions(), entry.incomeTax(), entry.employerCharges(),
                    entry.net(), cumuls.getT1(), cumuls.getT2(), lines,
                    emp.paymentChannel() != null ? emp.paymentChannel().name() : null, emp.accountRef());
            String canonical = PayrollPdfRenderer.payslipCanonical(view, employer);
            DocumentSeal seal = seal(ctx, canonical);
            byte[] pdf = PayrollPdfRenderer.renderPayslip(view, employer, seal);
            String fileName = "bulletin-" + run.period().format() + "-" + safe(emp.matricule()) + ".pdf";
            return store(ctx, PayrollDocumentType.PAYSLIP, entry.id(), entry.employeeId(),
                    run.period().format(), fileName, canonical, seal, pdf);
        });
    }

    @Override
    public Mono<PayrollDocument> generateFinalSettlement(UUID finalSettlementId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                finalSettlementRepository.findById(ctx.tenantId(), finalSettlementId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Final settlement not found")))
                        .flatMap(settlement -> Mono.zip(
                                        employeeName(ctx, settlement.employeeId()),
                                        employerInfoPort.find(ctx.tenantId(), ctx.organizationId()))
                                .flatMap(t -> buildSettlement(ctx, settlement, t.getT1(), t.getT2()))));
    }

    private Mono<PayrollDocument> buildSettlement(TenantContext ctx, FinalSettlement s, String employeeName,
                                                  EmployerInfo employer) {
        String canonical = PayrollPdfRenderer.settlementCanonical(s, employeeName);
        DocumentSeal seal = seal(ctx, canonical);
        byte[] pdf = PayrollPdfRenderer.renderFinalSettlement(s, employeeName, employer, seal);
        String fileName = "stc-" + s.periode() + "-" + s.employeeId() + ".pdf";
        return store(ctx, PayrollDocumentType.FINAL_SETTLEMENT, s.id(), s.employeeId(), s.periode(),
                fileName, canonical, seal, pdf);
    }

    @Override
    public Mono<PayrollDocument> generateWorkCertificate(UUID employeeId, String position) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                Mono.zip(hrmEmployeeDataPort.findEmployee(ctx.tenantId(), employeeId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Employee not found"))),
                        employerInfoPort.find(ctx.tenantId(), ctx.organizationId()))
                        .flatMap(t -> {
                            EmployeePayrollView emp = t.getT1();
                            EmployerInfo employer = t.getT2();
                            WorkCertificateView view = new WorkCertificateView(employeeId, emp.displayName(),
                                    emp.matricule(),
                                    position != null ? position
                                            : (emp.position() != null ? emp.position() : emp.departmentCode()),
                                    emp.hireDate(), emp.departureDate());
                            String canonical = PayrollPdfRenderer.certificateCanonical(view, employer);
                            DocumentSeal seal = seal(ctx, canonical);
                            byte[] pdf = PayrollPdfRenderer.renderWorkCertificate(view, employer, seal);
                            String fileName = "certificat-travail-" + safe(emp.matricule()) + ".pdf";
                            return store(ctx, PayrollDocumentType.WORK_CERTIFICATE, employeeId, employeeId,
                                    null, fileName, canonical, seal, pdf);
                        }));
    }

    @Override
    public Mono<PayrollDocument> getDocument(UUID documentId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> documentRepository.findById(ctx.tenantId(), documentId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Document not found"))));
    }

    @Override
    public Flux<PayrollDocument> listForEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> documentRepository.findByEmployee(ctx.tenantId(), employeeId));
    }

    @Override
    public Mono<DocumentVerification> verify(UUID documentId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> documentRepository.findById(ctx.tenantId(), documentId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Document not found")))
                        .map(doc -> {
                            boolean valid = DocumentSigner.verify(doc.canonicalContent(), doc.seal(),
                                    signingKeyProvider.publicKey());
                            return new DocumentVerification(valid, doc.verificationCode(),
                                    doc.contentHashHex(), doc.algorithm(), doc.signedAt());
                        }));
    }

    // --- helpers ---

    private Mono<PayrollDocument> store(TenantContext ctx, PayrollDocumentType type, UUID subjectId,
                                        UUID employeeId, String periode, String fileName, String canonical,
                                        DocumentSeal seal, byte[] pdf) {
        return documentStoragePort.store(fileName, PDF, pdf).flatMap(fileId -> {
            PayrollDocument doc = PayrollDocument.create(ctx.tenantId(), ctx.organizationId(), employeeId,
                    type, subjectId, periode, fileId, fileName, canonical, seal);
            return documentRepository.save(doc).flatMap(saved ->
                    businessEventPublisher.publish(BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                                    "PAYROLL_DOCUMENT_GENERATED", "PAYROLL_DOCUMENT", saved.id(),
                                    Map.of("type", type.name(), "fileId", fileId,
                                            "verificationCode", saved.verificationCode())))
                            .thenReturn(saved));
        });
    }

    private DocumentSeal seal(TenantContext ctx, String canonical) {
        String signedBy = ctx.userId() != null ? ctx.userId().toString() : "system";
        return DocumentSigner.seal(canonical, signingKeyProvider.privateKey(), signingKeyProvider.keyId(),
                signedBy);
    }

    private Mono<Tuple2<BigDecimal, BigDecimal>> cumuls(TenantContext ctx, UUID employeeId, int year) {
        return annualAccumulatorRepository.findByEmployeeAndYear(ctx.tenantId(), employeeId, year)
                .map(acc -> reactor.util.function.Tuples.of(acc.cumulativeGross(), acc.cumulativeNet()))
                .defaultIfEmpty(reactor.util.function.Tuples.of(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    private Mono<String> employeeName(TenantContext ctx, UUID employeeId) {
        return hrmEmployeeDataPort.findEmployee(ctx.tenantId(), employeeId)
                .map(EmployeePayrollView::displayName)
                .defaultIfEmpty(employeeId.toString());
    }

    private static String safe(String s) {
        return s == null ? "x" : s.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
