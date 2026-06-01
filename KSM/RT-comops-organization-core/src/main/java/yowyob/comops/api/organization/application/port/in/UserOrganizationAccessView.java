package yowyob.comops.api.organization.application.port.in;

import java.util.List;
import java.util.UUID;

public record UserOrganizationAccessView(
        UUID organizationId,
        String organizationCode,
        String shortName,
        String longName,
        List<String> services) {

    public String displayName() {
        return shortName;
    }

    public String legalName() {
        return longName;
    }
}
