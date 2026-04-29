package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record CreateSocialDeclarationCommand(
        UUID organizationId,
        String type,
        String periode,
        String format) {
}
