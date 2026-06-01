package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.domain.model.AdministrativeRoleTemplate;
import java.util.Set;

public record AdministrativeRoleTemplateResponse(String code, String name, String scopeType, Set<String> permissions,
        boolean protectedTemplate) {

    public static AdministrativeRoleTemplateResponse from(AdministrativeRoleTemplate template) {
        return new AdministrativeRoleTemplateResponse(template.code(), template.name(), template.scopeType(),
                template.permissions(), template.protectedTemplate());
    }
}
