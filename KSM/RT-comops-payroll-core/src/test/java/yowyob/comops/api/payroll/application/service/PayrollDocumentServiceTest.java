package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.out.AnnualAccumulatorRepository;
import yowyob.comops.api.payroll.application.port.out.DocumentSigningKeyProvider;
import yowyob.comops.api.payroll.application.port.out.DocumentStoragePort;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.EmployerInfo;
import yowyob.comops.api.payroll.application.port.out.EmployerInfoPort;
import yowyob.comops.api.payroll.application.port.out.FinalSettlementRepository;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.PayrollDocumentRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.PayslipLineRepository;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PayrollDocumentType;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayslipLine;
import yowyob.comops.api.payroll.domain.model.PayslipLineType;
import yowyob.comops.api.payroll.domain.model.RunType;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PayrollDocumentServiceTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID ORG = UUID.randomUUID();
    private static final UUID EMP = UUID.randomUUID();
    private static final UUID FILE_ID = UUID.randomUUID();

    private static KeyPair keyPair;

    private PayrollDocumentRepository documentRepo;
    private PayrollEntryRepository entryRepo;
    private PayrollRunRepository runRepo;
    private PayslipLineRepository lineRepo;
    private AnnualAccumulatorRepository accumulatorRepo;
    private FinalSettlementRepository settlementRepo;
    private HrmEmployeeDataPort hrmPort;
    private EmployerInfoPort employerPort;
    private DocumentStoragePort storagePort;
    private BusinessEventPublisher events;
    private PayrollDocumentService service;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        keyPair = g.generateKeyPair();

        documentRepo = mock(PayrollDocumentRepository.class);
        entryRepo = mock(PayrollEntryRepository.class);
        runRepo = mock(PayrollRunRepository.class);
        lineRepo = mock(PayslipLineRepository.class);
        accumulatorRepo = mock(AnnualAccumulatorRepository.class);
        settlementRepo = mock(FinalSettlementRepository.class);
        hrmPort = mock(HrmEmployeeDataPort.class);
        employerPort = mock(EmployerInfoPort.class);
        storagePort = mock(DocumentStoragePort.class);
        events = mock(BusinessEventPublisher.class);

        DocumentSigningKeyProvider keyProvider = new DocumentSigningKeyProvider() {
            public String keyId() { return "test-key-1"; }
            public PrivateKey privateKey() { return keyPair.getPrivate(); }
            public PublicKey publicKey() { return keyPair.getPublic(); }
        };

        service = new PayrollDocumentService(documentRepo, entryRepo, runRepo, lineRepo, accumulatorRepo,
                settlementRepo, hrmPort, employerPort, storagePort, keyProvider, events);
    }

    @Test
    void generatesSignedPayslipThatVerifies() {
        PayrollRun run = PayrollRun.open(TENANT, ORG, null, PayPeriod.parse("2026-10"), RunType.REGULAR,
                "XAF");
        PayrollEntry entry = PayrollEntry.create(TENANT, ORG, run.id(), EMP, "XAF",
                new BigDecimal("400000"), new BigDecimal("400000"), new BigDecimal("67934"),
                new BigDecimal("40334"), new BigDecimal("51800"), new BigDecimal("332066"),
                PaymentChannel.BANK_TRANSFER, "ACC-1");
        List<PayslipLine> lines = List.of(
                PayslipLine.create(TENANT, entry.id(), "SALAIRE_BASE", "Salaire de base",
                        PayslipLineType.EARNING, new BigDecimal("400000"), null, new BigDecimal("400000"), 1),
                PayslipLine.create(TENANT, entry.id(), "CNPS_PV_EE", "CNPS (part salariale)",
                        PayslipLineType.DEDUCTION, new BigDecimal("400000"), new BigDecimal("0.042"),
                        new BigDecimal("16800"), 2));

        when(entryRepo.findById(TENANT, entry.id())).thenReturn(Mono.just(entry));
        when(runRepo.findById(TENANT, run.id())).thenReturn(Mono.just(run));
        when(lineRepo.findByEntry(TENANT, entry.id())).thenReturn(Flux.fromIterable(lines));
        when(hrmPort.findEmployee(TENANT, EMP)).thenReturn(Mono.just(employee()));
        when(employerPort.find(TENANT, ORG)).thenReturn(Mono.just(employer()));
        when(accumulatorRepo.findByEmployeeAndYear(any(), any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());
        when(storagePort.store(any(), org.mockito.ArgumentMatchers.eq("application/pdf"), any()))
                .thenReturn(Mono.just(FILE_ID));
        when(documentRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(events.publish(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.generatePayslip(entry.id()).contextWrite(withContext()))
                .assertNext(doc -> {
                    assertThat(doc.type()).isEqualTo(PayrollDocumentType.PAYSLIP);
                    assertThat(doc.fileId()).isEqualTo(FILE_ID);
                    assertThat(doc.fileName()).startsWith("bulletin-2026-10-");
                    assertThat(doc.verificationCode()).matches("[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}");
                    // the stored seal verifies against the stored canonical content
                    assertThat(DocumentSigner.verify(doc.canonicalContent(), doc.seal(),
                            keyPair.getPublic())).isTrue();
                })
                .verifyComplete();
    }

    private EmployeePayrollView employee() {
        return new EmployeePayrollView(EMP, ORG, null, UUID.randomUUID(), "EMP-001", "Alice Mbarga",
                "CNPS-123", 6, "B", "DEP", LocalDate.of(2020, 1, 1), null, MaritalStatus.SINGLE, 0,
                new BigDecimal("400000"), BigDecimal.ZERO, "CM", null, PaymentChannel.BANK_TRANSFER, "ACC-1");
    }

    private EmployerInfo employer() {
        return new EmployerInfo("ACME SARL", "ACME", "RC/DLA/2020/B/1234", "M012345678", "J123",
                "Jean Directeur", "contact@acme.cm");
    }

    private Function<Context, Context> withContext() {
        TenantContext tc = new TenantContext(TENANT, ORG, null, UUID.randomUUID(), UUID.randomUUID());
        return ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, tc);
    }
}
