package yowyob.comops.api.blockchain.domain.model;

import java.util.List;

public record ChainValidationReport(
        String chainCode,
        boolean valid,
        long checkedBlocks,
        long checkedTransactions,
        String latestHash,
        List<String> errors) {
}
