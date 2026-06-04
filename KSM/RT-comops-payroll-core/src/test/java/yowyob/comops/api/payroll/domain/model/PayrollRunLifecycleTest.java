package yowyob.comops.api.payroll.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayrollRunLifecycleTest {

    private static PayrollRun open() {
        return PayrollRun.open(UUID.randomUUID(), UUID.randomUUID(), null,
                PayPeriod.parse("2026-10"), RunType.REGULAR, "XAF");
    }

    private static PayrollRunTotals totals() {
        return new PayrollRunTotals(new BigDecimal("1000000"), new BigDecimal("150000"),
                new BigDecimal("80000"), new BigDecimal("850000"), new BigDecimal("200000"), 5);
    }

    @Test
    void opensInDraftWithZeroTotals() {
        PayrollRun run = open();
        assertThat(run.status()).isEqualTo(PayrollRunStatus.DRAFT);
        assertThat(run.totalGross()).isEqualByComparingTo("0");
        assertThat(run.nbEmployes()).isZero();
    }

    @Test
    void happyPathThroughEveryState() {
        UUID validator = UUID.randomUUID();
        UUID approver = UUID.randomUUID();
        PayrollRun run = open()
                .lockVariables()
                .markCalculated(totals())
                .putInReview()
                .validate(validator)
                .approve(approver)
                .initiatePayment()
                .markPaid()
                .close();

        assertThat(run.status()).isEqualTo(PayrollRunStatus.CLOSED);
        assertThat(run.totalNet()).isEqualByComparingTo("850000");
        assertThat(run.nbEmployes()).isEqualTo(5);
        assertThat(run.validatedBy()).isEqualTo(validator);
        assertThat(run.approvedBy()).isEqualTo(approver);
        assertThat(run.calculatedAt()).isNotNull();
        assertThat(run.validatedAt()).isNotNull();
        assertThat(run.approvedAt()).isNotNull();
        assertThat(run.paidAt()).isNotNull();
        assertThat(run.closedAt()).isNotNull();
    }

    @Test
    void calculateMayShortcutFromDraft() {
        PayrollRun run = open().markCalculated(totals());
        assertThat(run.status()).isEqualTo(PayrollRunStatus.CALCULATED);
    }

    @Test
    void canValidateDirectlyFromCalculatedWithoutReview() {
        PayrollRun run = open().markCalculated(totals()).validate(UUID.randomUUID());
        assertThat(run.status()).isEqualTo(PayrollRunStatus.VALIDATED);
    }

    @Test
    void rejectsApprovalBeforeValidation() {
        PayrollRun calculated = open().markCalculated(totals());
        assertThatThrownBy(() -> calculated.approve(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPaymentBeforeApproval() {
        PayrollRun validated = open().markCalculated(totals()).validate(UUID.randomUUID());
        assertThatThrownBy(validated::initiatePayment)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsDoubleValidation() {
        PayrollRun validated = open().markCalculated(totals()).validate(UUID.randomUUID());
        assertThatThrownBy(() -> validated.validate(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }
}
