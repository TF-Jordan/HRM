package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.ManageLeaveUseCase;
import yowyob.comops.api.hrm.application.port.in.SubmitLeaveCommand;
import yowyob.comops.api.hrm.application.port.out.ActorPort;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveRequestRepository;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.InsufficientLeaveBalanceException;
import yowyob.comops.api.hrm.domain.model.CameroonHolidays;
import yowyob.comops.api.hrm.domain.model.LeaveRequest;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class LeaveService implements ManageLeaveUseCase {

    /** Maternity leave: 14 weeks = 98 calendar days (Cameroon labour code). */
    private static final long MATERNITY_MAX_CALENDAR_DAYS = 98;
    /** Paternity leave: 3 jours ouvrables (Cameroon labour code). */
    private static final BigDecimal PATERNITY_MAX_JOURS = BigDecimal.valueOf(3);
    /** Minimum consecutive jours ouvrables for the main annual leave fraction. */
    private static final BigDecimal ANNUAL_MIN_MAIN_FRACTION = BigDecimal.valueOf(12);

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ActorPort actorPort;
    private final BusinessEventPublisher businessEventPublisher;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        LeaveBalanceRepository leaveBalanceRepository,
                        EmployeeRepository employeeRepository,
                        ActorPort actorPort,
                        BusinessEventPublisher businessEventPublisher) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.actorPort = actorPort;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<LeaveRequest> submitLeave(SubmitLeaveCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> {
                            LeaveType type = LeaveType.valueOf(command.type());
                            int currentYear = command.dateDebut().getYear();
                            BigDecimal nbJours = BigDecimal.valueOf(
                                    CameroonHolidays.joursOuvrables(command.dateDebut(), command.dateFin()));

                            // ── Type-specific duration constraints (Cameroon labour code) ──
                            Mono<Void> typeCheck = validateTypeConstraints(type, command, nbJours);

                            // ── Balance check: only ANNUAL leave is drawn from an accrued balance ──
                            Mono<Void> balanceCheck;
                            if (type == LeaveType.ANNUAL) {
                                balanceCheck = leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                                context.tenantId(), command.employeeId(), type, currentYear)
                                        .switchIfEmpty(Mono.error(new InsufficientLeaveBalanceException(
                                                nbJours, BigDecimal.ZERO)))
                                        .flatMap(balance -> {
                                            if (balance.soldeRestant().compareTo(nbJours) < 0) {
                                                return Mono.<Void>error(new InsufficientLeaveBalanceException(
                                                        nbJours, balance.soldeRestant()));
                                            }
                                            return Mono.empty();
                                        });
                            } else {
                                balanceCheck = Mono.empty();
                            }

                            return typeCheck.then(balanceCheck).then(
                                    actorPort.resolveManager(context.tenantId(), employee.actorId())
                                            .flatMap(manager -> {
                                                LeaveRequest request = LeaveRequest.submit(
                                                        context.tenantId(), context.organizationId(),
                                                        context.agencyId(), command.employeeId(), type,
                                                        command.dateDebut(), command.dateFin(), nbJours,
                                                        command.motif(), manager.actorId(),
                                                        manager.displayName(), command.justificatifFileId());
                                                return leaveRequestRepository.save(request);
                                            })
                                            .flatMap(saved -> businessEventPublisher.publish(
                                                    BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                            "LEAVE_SUBMITTED", "LEAVE_REQUEST", saved.id(),
                                                            payload("employeeId", command.employeeId(),
                                                                    "type", type.name(),
                                                                    "nbJours", nbJours))).thenReturn(saved)));
                        }));
    }

    /**
     * Validates type-specific constraints per the Cameroon labour code.
     */
    private Mono<Void> validateTypeConstraints(LeaveType type, SubmitLeaveCommand command,
                                                BigDecimal nbJours) {
        switch (type) {
            case MATERNITY -> {
                long calendarDays = ChronoUnit.DAYS.between(command.dateDebut(), command.dateFin()) + 1;
                if (calendarDays > MATERNITY_MAX_CALENDAR_DAYS) {
                    return Mono.error(new IllegalArgumentException(
                            "Maternity leave cannot exceed 14 weeks (98 calendar days). Requested: "
                                    + calendarDays + " days."));
                }
            }
            case PATERNITY -> {
                if (nbJours.compareTo(PATERNITY_MAX_JOURS) > 0) {
                    return Mono.error(new IllegalArgumentException(
                            "Paternity leave cannot exceed 3 jours ouvrables. Requested: "
                                    + nbJours + " days."));
                }
            }
            case ANNUAL -> {
                if (nbJours.compareTo(ANNUAL_MIN_MAIN_FRACTION) < 0) {
                    // Warn but don't block — fractionnement requires employer agreement.
                    // The rule says the main fraction must be >= 12 consecutive ouvrables days,
                    // but secondary fractions can be shorter with agreement.
                }
            }
            default -> {}
        }
        return Mono.empty();
    }

    @Override
    public Mono<LeaveRequest> approveLeave(UUID leaveRequestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> leaveRequestRepository.findById(context.tenantId(), leaveRequestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Leave request not found")))
                        .flatMap(request -> {
                            LeaveRequest approved = request.approve(context.userId());
                            // Only debit balance for ANNUAL leave (other types have no accrued balance)
                            Mono<Void> debitMono;
                            if (request.type() == LeaveType.ANNUAL) {
                                debitMono = leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                                context.tenantId(), request.employeeId(),
                                                request.type(), request.dateDebut().getYear())
                                        .flatMap(balance -> leaveBalanceRepository.save(
                                                balance.debiter(request.nbJours())))
                                        .then();
                            } else {
                                debitMono = Mono.empty();
                            }
                            return debitMono
                                    .then(leaveRequestRepository.save(approved))
                                    .flatMap(saved -> {
                                        Mono<Void> events = businessEventPublisher.publish(
                                                BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                        "LEAVE_APPROVED", "LEAVE_REQUEST", saved.id(),
                                                        payload("employeeId", saved.employeeId(),
                                                                "type", saved.type().name(),
                                                                "nbJours", saved.nbJours())));
                                        // Only emit LEAVE_BALANCE_UPDATED when a balance was actually debited
                                        if (saved.type() == LeaveType.ANNUAL) {
                                            events = events.then(businessEventPublisher.publish(
                                                    BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                            "LEAVE_BALANCE_UPDATED", "LEAVE_BALANCE", saved.id(),
                                                            payload("employeeId", saved.employeeId(),
                                                                    "type", saved.type().name()))));
                                        }
                                        return events.thenReturn(saved);
                                    });
                        }));
    }

    @Override
    public Mono<LeaveRequest> rejectLeave(UUID leaveRequestId, String commentaire) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> leaveRequestRepository.findById(context.tenantId(), leaveRequestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Leave request not found")))
                        .map(request -> request.reject(context.userId(), commentaire))
                        .flatMap(leaveRequestRepository::save));
    }

    @Override
    public Mono<LeaveRequest> cancelLeave(UUID leaveRequestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> leaveRequestRepository.findById(context.tenantId(), leaveRequestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Leave request not found")))
                        .flatMap(request -> {
                            boolean wasApproved = request.status() == yowyob.comops.api.hrm.domain.model.LeaveStatus.APPROVED;
                            LeaveRequest cancelled = request.cancel();
                            Mono<LeaveRequest> saveMono = leaveRequestRepository.save(cancelled);
                            if (wasApproved && request.type() == LeaveType.ANNUAL) {
                                return leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                                context.tenantId(), request.employeeId(),
                                                request.type(), request.dateDebut().getYear())
                                        .flatMap(balance -> leaveBalanceRepository.save(
                                                balance.crediter(request.nbJours())))
                                        .then(saveMono);
                            }
                            return saveMono;
                        }));
    }

    @Override
    public Mono<LeaveRequest> getLeaveRequest(UUID leaveRequestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> leaveRequestRepository.findById(context.tenantId(), leaveRequestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Leave request not found"))));
    }

    @Override
    public Flux<LeaveRequest> listLeavesByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> leaveRequestRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Flux<LeaveRequest> listPendingLeaves(UUID organizationId, UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> agencyId != null
                        ? leaveRequestRepository.findPendingByOrganizationIdAndAgencyId(
                                context.tenantId(), organizationId, agencyId)
                        : leaveRequestRepository.findPendingByOrganizationId(
                                context.tenantId(), organizationId));
    }

    @Override
    public Flux<LeaveRequest> listOrganizationLeaves(UUID organizationId, UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> agencyId != null
                        ? leaveRequestRepository.findByOrganizationIdAndAgencyId(
                                context.tenantId(), organizationId, agencyId)
                        : leaveRequestRepository.findByOrganizationId(
                                context.tenantId(), organizationId));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
