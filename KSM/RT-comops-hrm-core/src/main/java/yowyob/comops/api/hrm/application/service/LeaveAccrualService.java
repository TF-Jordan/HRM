package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class LeaveAccrualService {

    private static final BigDecimal BASE_MONTHLY_ACCRUAL = new BigDecimal("1.5");
    private static final BigDecimal SENIORITY_BONUS_PER_BRACKET = new BigDecimal("2");
    private static final int SENIORITY_BRACKET_YEARS = 5;
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");
    private static final BigDecimal CHILD_BONUS_PER_CHILD = new BigDecimal("2");
    private static final int MAX_CHILD_BONUS_DAYS = 10;
    private static final int CHILD_AGE_LIMIT = 6;

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final DependentRepository dependentRepository;
    private final BusinessEventPublisher businessEventPublisher;

    public LeaveAccrualService(EmployeeRepository employeeRepository,
                               LeaveBalanceRepository leaveBalanceRepository,
                               DependentRepository dependentRepository,
                               BusinessEventPublisher businessEventPublisher) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.dependentRepository = dependentRepository;
        this.businessEventPublisher = businessEventPublisher;
    }

    public Mono<Integer> runMonthlyAccrual(UUID tenantId, UUID organizationId) {
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        return employeeRepository.findActiveByOrganizationId(tenantId, organizationId)
                .flatMap(employee -> computeAndCreditAccrual(tenantId, organizationId, employee, currentYear, today))
                .collectList()
                .map(java.util.List::size);
    }

    private Mono<LeaveBalance> computeAndCreditAccrual(UUID tenantId, UUID organizationId,
                                                        Employee employee, int currentYear, LocalDate today) {
        long seniorityYears = ChronoUnit.YEARS.between(employee.dateEmbauche(), today);

        return dependentRepository.findByEmployeeId(tenantId, employee.id())
                .filter(dep -> {
                    if (dep.dateNaissance() == null) return false;
                    long age = ChronoUnit.YEARS.between(dep.dateNaissance(), today);
                    return age < CHILD_AGE_LIMIT;
                })
                .count()
                .flatMap(eligibleChildren -> {
                    BigDecimal monthlyAccrual = calculateMonthlyAccrual(seniorityYears, eligibleChildren);

                    return leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                    tenantId, employee.id(), LeaveType.ANNUAL, currentYear)
                            .switchIfEmpty(Mono.defer(() -> leaveBalanceRepository.save(
                                    LeaveBalance.initialize(tenantId, organizationId,
                                            employee.id(), LeaveType.ANNUAL, currentYear))))
                            .flatMap(balance -> {
                                LeaveBalance credited = balance.crediter(monthlyAccrual);
                                return leaveBalanceRepository.save(credited);
                            })
                            .flatMap(saved -> businessEventPublisher.publish(
                                    BusinessEvent.now(tenantId, organizationId,
                                            "LEAVE_BALANCE_UPDATED", "LEAVE_BALANCE", saved.id(),
                                            payload("employeeId", employee.id(),
                                                    "type", "ANNUAL",
                                                    "credited", monthlyAccrual))).thenReturn(saved));
                });
    }

    static BigDecimal calculateMonthlyAccrual(long seniorityYears, long eligibleChildren) {
        BigDecimal monthly = BASE_MONTHLY_ACCRUAL;

        if (seniorityYears >= SENIORITY_BRACKET_YEARS) {
            long brackets = seniorityYears / SENIORITY_BRACKET_YEARS;
            BigDecimal seniorityBonus = SENIORITY_BONUS_PER_BRACKET
                    .multiply(BigDecimal.valueOf(brackets))
                    .divide(MONTHS_PER_YEAR, 3, RoundingMode.HALF_UP);
            monthly = monthly.add(seniorityBonus);
        }

        if (eligibleChildren > 0) {
            BigDecimal childBonusAnnual = CHILD_BONUS_PER_CHILD.multiply(BigDecimal.valueOf(eligibleChildren));
            if (childBonusAnnual.compareTo(BigDecimal.valueOf(MAX_CHILD_BONUS_DAYS)) > 0) {
                childBonusAnnual = BigDecimal.valueOf(MAX_CHILD_BONUS_DAYS);
            }
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
