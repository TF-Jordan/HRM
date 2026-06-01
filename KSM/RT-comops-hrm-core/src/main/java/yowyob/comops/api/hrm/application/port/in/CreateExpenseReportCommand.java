package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record CreateExpenseReportCommand(
        UUID employeeId,
        String periode,
        String motif,
        UUID missionOrderId) {
}
