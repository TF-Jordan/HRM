package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;

public record TerminateEmployeeCommand(LocalDate terminationDate, String reason) {
}
