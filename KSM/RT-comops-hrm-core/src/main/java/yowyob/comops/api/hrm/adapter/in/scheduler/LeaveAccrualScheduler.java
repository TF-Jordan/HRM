package yowyob.comops.api.hrm.adapter.in.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import yowyob.comops.api.hrm.application.port.in.RunLeaveAccrualUseCase;

/**
 * Drives the monthly leave accrual on a schedule. Runs across every organization that has active
 * employees; the underlying use case is idempotent per calendar month, so a daily cron simply
 * credits once at the start of each month and no-ops on subsequent days — this also provides
 * automatic catch-up if the server was down on the 1st.
 *
 * <p>Active only under the {@code r2dbc} profile (i.e. the running backend, not unit tests), and
 * can be disabled with {@code iwm.hrm.leave-accrual.enabled=false}. The cron is configurable via
 * {@code iwm.hrm.leave-accrual.cron} (default: 02:00 every day).
 */
@Component
@Profile("r2dbc")
public class LeaveAccrualScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaveAccrualScheduler.class);

    private final RunLeaveAccrualUseCase runLeaveAccrualUseCase;
    private final boolean enabled;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public LeaveAccrualScheduler(RunLeaveAccrualUseCase runLeaveAccrualUseCase,
                                 @Value("${iwm.hrm.leave-accrual.enabled:true}") boolean enabled) {
        this.runLeaveAccrualUseCase = runLeaveAccrualUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${iwm.hrm.leave-accrual.cron:0 0 2 * * *}")
    public void accrueMonthly() {
        if (!enabled) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            LOGGER.debug("leave accrual run skipped because a previous run is still in progress");
            return;
        }
        runLeaveAccrualUseCase.runForAllActiveOrganizations()
                .doOnError(error -> LOGGER.error("leave accrual run failed", error))
                .doFinally(signal -> running.set(false))
                .subscribe(credited -> LOGGER.info("leave accrual credited {} balance(s)", credited));
    }
}
