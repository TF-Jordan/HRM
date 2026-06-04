package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FinalSettlementCalculatorTest {

    private static FinalSettlementInput input(TerminationReason reason, LocalDate hire,
                                              LocalDate departure, String leaveDays, int noticeMonths,
                                              String loan) {
        return new FinalSettlementInput(new BigDecimal("400000"), hire, departure, reason,
                new BigDecimal(leaveDays), 31, 31, noticeMonths, BigDecimal.ZERO, new BigDecimal(loan));
    }

    @Test
    void dismissalWithSeniorityYieldsSeveranceAndNotice() {
        // 7 years: severance factor = 5*0.20 + 2*0.25 = 1.50 → 600000
        FinalSettlementResult r = FinalSettlementCalculator.calculate(
                input(TerminationReason.DISMISSAL, LocalDate.of(2019, 6, 1), LocalDate.of(2026, 10, 15),
                        "10", 1, "0"));

        assertThat(r.seniorityYears()).isEqualTo(7);
        assertThat(r.proratedSalary()).isEqualByComparingTo("400000");      // full month
        assertThat(r.leaveCompensation()).isEqualByComparingTo("133333");   // 10 * 400000/30
        assertThat(r.noticeIndemnity()).isEqualByComparingTo("400000");     // 1 month
        assertThat(r.severanceIndemnity()).isEqualByComparingTo("600000");  // 1.50 * 400000
        assertThat(r.grossSettlement()).isEqualByComparingTo("1533333");
        assertThat(r.netSettlement()).isEqualByComparingTo("1533333");
    }

    @Test
    void resignationHasNoSeveranceNoNotice() {
        FinalSettlementResult r = FinalSettlementCalculator.calculate(
                input(TerminationReason.RESIGNATION, LocalDate.of(2019, 6, 1), LocalDate.of(2026, 10, 15),
                        "10", 1, "0"));

        assertThat(r.severanceIndemnity()).isEqualByComparingTo("0");
        assertThat(r.noticeIndemnity()).isEqualByComparingTo("0");
        assertThat(r.grossSettlement()).isEqualByComparingTo("533333");     // 400000 + 133333
    }

    @Test
    void grossMisconductForfeitsSeveranceAndNotice() {
        FinalSettlementResult r = FinalSettlementCalculator.calculate(
                input(TerminationReason.DISMISSAL_GROSS_MISCONDUCT, LocalDate.of(2010, 1, 1),
                        LocalDate.of(2026, 10, 31), "0", 1, "0"));

        assertThat(r.severanceIndemnity()).isEqualByComparingTo("0");
        assertThat(r.noticeIndemnity()).isEqualByComparingTo("0");
    }

    @Test
    void noSeveranceBelowTwoYears() {
        FinalSettlementResult r = FinalSettlementCalculator.calculate(
                input(TerminationReason.DISMISSAL, LocalDate.of(2025, 6, 1), LocalDate.of(2026, 5, 31),
                        "0", 0, "0"));
        assertThat(r.seniorityYears()).isZero();
        assertThat(r.severanceIndemnity()).isEqualByComparingTo("0");
    }

    @Test
    void severanceTiersCompoundBeyondFifteenYears() {
        // 17 years: 5*0.20 + 5*0.25 + 5*0.30 + 2*0.35 = 4.45 → 1,780,000
        FinalSettlementResult r = FinalSettlementCalculator.calculate(
                input(TerminationReason.RETIREMENT, LocalDate.of(2009, 1, 1), LocalDate.of(2026, 1, 1),
                        "0", 0, "0"));
        assertThat(r.seniorityYears()).isEqualTo(17);
        assertThat(r.severanceIndemnity()).isEqualByComparingTo("1780000");
    }

    @Test
    void proratesFinalMonthSalary() {
        FinalSettlementInput in = new FinalSettlementInput(new BigDecimal("400000"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 10, 15), TerminationReason.RESIGNATION,
                BigDecimal.ZERO, 15, 31, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        FinalSettlementResult r = FinalSettlementCalculator.calculate(in);
        assertThat(r.proratedSalary()).isEqualByComparingTo("193548");      // 400000 * 15/31
    }

    @Test
    void loanBalanceClearedAndCappedAtGross() {
        // resignation gross = 533333; loan 600000 → deduct 533333, net 0
        FinalSettlementResult r = FinalSettlementCalculator.calculate(
                input(TerminationReason.RESIGNATION, LocalDate.of(2019, 6, 1), LocalDate.of(2026, 10, 15),
                        "10", 0, "600000"));
        assertThat(r.grossSettlement()).isEqualByComparingTo("533333");
        assertThat(r.loanDeducted()).isEqualByComparingTo("533333");
        assertThat(r.netSettlement()).isEqualByComparingTo("0");
    }
}
