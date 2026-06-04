package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.application.service.JournalEntry.JournalLine;
import yowyob.comops.api.payroll.domain.model.PayrollRun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the OHADA (SYSCOHADA) payroll journal entry from a run's totals. Pure and stateless;
 * the resulting {@link JournalEntry} is what a service pushes to accounting-core.
 *
 * <p>The entry is balanced by construction. With
 * {@code gross = net + employeeDeductions}:
 * <pre>
 *   Debit  6611  Appointements et salaires          = gross
 *   Debit  6641  Charges sociales patronales         = employerCharges
 *   Credit 4211  Personnel, rémunérations dues        = net
 *   Credit 4311  Organismes sociaux                   = (employeeDeductions − incomeTax) + employerCharges
 *   Credit 4471  État, impôts sur les salaires        = incomeTax
 * </pre>
 * Debits (gross + employerCharges) equal credits
 * (net + employeeDeductions + employerCharges).
 */
public final class AccountingEntryBuilder {

    private AccountingEntryBuilder() {}

    public static JournalEntry build(PayrollRun run) {
        BigDecimal gross = nz(run.totalGross());
        BigDecimal net = nz(run.totalNet());
        BigDecimal deductions = nz(run.totalEmployeeDeductions());
        BigDecimal incomeTax = nz(run.totalIncomeTax());
        BigDecimal employerCharges = nz(run.totalEmployerCharges());
        BigDecimal socialPart = deductions.subtract(incomeTax).add(employerCharges);
        String reference = "PAIE-" + run.period().format();

        List<JournalLine> lines = new ArrayList<>();
        lines.add(debit("6611", "Appointements et salaires", gross));
        if (employerCharges.signum() > 0) {
            lines.add(debit("6641", "Charges sociales patronales", employerCharges));
        }
        lines.add(credit("4211", "Personnel, rémunérations dues", net));
        if (socialPart.signum() > 0) {
            lines.add(credit("4311", "Organismes sociaux (CNPS, CFC...)", socialPart));
        }
        if (incomeTax.signum() > 0) {
            lines.add(credit("4471", "État, impôts sur les salaires", incomeTax));
        }

        BigDecimal totalDebit = lines.stream().map(JournalLine::debit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(JournalLine::credit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new JournalEntry(run.period().format(), reference, List.copyOf(lines),
                Rounding.money(totalDebit), Rounding.money(totalCredit));
    }

    private static JournalLine debit(String account, String label, BigDecimal amount) {
        return new JournalLine(account, label, Rounding.money(amount), BigDecimal.ZERO);
    }

    private static JournalLine credit(String account, String label, BigDecimal amount) {
        return new JournalLine(account, label, BigDecimal.ZERO, Rounding.money(amount));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
