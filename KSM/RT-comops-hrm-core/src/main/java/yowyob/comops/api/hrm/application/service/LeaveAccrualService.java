package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.RunLeaveAccrualUseCase;
import yowyob.comops.api.hrm.application.port.out.ActorPort;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class LeaveAccrualService implements RunLeaveAccrualUseCase {

    /** Base monthly accrual for adult workers (art. 89 Cameroon labour code). */
    private static final BigDecimal BASE_MONTHLY_ACCRUAL = new BigDecimal("1.5");
    /** Base monthly accrual for minor workers under 18 (art. 89 al. 2). */
    private static final BigDecimal MINOR_MONTHLY_ACCRUAL = new BigDecimal("2.5");
    private static final int MINOR_AGE_LIMIT = 18;

    private static final BigDecimal SENIORITY_BONUS_PER_BRACKET = new BigDecimal("2");
    private static final int SENIORITY_BRACKET_YEARS = 5;
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");

    /** Bonus per eligible child under 6 — no cap per the Cameroon labour code. */
    private static final BigDecimal CHILD_BONUS_PER_CHILD = new BigDecimal("2");
    private static final int CHILD_AGE_LIMIT = 6;

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final DependentRepository dependentRepository;
    private final ActorPort actorPort;
    private final BusinessEventPublisher businessEventPublisher;

    public LeaveAccrualService(EmployeeRepository employeeRepository,
                               LeaveBalanceRepository leaveBalanceRepository,
                               DependentRepository dependentRepository,
                               ActorPort actorPort,
                               BusinessEventPublisher businessEventPublisher) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.dependentRepository = dependentRepository;
        this.actorPort = actorPort;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<Integer> runMonthlyAccrual(UUID tenantId, UUID organizationId) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        String period = YearMonth.from(today).toString(); // YYYY-MM

        return employeeRepository.findActiveByOrganizationId(tenantId, organizationId)
                .flatMap(employee -> computeAndCreditAccrual(tenantId, organizationId, employee,
                        currentYear, period, today))
                .count()
                .map(Long::intValue);
    }

    @Override
    public Mono<Integer> runForAllActiveOrganizations() {
        return employeeRepository.findDistinctActiveOrganizations()
                .concatMap(pair -> runMonthlyAccrual(pair.tenantId(), pair.organizationId())
                        .onErrorReturn(0))
                .reduce(0, Integer::sum);
    }

    @Override
    public Mono<Integer> runForCurrentContext() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> runMonthlyAccrual(ctx.tenantId(), ctx.organizationId()));
    }

    /**
     * Credits one employee's ANNUAL balance for {@code period}, creating the row if missing.
     * Returns empty (contributing nothing to the run count) when the balance has already been
     * accrued for this month — making the whole job idempotent.
     *
     * <p>When creating a balance for a new year, any remaining days from the previous year
     * are carried over (report de solde).
     */
    private Mono<LeaveBalance> computeAndCreditAccrual(UUID tenantId, UUID organizationId,
                                                       Employee employee, int currentYear,
                                                       String period, LocalDate today) {
        long seniorityYears = ChronoUnit.YEARS.between(employee.dateEmbauche(), today);

        return Mono.zip(
                // Count eligible children (under 6 years old)
                dependentRepository.findByEmployeeId(tenantId, employee.id())
                        .filter(dep -> {
                            if (dep.dateNaissance() == null) return false;
                            long age = ChronoUnit.YEARS.between(dep.dateNaissance(), today);
                            return age < CHILD_AGE_LIMIT;
                        })
                        .count(),
                // Resolve actor to check if employee is a minor (< 18)
                actorPort.resolveActor(tenantId, employee.actorId())
                        .map(actor -> actor.birthDate() != null
                                && ChronoUnit.YEARS.between(actor.birthDate(), today) < MINOR_AGE_LIMIT)
                        .defaultIfEmpty(false)
        ).flatMap(tuple -> {
            long eligibleChildren = tuple.getT1();
            boolean isMinor = tuple.getT2();
            BigDecimal monthlyAccrual = calculateMonthlyAccrual(seniorityYears, eligibleChildren, isMinor);

            return leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                            tenantId, employee.id(), LeaveType.ANNUAL, currentYear)
                    .flatMap(balance -> period.equals(balance.lastAccrualPeriod())
                            ? Mono.<LeaveBalance>empty()
                            : creditAccrual(tenantId, organizationId, employee.id(),
                                    balance.accrue(monthlyAccrual, period), monthlyAccrual))
                    // No balance for current year — create one with carry-over from previous year
                    .switchIfEmpty(Mono.defer(() ->
                            leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                            tenantId, employee.id(), LeaveType.ANNUAL, currentYear - 1)
                                    .map(LeaveBalance::soldeRestant)
                                    .defaultIfEmpty(BigDecimal.ZERO)
                                    .flatMap(carryOver -> {
                                        LeaveBalance newBalance = LeaveBalance.initialize(
                                                tenantId, organizationId, employee.id(),
                                                LeaveType.ANNUAL, currentYear);
                                        if (carryOver.compareTo(BigDecimal.ZERO) > 0) {
                                            newBalance = newBalance.crediter(carryOver);
                                        }
                                        return creditAccrual(tenantId, organizationId, employee.id(),
                                                newBalance.accrue(monthlyAccrual, period), monthlyAccrual);
                                    })
                    ));
        });
    }

    private Mono<LeaveBalance> creditAccrual(UUID tenantId, UUID organizationId, UUID employeeId,
                                             LeaveBalance credited, BigDecimal monthlyAccrual) {
        return leaveBalanceRepository.save(credited)
                .flatMap(saved -> businessEventPublisher.publish(
                        BusinessEvent.now(tenantId, organizationId,
                                "LEAVE_BALANCE_UPDATED", "LEAVE_BALANCE", saved.id(),
                                payload("employeeId", employeeId,
                                        "type", "ANNUAL",
                                        "credited", monthlyAccrual))).thenReturn(saved));
    }

    /**
     * Computes the monthly leave accrual per the Cameroon labour code:
     * <ul>
     *   <li>Base: 1.5 j/month (adult) or 2.5 j/month (minor &lt; 18)</li>
     *   <li>Seniority bonus: +2 j/year per 5-year bracket (prorated monthly)</li>
     *   <li>Children bonus: +2 j/year per child under 6 (no cap)</li>
     * </ul>
     */
    static BigDecimal calculateMonthlyAccrual(long seniorityYears, long eligibleChildren, boolean isMinor) {
        BigDecimal monthly = isMinor ? MINOR_MONTHLY_ACCRUAL : BASE_MONTHLY_ACCRUAL;

        if (seniorityYears >= SENIORITY_BRACKET_YEARS) {
            long brackets = seniorityYears / SENIORITY_BRACKET_YEARS;
            BigDecimal seniorityBonus = SENIORITY_BONUS_PER_BRACKET
                    .multiply(BigDecimal.valueOf(brackets))
                    .divide(MONTHS_PER_YEAR, 3, RoundingMode.HALF_UP);
            monthly = monthly.add(seniorityBonus);
        }

        if (eligibleChildren > 0) {
            BigDecimal childBonusAnnual = CHILD_BONUS_PER_CHILD.multiply(BigDecimal.valueOf(eligibleChildren));
            BigDecimal childBonusMonthly = childBonusAnnual.divide(MONTHS_PER_YEAR, 3, RoundingMode.HALF_UP);
            monthly = monthly.add(childBonusMonthly);
        }

        return monthly;
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
