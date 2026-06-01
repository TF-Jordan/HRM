package yowyob.comops.api.organization.application.port.in;

import java.util.List;

public record OrganizationServicePackEntry(
        String code,
        String name,
        String description,
        List<String> serviceCodes) {
}
