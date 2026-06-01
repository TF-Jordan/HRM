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
import yowyob.comops.api.hrm.domain.model.LeaveRequest;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class LeaveService implements ManageLeaveUseCase {

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
                            BigDecimal nbJours = calculateBusinessDays(command.dateDebut(), command.dateFin());

                            return leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                            context.tenantId(), command.employeeId(), type, currentYear)
                                    .flatMap(balance -> {
                                        if (balance.soldeRestant().compareTo(nbJours) < 0) {
                                            return Mono.error(new InsufficientLeaveBalanceException(
                                                    nbJours, balance.soldeRestant()));
                                        }
                                        return actorPort.resolveManager(context.tenantId(), employee.actorId())
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
                                                                        "nbJours", nbJours))).thenReturn(saved));
                                    })
                                    .switchIfEmpty(Mono.error(new InsufficientLeaveBalanceException(
                                            nbJours, BigDecimal.ZERO)));
                        }));
    }

    @Override
    public Mono<LeaveRequest> approveLeave(UUID leaveRequestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> leaveRequestRepository.findById(context.tenantId(), leaveRequestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Leave request not found")))
                        .flatMap(request -> {
                            LeaveRequest approved = request.approve(context.userId());
                            return leaveBalanceRepository.findByEmployeeIdAndTypeAndAnnee(
                                            context.tenantId(), request.employeeId(),
                                            request.type(), request.dateDebut().getYear())
                                    .flatMap(balance -> {
                                        var debited = balance.debiter(request.nbJours());
                                        return leaveBalanceRepository.save(debited);
                                    })
                                    .then(leaveRequestRepository.save(approved))
                                    .flatMap(saved -> businessEventPublisher.publish(
                                                    BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                            "LEAVE_APPROVED", "LEAVE_REQUEST", saved.id(),
                                                            payload("employeeId", saved.employeeId(),
                                                                    "type", saved.type().name(),
                                                                    "nbJours", saved.nbJours())))
                                            .then(businessEventPublisher.publish(
                                                    BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                            "LEAVE_BALANCE_UPDATED", "LEAVE_BALANCE", null,
                                                            payload("employeeId", saved.employeeId(),
                                                                    "type", saved.type().name()))))
                                            .thenReturn(saved));
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
                            if (wasApproved) {
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

    static BigDecimal calculateBusinessDays(LocalDate start, LocalDate end) {
        long count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return BigDecimal.valueOf(count);
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
