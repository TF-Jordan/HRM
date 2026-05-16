package yowyob.comops.api.hrm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import yowyob.comops.api.hrm.application.port.in.CreateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.out.ActorPort;
import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.application.port.out.SettingsPort;
import yowyob.comops.api.hrm.application.port.out.ThirdPartyProfilePort;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.TenantContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EmployeeServiceTest {

    @Test
    void createEmployeeEnsuresThirdPartyEmployeeProfile() {
        UUID tenantId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID thirdPartyId = UUID.randomUUID();

        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ContractRepository contractRepository = mock(ContractRepository.class);
        DependentRepository dependentRepository = mock(DependentRepository.class);
        LeaveBalanceRepository leaveBalanceRepository = mock(LeaveBalanceRepository.class);
        ActorPort actorPort = mock(ActorPort.class);
        SettingsPort settingsPort = mock(SettingsPort.class);
        ThirdPartyProfilePort thirdPartyProfilePort = mock(ThirdPartyProfilePort.class);
        BusinessEventPublisher businessEventPublisher = mock(BusinessEventPublisher.class);

        EmployeeService service = new EmployeeService(employeeRepository, contractRepository, dependentRepository,
                leaveBalanceRepository, actorPort, settingsPort, thirdPartyProfilePort, businessEventPublisher);

        when(actorPort.resolveActor(tenantId, actorId))
                .thenReturn(Mono.just(new ActorPort.ActorInfo(actorId, "Alice Employee")));
        when(employeeRepository.existsByActorIdAndTenantId(actorId, tenantId)).thenReturn(Mono.just(false));
        when(settingsPort.generateMatricule(tenantId, organizationId, agencyId)).thenReturn(Mono.just("EMP-001"));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(thirdPartyProfilePort.ensureEmployeeFinancialProfile(tenantId, organizationId, actorId, "EMP-001",
                "Alice Employee"))
                .thenReturn(Mono.just(new ThirdPartyProfilePort.ThirdPartyProfile(thirdPartyId, "EMP-001", "421100")));
        when(businessEventPublisher.publish(any(BusinessEvent.class))).thenReturn(Mono.empty());

        CreateEmployeeCommand command = new CreateEmployeeCommand(
                actorId,
                "CNPS-001",
                1,
                "A",
                LocalDate.of(2026, 1, 1),
                "OPS",
                "BANK_TRANSFER",
                "CM211000000000000000001",
                null,
                null,
                "CDI",
                LocalDate.of(2026, 1, 1),
                null,
                BigDecimal.valueOf(250000),
                BigDecimal.ZERO,
                3);

        StepVerifier.create(service.createEmployee(command)
                        .contextWrite(context -> ReactiveRequestContextHolder.withTenantContext(context,
                                new TenantContext(tenantId, organizationId, agencyId, UUID.randomUUID(), actorId))))
                .assertNext(employee -> {
                    assertThat(employee.actorId()).isEqualTo(actorId);
                    assertThat(employee.matricule()).isEqualTo("EMP-001");
                    assertThat(employee.actorDisplayName()).isEqualTo("Alice Employee");
                })
                .verifyComplete();

        verify(thirdPartyProfilePort).ensureEmployeeFinancialProfile(tenantId, organizationId, actorId, "EMP-001",
                "Alice Employee");
    }
}
