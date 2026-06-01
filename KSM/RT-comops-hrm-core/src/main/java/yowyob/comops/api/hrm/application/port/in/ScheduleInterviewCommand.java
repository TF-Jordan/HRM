package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.InterviewType;

import java.time.Instant;
import java.util.UUID;

public record ScheduleInterviewCommand(
        UUID applicationId,
        InterviewType type,
        Instant dateHeure,
        String lieu,
        UUID interviewerPartyId,
        String interviewerDisplayName) {
}
