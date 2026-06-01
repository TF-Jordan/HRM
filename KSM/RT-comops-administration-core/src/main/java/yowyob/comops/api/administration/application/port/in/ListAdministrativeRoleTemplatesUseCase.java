package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.administration.domain.model.AdministrativeRoleTemplate;
import reactor.core.publisher.Flux;

public interface ListAdministrativeRoleTemplatesUseCase {

    Flux<AdministrativeRoleTemplate> listRoleTemplates();
}
