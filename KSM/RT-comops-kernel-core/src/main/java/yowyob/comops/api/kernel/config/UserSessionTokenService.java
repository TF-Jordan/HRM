package yowyob.comops.api.kernel.config;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserSessionTokenService {

    private final JwtTokenService jwtTokenService;

    public UserSessionTokenService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public String issue(UUID tenantId, UUID userId, UUID actorId) {
        return issue(tenantId, null, null, userId, actorId, Set.of());
    }

    public String issue(UUID tenantId, UUID userId, UUID actorId, Set<String> permissions) {
        return issue(tenantId, null, null, userId, actorId, permissions);
    }

    public String issue(
            UUID tenantId,
            UUID organizationId,
            UUID agencyId,
            UUID userId,
            UUID actorId,
            Set<String> permissions) {
        return jwtTokenService.issueAccessToken(tenantId, organizationId, agencyId, userId, actorId, permissions);
    }

    public Optional<UserSessionTokenClaims> verify(String token) {
        return jwtTokenService.decode(token).map(UserSessionTokenClaims::fromJwtClaims);
    }

    public boolean isJwtEnabled() {
        return true;
    }

    public Duration getAccessTokenTtl() {
        return jwtTokenService.getAccessTokenTtl();
    }
}
