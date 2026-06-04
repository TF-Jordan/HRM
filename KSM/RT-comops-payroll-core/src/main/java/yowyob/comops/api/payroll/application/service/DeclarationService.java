package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.GenerateDeclarationUseCase;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.PayslipLineRepository;
import yowyob.comops.api.payroll.domain.model.DeclarationType;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PayslipLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Generates statutory declarations from a calculated run by aggregating the relevant payslip
 * lines per employee. The pay-element codes that feed each declaration are fixed for Cameroon;
 * the per-employee identity (matricule, name, CNPS number) is read from HR. Pure formatting and
 * totals are delegated to {@link DeclarationBuilder}.
 */
@Service
@Profile("!test-memory")
public class DeclarationService implements GenerateDeclarationUseCase {

    /** Employee-side pay-element codes contributing to each declaration. */
    private static final Set<String> CNPS_EMPLOYEE = Set.of("CNPS_PV_EE");
    private static final Set<String> CNPS_EMPLOYER = Set.of("CNPS_PV_ER", "CNPS_AF_ER", "CNPS_AT_ER");
    private static final Set<String> IRPP_EMPLOYEE = Set.of("IRPP", "CAC");

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEntryRepository payrollEntryRepository;
    private final PayslipLineRepository payslipLineRepository;
    private final HrmEmployeeDataPort hrmEmployeeDataPort;

    public DeclarationService(PayrollRunRepository payrollRunRepository,
                              PayrollEntryRepository payrollEntryRepository,
                              PayslipLineRepository payslipLineRepository,
                              HrmEmployeeDataPort hrmEmployeeDataPort) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEntryRepository = payrollEntryRepository;
        this.payslipLineRepository = payslipLineRepository;
        this.hrmEmployeeDataPort = hrmEmployeeDataPort;
    }

    @Override
    public Mono<DeclarationDocument> generate(DeclarationType type, UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                payrollRunRepository.findById(ctx.tenantId(), payrollRunId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Payroll run not found")))
                        .flatMap(run -> payrollEntryRepository.findByRun(ctx.tenantId(), payrollRunId)
                                .concatMap(entry -> lineItem(ctx, type, entry))
                                .collectList()
                                .map(items -> DeclarationBuilder.build(type, run.period().format(), items))));
    }

    @Override
    public Mono<String> generateCsv(DeclarationType type, UUID payrollRunId) {
        return generate(type, payrollRunId).map(DeclarationBuilder::toCsv);
    }

    private Mono<DeclarationLineItem> lineItem(TenantContext ctx, DeclarationType type, PayrollEntry entry) {
        Mono<List<PayslipLine>> linesMono =
                payslipLineRepository.findByEntry(ctx.tenantId(), entry.id()).collectList();
        return linesMono.flatMap(lines -> hrmEmployeeDataPort
                .findEmployee(ctx.tenantId(), entry.employeeId())
                .map(view -> new DeclarationLineItem(entry.employeeId(), view.matricule(),
                        view.displayName(), view.socialSecurityNo(), entry.brut(),
                        sumByCodes(lines, employeeCodes(type)),
                        sumByCodes(lines, employerCodes(type))))
                .defaultIfEmpty(new DeclarationLineItem(entry.employeeId(), entry.employeeId().toString(),
                        "", "", entry.brut(), sumByCodes(lines, employeeCodes(type)),
                        sumByCodes(lines, employerCodes(type)))));
    }

    private static Set<String> employeeCodes(DeclarationType type) {
        return type == DeclarationType.IRPP_CAC ? IRPP_EMPLOYEE : CNPS_EMPLOYEE;
    }

    private static Set<String> employerCodes(DeclarationType type) {
        return type == DeclarationType.IRPP_CAC ? Set.of() : CNPS_EMPLOYER;
    }

    private static BigDecimal sumByCodes(List<PayslipLine> lines, Set<String> codes) {
        return lines.stream()
                .filter(l -> l.payElementCode() != null && codes.contains(l.payElementCode()))
                .map(PayslipLine::montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
