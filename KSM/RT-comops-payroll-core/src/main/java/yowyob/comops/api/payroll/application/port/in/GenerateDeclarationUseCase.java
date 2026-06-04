package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.service.DeclarationDocument;
import yowyob.comops.api.payroll.domain.model.DeclarationType;

import java.util.UUID;

/** Generates a statutory declaration (CNPS / DIPE / IRPP_CAC) from a calculated payroll run. */
public interface GenerateDeclarationUseCase {

    Mono<DeclarationDocument> generate(DeclarationType type, UUID payrollRunId);

    /** Same content, serialised to semicolon-separated CSV. */
    Mono<String> generateCsv(DeclarationType type, UUID payrollRunId);
}
