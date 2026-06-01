package yowyob.comops.api.blockchain.application.port.in;

import java.util.UUID;

public record CreateWalletCommand(UUID organizationId, String label) {
}
