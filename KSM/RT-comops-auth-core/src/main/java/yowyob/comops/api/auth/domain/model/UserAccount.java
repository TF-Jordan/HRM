package yowyob.comops.api.auth.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class UserAccount extends BaseEntity {

    private final UUID actorId;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String authProvider;
    private final String status;
    private final String plan;
    private final String onboardingStatus;
    private final int onboardingStep;

    private UserAccount(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID actorId, String username,
            String email, String passwordHash, String authProvider, String status, String plan,
            String onboardingStatus, int onboardingStep) {
        super(id, tenantId, createdAt, updatedAt);
        this.actorId = actorId;
        this.username = requireText(username, "username").toLowerCase(Locale.ROOT);
        this.email = requireText(email, "email").toLowerCase(Locale.ROOT);
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.authProvider = requireText(authProvider, "authProvider");
        this.status = requireText(status, "status").toUpperCase(Locale.ROOT);
        this.plan = requireText(plan, "plan").toUpperCase(Locale.ROOT);
        this.onboardingStatus = requireText(onboardingStatus, "onboardingStatus").toUpperCase(Locale.ROOT);
        if (onboardingStep < 0) {
            throw new IllegalArgumentException("onboardingStep must be greater than or equal to 0");
        }
        this.onboardingStep = onboardingStep;
    }

    public static UserAccount register(UUID tenantId, UUID actorId, String username, String email, String passwordHash,
            String authProvider) {
        Instant now = Instant.now();
        return new UserAccount(UUID.randomUUID(), tenantId, now, now, actorId, username, email, passwordHash,
                authProvider, "ACTIVE", "FREE_TIER", "NOT_STARTED", 0);
    }

    public static UserAccount rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID actorId,
            String username, String email, String passwordHash, String authProvider, String status, String plan,
            String onboardingStatus, int onboardingStep) {
        return new UserAccount(id, tenantId, createdAt, updatedAt, actorId, username, email, passwordHash,
                authProvider, status, plan, onboardingStatus, onboardingStep);
    }

    public UUID actorId() { return actorId; }
    public String username() { return username; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public String authProvider() { return authProvider; }
    public String status() { return status; }
    public String plan() { return plan; }
    public String onboardingStatus() { return onboardingStatus; }
    public int onboardingStep() { return onboardingStep; }

    public UserAccount updatePlan(String plan) {
        return new UserAccount(id(), tenantId(), createdAt(), Instant.now(), actorId, username, email, passwordHash,
                authProvider, status, requireText(plan, "plan"), onboardingStatus, onboardingStep);
    }

    public UserAccount updateOnboarding(int onboardingStep, String onboardingStatus) {
        return new UserAccount(id(), tenantId(), createdAt(), Instant.now(), actorId, username, email, passwordHash,
                authProvider, status, plan,
                onboardingStatus == null || onboardingStatus.isBlank() ? this.onboardingStatus : onboardingStatus,
                onboardingStep);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
