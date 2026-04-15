package yowyob.comops.api.auth.application.port.in;

import java.util.UUID;

public record RegisterUserCommand(
        UUID tenantId,
        UUID actorId,
        String username,
        String email,
        String password,
        String authProvider) {
}
