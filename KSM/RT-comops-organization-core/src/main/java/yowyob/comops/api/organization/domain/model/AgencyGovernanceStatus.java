package yowyob.comops.api.organization.domain.model;

public enum AgencyGovernanceStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED;

    public static AgencyGovernanceStatus from(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        return AgencyGovernanceStatus.valueOf(value.trim().toUpperCase());
    }
}
