package yowyob.comops.api.organization.domain.model;

public enum OrganizationGovernanceStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED,
    CLOSED;

    public static OrganizationGovernanceStatus from(String value) {
        if (value == null || value.isBlank()) {
            return PENDING_APPROVAL;
        }
        return OrganizationGovernanceStatus.valueOf(value.trim().toUpperCase());
    }
}
