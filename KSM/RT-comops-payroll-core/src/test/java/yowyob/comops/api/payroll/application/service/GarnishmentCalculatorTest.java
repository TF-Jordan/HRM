package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.application.service.GarnishmentCalculator.Allocation;
import yowyob.comops.api.payroll.application.service.GarnishmentCalculator.Band;
import yowyob.comops.api.payroll.application.service.GarnishmentCalculator.Request;
import yowyob.comops.api.payroll.application.service.GarnishmentCalculator.Result;
import yowyob.comops.api.payroll.domain.model.GarnishmentType;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GarnishmentCalculatorTest {

    private static List<Band> bands() {
        return List.of(
                new Band(new BigDecimal("100000"), new BigDecimal("0.05")),
                new Band(new BigDecimal("300000"), new BigDecimal("0.10")),
                new Band(new BigDecimal("500000"), new BigDecimal("0.20")),
                new Band(null, new BigDecimal("0.33")));
    }

    private static BigDecimal amount(Result r, GarnishmentType type) {
        return r.allocations().stream().filter(a -> a.type() == type)
                .map(Allocation::allocated).findFirst().orElseThrow();
    }

    @Test
    void computesProgressiveSeizableQuota() {
        // 100000*5% + 200000*10% + 100000*20% = 5000 + 20000 + 20000 = 45000
        assertThat(GarnishmentCalculator.ordinaryQuota(new BigDecimal("400000"), bands()))
                .isEqualByComparingTo("45000");
    }

    @Test
    void creditorLimitedToOrdinaryQuota() {
        Result r = GarnishmentCalculator.allocate(new BigDecimal("400000"),
                List.of(new Request(GarnishmentType.CREDITOR, new BigDecimal("100000"))), bands());
        assertThat(amount(r, GarnishmentType.CREDITOR)).isEqualByComparingTo("45000");
        assertThat(r.totalWithheld()).isEqualByComparingTo("45000");
    }

    @Test
    void alimonyReachesBeyondQuotaAndIsPaidFirst() {
        Result r = GarnishmentCalculator.allocate(new BigDecimal("400000"), List.of(
                new Request(GarnishmentType.CREDITOR, new BigDecimal("100000")),
                new Request(GarnishmentType.ALIMONY, new BigDecimal("200000"))), bands());
        assertThat(amount(r, GarnishmentType.ALIMONY)).isEqualByComparingTo("200000"); // not capped by quota
        assertThat(amount(r, GarnishmentType.CREDITOR)).isEqualByComparingTo("45000");  // ordinary quota
        assertThat(r.totalWithheld()).isEqualByComparingTo("245000");
    }

    @Test
    void totalNeverExceedsNet() {
        Result r = GarnishmentCalculator.allocate(new BigDecimal("400000"), List.of(
                new Request(GarnishmentType.ALIMONY, new BigDecimal("500000")),
                new Request(GarnishmentType.CREDITOR, new BigDecimal("50000"))), bands());
        assertThat(amount(r, GarnishmentType.ALIMONY)).isEqualByComparingTo("400000");
        assertThat(amount(r, GarnishmentType.CREDITOR)).isEqualByComparingTo("0");
        assertThat(r.totalWithheld()).isEqualByComparingTo("400000");
    }

    @Test
    void taxLevyHasPriorityOverCreditorWithinQuota() {
        Result r = GarnishmentCalculator.allocate(new BigDecimal("400000"), List.of(
                new Request(GarnishmentType.CREDITOR, new BigDecimal("50000")),
                new Request(GarnishmentType.TAX_LEVY, new BigDecimal("30000"))), bands());
        assertThat(amount(r, GarnishmentType.TAX_LEVY)).isEqualByComparingTo("30000");
        assertThat(amount(r, GarnishmentType.CREDITOR)).isEqualByComparingTo("15000"); // 45000 - 30000
        assertThat(r.totalWithheld()).isEqualByComparingTo("45000");
    }
}
