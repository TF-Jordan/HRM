package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * A double-entry accounting journal for a payroll run, ready to push to accounting-core.
 *
 * @param period      canonical {@code YYYY-MM} period
 * @param reference   human-readable reference (e.g. {@code PAIE-2026-10})
 * @param lines       journal lines (account, label, debit, credit)
 * @param totalDebit  sum of debits
 * @param totalCredit sum of credits
 */
public record JournalEntry(
        String period,
        String reference,
        List<JournalLine> lines,
        BigDecimal totalDebit,
        BigDecimal totalCredit) {

    /** A balanced journal has equal total debit and credit. */
    public boolean isBalanced() {
        return totalDebit.compareTo(totalCredit) == 0;
    }

    /** One line of the journal: exactly one of debit/credit is non-zero. */
    public record JournalLine(String accountCode, String label, BigDecimal debit, BigDecimal credit) {}
}
