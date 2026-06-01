package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record EnrollTrainingCommand(
        UUID trainingId,
        UUID employeeId) {
}
