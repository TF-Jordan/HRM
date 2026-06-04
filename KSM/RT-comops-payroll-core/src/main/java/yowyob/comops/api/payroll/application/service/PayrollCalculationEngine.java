package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The configurable, jurisdiction-agnostic calculation engine — successor to hrm-core's
 * hard-coded {@code PayrollCalculationEngine}.
 *
 * It assembles gross, derives the taxable net from a configurable abatement policy, then walks
 * the active {@link PayElement}s in display order. Each element is evaluated against a
 * {@link PayrollCalculationContext}; its result is registered back into the context so later
 * elements can reference earlier ones (e.g. CAC = 10% of IRPP). Deductions reduce the net,
 * employer charges are informational, and the configured income-tax codes are summed for
 * reporting. Pure and stateless — all I/O lives in the orchestration layer.
 */
public final class PayrollCalculationEngine {

    private PayrollCalculationEngine() {}

    public static CalculationResult calculate(CalculationRequest request) {
        GrossComponents gc = request.gross();
        BigDecimal gross = gc.gross();
        BigDecimal taxableNet = taxableNet(gross, request.abatementRate(), request.abatementCap());

        PayrollCalculationContext ctx = new PayrollCalculationContext(
                gross, gc.proratedBaseSalary(), gc.benefitsInKind(), taxableNet);

        List<ComputedLine> lines = new ArrayList<>();
        lines.addAll(earningLines(gc));

        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal employerCharges = BigDecimal.ZERO;
        BigDecimal incomeTax = BigDecimal.ZERO;

        List<PayElement> ordered = new ArrayList<>(request.elements());
        ordered.sort(Comparator.comparingInt(PayElement::displayOrder));

        for (PayElement element : ordered) {
            BigDecimal amount = PayElementEvaluator.evaluate(
                    element, ctx, request.bracketTables(), request.lookupTables());
            ctx.register(element.code(), amount);

            lines.add(ComputedLine.of(element.code(), element.label(), element.category(),
                    baseForLine(element, ctx), element.rate(), amount));

            switch (element.category()) {
                case DEDUCTION -> totalDeductions = totalDeductions.add(amount);
                case EMPLOYER_CHARGE -> employerCharges = employerCharges.add(amount);
                case EARNING, INFORMATIONAL -> { /* earnings already in gross; info is display-only */ }
            }
            if (request.incomeTaxCodes() != null && request.incomeTaxCodes().contains(element.code())) {
                incomeTax = incomeTax.add(amount);
            }
        }

        BigDecimal voluntary = request.voluntaryDeductions() == null
                ? BigDecimal.ZERO : Rounding.money(request.voluntaryDeductions());
        BigDecimal net = gross.subtract(totalDeductions).subtract(voluntary);

        return new CalculationResult(gross, taxableNet, Rounding.money(totalDeductions),
                Rounding.money(incomeTax), Rounding.money(employerCharges), voluntary,
                Rounding.money(net), List.copyOf(lines));
    }

    /** Taxable net = gross − min(gross × abatementRate, abatementCap), floored at zero. */
    static BigDecimal taxableNet(BigDecimal gross, BigDecimal abatementRate, BigDecimal abatementCap) {
        if (gross == null || gross.signum() <= 0 || abatementRate == null) {
            return gross == null ? BigDecimal.ZERO : Rounding.money(gross);
        }
        BigDecimal abatement = gross.multiply(abatementRate);
        if (abatementCap != null && abatement.compareTo(abatementCap) > 0) {
            abatement = abatementCap;
        }
        BigDecimal net = gross.subtract(abatement);
        return Rounding.money(net.signum() < 0 ? BigDecimal.ZERO : net);
    }

    private static List<ComputedLine> earningLines(GrossComponents gc) {
        List<ComputedLine> lines = new ArrayList<>();
        lines.add(ComputedLine.of("SALAIRE_BASE", "Salaire de base", PayElementCategory.EARNING,
                gc.contractualBaseSalary(), null, gc.proratedBaseSalary()));
        if (gc.benefitsInKind().signum() > 0) {
            lines.add(ComputedLine.of("AVANTAGES_NATURE", "Avantages en nature",
                    PayElementCategory.EARNING, null, null, gc.benefitsInKind()));
        }
        if (gc.overtimeAmount().signum() > 0) {
            lines.add(ComputedLine.of("HEURES_SUP", "Heures supplémentaires",
                    PayElementCategory.EARNING, null, null, gc.overtimeAmount()));
        }
        if (gc.bonuses().signum() > 0) {
            lines.add(ComputedLine.of("PRIMES", "Primes", PayElementCategory.EARNING,
                    null, null, gc.bonuses()));
        }
        return lines;
    }

    /** The base shown on a payslip line: the resolved reference for RATE elements, else null. */
    private static BigDecimal baseForLine(PayElement element, PayrollCalculationContext ctx) {
        return switch (element.method()) {
            case RATE -> Rounding.clamp(ctx.resolve(element.baseReference()),
                    element.floor(), element.ceiling());
            case BRACKET, LOOKUP_TABLE -> ctx.resolve(element.baseReference());
            case FLAT, FORMULA -> null;
        };
    }
}
