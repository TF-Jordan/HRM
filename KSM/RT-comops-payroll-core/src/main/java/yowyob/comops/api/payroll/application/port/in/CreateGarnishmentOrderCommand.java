package yowyob.comops.api.payroll.application.port.in;

import yowyob.comops.api.payroll.domain.model.GarnishmentType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to register a garnishment order against an employee. Tenant comes from the context.
 *
 * @param organizationId owning organization
 * @param employeeId     employee whose wages are garnished
 * @param type           garnishment type (sets legal priority)
 * @param beneficiary    who receives the withheld amounts
 * @param reference      external reference (court order number, etc.)
 * @param totalAmount    total debt to recover
 * @param monthlyAmount  installment to withhold each period
 */
public record CreateGarnishmentOrderCommand(
        UUID organizationId,
        UUID employeeId,
        GarnishmentType type,
        String beneficiary,
        String reference,
        BigDecimal totalAmount,
        BigDecimal monthlyAmount) {
}
