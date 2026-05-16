package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_payroll_entry")
public record PayrollEntryEntity(
        @Id UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
        UUID organizationId, UUID payrollRunId, UUID employeeId,
        BigDecimal salaireBase, BigDecimal brut, BigDecimal net,
        BigDecimal cnpsEmploye, BigDecimal cnpsEmployeur, BigDecimal irpp, BigDecimal cac,
        BigDecimal primes, BigDecimal retenues, BigDecimal avancesDeduites,
        String paymentStatus, String paymentChannel, String accountRef) implements PersistableEntity {
}
