package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.application.service.JournalEntry.JournalLine;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayrollRunTotals;
import yowyob.comops.api.payroll.domain.model.RunType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountingEntryBuilderTest {

    private static PayrollRun runWithTotals() {
        // gross 1,000,000 ; deductions 150,000 (of which income tax 80,000) ; net 850,000 ;
        // employer charges 200,000
        PayrollRunTotals totals = new PayrollRunTotals(new BigDecimal("1000000"),
                new BigDecimal("150000"), new BigDecimal("80000"), new BigDecimal("850000"),
                new BigDecimal("200000"), 5);
        return PayrollRun.open(UUID.randomUUID(), UUID.randomUUID(), null,
                PayPeriod.parse("2026-10"), RunType.REGULAR, "XAF").markCalculated(totals);
    }

    @Test
    void buildsBalancedJournal() {
        JournalEntry entry = AccountingEntryBuilder.build(runWithTotals());
        assertThat(entry.isBalanced()).isTrue();
        // debit = gross 1,000,000 + employer 200,000 = 1,200,000
        assertThat(entry.totalDebit()).isEqualByComparingTo("1200000");
        assertThat(entry.totalCredit()).isEqualByComparingTo("1200000");
        assertThat(entry.reference()).isEqualTo("PAIE-2026-10");
    }

    @Test
    void mapsTheExpectedSyscohadaAccounts() {
        JournalEntry entry = AccountingEntryBuilder.build(runWithTotals());

        assertThat(credit(entry, "4211")).isEqualByComparingTo("850000");   // net
        assertThat(credit(entry, "4471")).isEqualByComparingTo("80000");    // income tax
        // social part = (150000 - 80000) + 200000 = 270000
        assertThat(credit(entry, "4311")).isEqualByComparingTo("270000");
        assertThat(debit(entry, "6611")).isEqualByComparingTo("1000000");   // gross
        assertThat(debit(entry, "6641")).isEqualByComparingTo("200000");    // employer charges
    }

    private static BigDecimal debit(JournalEntry e, String account) {
        return line(e, account).debit();
    }

    private static BigDecimal credit(JournalEntry e, String account) {
        return line(e, account).credit();
    }

    private static JournalLine line(JournalEntry e, String account) {
        return e.lines().stream().filter(l -> l.accountCode().equals(account)).findFirst().orElseThrow();
    }
}
