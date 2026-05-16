package yowyob.comops.api.blockchain.application.port.in;

import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;
import yowyob.comops.api.blockchain.domain.model.ChainValidationReport;
import yowyob.comops.api.blockchain.domain.model.GeneratedWallet;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageBlockchainUseCase {

    Mono<GeneratedWallet> createWallet(CreateWalletCommand command);

    Mono<String> buildSigningPayload(CreateBlockchainTransactionCommand command);

    Mono<String> signPayload(SignPayloadCommand command);

    Mono<BlockchainTransaction> submitTransaction(CreateBlockchainTransactionCommand command);

    Mono<BlockchainTransaction> anchorDocument(AnchorDocumentCommand command);

    Mono<BlockchainBlock> minePendingBlock(UUID organizationId, String chainCode, String minedBy, int difficulty);

    Mono<ChainValidationReport> validateChain(UUID organizationId, String chainCode);

    Flux<BlockchainBlock> listBlocks(UUID organizationId, String chainCode);

    Flux<BlockchainTransaction> listTransactions(UUID organizationId, String chainCode);

    Flux<BlockchainTransaction> listBlockTransactions(UUID blockId);

    Flux<BlockchainWallet> listWallets(UUID organizationId);
}
