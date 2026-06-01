package yowyob.comops.api.hrm.config;

import yowyob.comops.api.hrm.application.port.in.RunPayrollUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.PayrollRunRepository;

import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Automatically triggers payroll at 23:59 on the last day of every month for each organization
 * that has active employees but has not yet run payroll for the current period.
 *
 * <p>Can be disabled via {@code hrm.payroll.auto-run.enabled=false}.
 */
@Component
@Profile("r2dbc")
@ConditionalOnProperty(name = "hrm.payroll.auto-run.enabled", havingValue = "true", matchIfMissing = true)
public class PayrollAutoRunScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayrollAutoRunScheduler.class);

    private final RunPayrollUseCase runPayrollUseCase;
    private final EmployeeRepository employeeRepository;
    private final PayrollRunRepository payrollRunRepository;

    public PayrollAutoRunScheduler(RunPayrollUseCase runPayrollUseCase,
                                    EmployeeRepository employeeRepository,
                                    PayrollRunRepository payrollRunRepository) {
        this.runPayrollUseCase = runPayrollUseCase;
        this.employeeRepository = employeeRepository;
        this.payrollRunRepository = payrollRunRepository;
    }

    /** Runs at 23:59:00 on the last calendar day of every month. */
    @Scheduled(cron = "0 59 23 L * *")
    public void autoRunPayrollForAllOrganizations() {
        String periode = currentPeriode();
        LOGGER.info("[PayrollAutoRun] Checking organizations for auto-run — period {}", periode);

        employeeRepository.findDistinctActiveOrganizations()
                .flatMap(pair ->
                        payrollRunRepository.findByOrganizationIdAndPeriode(
                                pair.tenantId(), pair.organizationId(), periode)
                                .hasElement()
                                .filter(exists -> !exists)
                                .flatMap(__ ->
                                        runPayrollUseCase.autoRunPayroll(
                                                pair.tenantId(), pair.organizationId(), null, periode)
                                                .doOnSuccess(run -> LOGGER.info(
                                                        "[PayrollAutoRun] Completed for org {} period {} — {} employees",
                                                        pair.organizationId(), periode, run.nbEmployes()))
                                                .doOnError(err -> LOGGER.error(
                                                        "[PayrollAutoRun] Failed for org {} period {}",
                                                        pair.organizationId(), periode, err))
                                                .onErrorResume(err -> Mono.empty())
                                )
                )
                .subscribe(
                        run -> {},
                        err -> LOGGER.error("[PayrollAutoRun] Unhandled error during auto-run sweep", err)
                );
    }

    private static String currentPeriode() {
        YearMonth ym = YearMonth.now();
        return String.format("%d-%02d", ym.getYear(), ym.getMonthValue());
    }
}
