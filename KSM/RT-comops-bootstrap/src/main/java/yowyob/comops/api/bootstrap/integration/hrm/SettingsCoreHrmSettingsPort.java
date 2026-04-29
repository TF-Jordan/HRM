package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.hrm.application.port.out.SettingsPort;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SettingsCoreHrmSettingsPort implements SettingsPort {

    private static final String DOCUMENT_TYPE_MATRICULE = "HRM_MATRICULE";

    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;

    public SettingsCoreHrmSettingsPort(GenerateDocumentNumberUseCase generateDocumentNumberUseCase) {
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
    }

    @Override
    public Mono<String> generateMatricule(UUID tenantId, UUID organizationId, UUID agencyId) {
        return generateDocumentNumberUseCase.generate(
                new GenerateDocumentNumberCommand(tenantId, organizationId, agencyId, DOCUMENT_TYPE_MATRICULE));
    }
}
