package yowyob.comops.api.blockchain.application.port.in;

import java.util.UUID;

public record AnchorDocumentCommand(
        UUID organizationId,
        String chainCode,
        String sourceService,
        String sourceReference,
        String documentHash,
        String metadata) {
}
