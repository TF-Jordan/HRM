package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.OrganizationServicePackEntry;
import java.util.List;

public record OrganizationServicePackResponse(
        String code,
        String name,
        String description,
        List<String> serviceCodes) {

    public static OrganizationServicePackResponse from(OrganizationServicePackEntry entry) {
        return new OrganizationServicePackResponse(entry.code(), entry.name(), entry.description(), entry.serviceCodes());
    }
}
