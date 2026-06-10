package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.ManagePayrollEmployeeUseCase.CsvImportReport;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEmployeeRepository;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the CSV import / upsert and data-source flip with in-memory fakes for the two ports.
 */
class PayrollEmployeeServiceTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID ORG = UUID.randomUUID();

    private InMemoryEmployeeRepo employeeRepo;
    private InMemoryDataSourceRepo dataSourceRepo;
    private PayrollEmployeeService service;

    @BeforeEach
    void setup() {
        employeeRepo = new InMemoryEmployeeRepo();
        dataSourceRepo = new InMemoryDataSourceRepo();
        service = new PayrollEmployeeService(employeeRepo, dataSourceRepo);
    }

    @Test
    void importsRowsCreatesEmployeesAndFlipsSourceToLocal() {
        String csv = String.join("\n",
                "matricule,nom,email,dateEmbauche,salaireBase,situationMatrimoniale,enfants,modePaiement",
                "EMP-001,Jean Mballa,jean@x.cm,2022-01-15,450000,MARRIED,2,BANK_TRANSFER",
                "EMP-002,Awa Ngono,awa@x.cm,2023-06-01,300000,SINGLE,0,MTN_MOBILE_MONEY");

        StepVerifier.create(service.importCsv(ORG, csv).contextWrite(withContext()))
                .assertNext(report -> {
                    assertThat(report.total()).isEqualTo(2);
                    assertThat(report.created()).isEqualTo(2);
                    assertThat(report.updated()).isZero();
                    assertThat(report.errors()).isEmpty();
                })
                .verifyComplete();

        assertThat(employeeRepo.store).hasSize(2);
        PayrollEmployee jean = employeeRepo.store.values().stream()
                .filter(e -> e.matricule().equals("EMP-001")).findFirst().orElseThrow();
        assertThat(jean.maritalStatus()).isEqualTo(MaritalStatus.MARRIED);
        assertThat(jean.dependentChildren()).isEqualTo(2);
        assertThat(jean.paymentChannel()).isEqualTo(PaymentChannel.BANK_TRANSFER);
        assertThat(dataSourceRepo.get(TENANT, ORG).block())
                .isEqualTo(PayrollDataSourceRepository.Source.LOCAL);
    }

    @Test
    void reimportUpdatesExistingByMatricule() {
        String first = "matricule,nom,dateEmbauche,salaireBase\nEMP-001,Jean,2022-01-15,400000";
        service.importCsv(ORG, first).contextWrite(withContext()).block();

        String second = "matricule,nom,dateEmbauche,salaireBase\nEMP-001,Jean Mballa,2022-01-15,500000";
        StepVerifier.create(service.importCsv(ORG, second).contextWrite(withContext()))
                .assertNext(report -> {
                    assertThat(report.created()).isZero();
                    assertThat(report.updated()).isEqualTo(1);
                })
                .verifyComplete();

        assertThat(employeeRepo.store).hasSize(1);
        PayrollEmployee only = employeeRepo.store.values().iterator().next();
        assertThat(only.displayName()).isEqualTo("Jean Mballa");
        assertThat(only.baseSalary()).isEqualByComparingTo("500000");
    }

    @Test
    void reportsPerRowErrorsWithoutAbortingTheImport() {
        String csv = String.join("\n",
                "matricule,nom,dateEmbauche,salaireBase",
                "EMP-001,Good,2022-01-15,400000",
                "EMP-002,BadDate,not-a-date,400000",
                "EMP-003,BadAmount,2022-01-15,abc");

        StepVerifier.create(service.importCsv(ORG, csv).contextWrite(withContext()))
                .assertNext(report -> {
                    assertThat(report.created()).isEqualTo(1);
                    assertThat(report.errors()).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    void rejectsCsvMissingRequiredColumns() {
        String csv = "matricule,nom\nEMP-001,Jean";
        StepVerifier.create(service.importCsv(ORG, csv).contextWrite(withContext()))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException)
                .verify();
    }

    private Function<Context, Context> withContext() {
        TenantContext tc = new TenantContext(TENANT, ORG, null, UUID.randomUUID(), UUID.randomUUID());
        return ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, tc);
    }

    // --- in-memory fakes ---

    private static final class InMemoryEmployeeRepo implements PayrollEmployeeRepository {
        final Map<UUID, PayrollEmployee> store = new HashMap<>();

        @Override
        public Mono<PayrollEmployee> save(PayrollEmployee employee) {
            store.put(employee.id(), employee);
            return Mono.just(employee);
        }

        @Override
        public Mono<PayrollEmployee> findById(UUID tenantId, UUID id) {
            return Mono.justOrEmpty(store.get(id));
        }

        @Override
        public Mono<PayrollEmployee> findByMatricule(UUID tenantId, UUID organizationId, String matricule) {
            return Mono.justOrEmpty(store.values().stream()
                    .filter(e -> e.organizationId().equals(organizationId) && e.matricule().equals(matricule))
                    .findFirst().orElse(null));
        }

        @Override
        public Mono<PayrollEmployee> findByActorId(UUID tenantId, UUID actorId) {
            return Mono.empty();
        }

        @Override
        public Flux<PayrollEmployee> findByOrganization(UUID tenantId, UUID organizationId) {
            return Flux.fromIterable(store.values())
                    .filter(e -> e.organizationId().equals(organizationId));
        }

        @Override
        public Flux<PayrollEmployee> findActiveByOrganization(UUID tenantId, UUID organizationId, UUID agencyId) {
            return findByOrganization(tenantId, organizationId).filter(PayrollEmployee::active);
        }
    }

    private static final class InMemoryDataSourceRepo implements PayrollDataSourceRepository {
        final Map<UUID, Source> store = new HashMap<>();

        @Override
        public Mono<Source> get(UUID tenantId, UUID organizationId) {
            return Mono.just(store.getOrDefault(organizationId, Source.HRM));
        }

        @Override
        public Mono<Void> set(UUID tenantId, UUID organizationId, Source source) {
            store.put(organizationId, source);
            return Mono.empty();
        }
    }
}
