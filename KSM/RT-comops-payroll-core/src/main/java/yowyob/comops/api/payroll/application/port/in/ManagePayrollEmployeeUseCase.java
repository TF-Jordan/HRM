package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository.Source;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Primary inbound port for payroll-owned employee records — the data backbone of the
 * <em>standalone</em> payroll mode (tenants subscribing to payroll without hrm-core).
 */
public interface ManagePayrollEmployeeUseCase {

    Flux<PayrollEmployee> list(UUID organizationId);

    Mono<PayrollEmployee> get(UUID payrollEmployeeId);

    Mono<PayrollEmployee> create(UpsertPayrollEmployeeCommand command);

    Mono<PayrollEmployee> update(UUID payrollEmployeeId, UpsertPayrollEmployeeCommand command);

    Mono<PayrollEmployee> deactivate(UUID payrollEmployeeId, LocalDate departureDate);

    /**
     * Parses {@code csv} (header line + data rows, comma or semicolon separated) and upserts
     * one employee per row, keyed by matricule. The first successful import flips the
     * organization's payroll data source to {@code LOCAL}.
     */
    Mono<CsvImportReport> importCsv(UUID organizationId, String csv);

    Mono<Source> getDataSource(UUID organizationId);

    Mono<Void> setDataSource(UUID organizationId, Source source);

    record UpsertPayrollEmployeeCommand(
            UUID organizationId,
            UUID agencyId,
            String matricule,
            String displayName,
            String email,
            String socialSecurityNo,
            Integer categorie,
            String echelon,
            String departmentCode,
            LocalDate hireDate,
            String maritalStatus,
            Integer dependentChildren,
            BigDecimal baseSalary,
            BigDecimal benefitsInKind,
            String position,
            String paymentChannel,
            String accountRef) {
    }

    record CsvImportReport(int total, int created, int updated, List<CsvRowError> errors) {
    }

    record CsvRowError(int line, String matricule, String message) {
    }
}
