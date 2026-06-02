package yowyob.comops.api.auth.application.port.out;

import java.util.List;
import java.util.UUID;

public record UserOrganizationAccess(
        UUID organizationId,
        String organizationCode,
        String shortName,
        String longName,
        List<String> services,
        List<String> roleCodes) {

    /**
     * Backwards-compatible constructor for adapters that don't yet wire the
     * role assignments (e.g. legacy/test paths). Emits an empty role list.
     */
    public UserOrganizationAccess(
            UUID organizationId,
            String organizationCode,
            String shortName,
            String longName,
            List<String> services) {
        this(organizationId, organizationCode, shortName, longName, services, List.of());
    }

    public String displayName() {
        return shortName;
    }

    public String legalName() {
        return longName;
    }
}
