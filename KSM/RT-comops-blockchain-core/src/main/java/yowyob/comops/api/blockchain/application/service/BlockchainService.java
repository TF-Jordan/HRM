package yowyob.comops.api.blockchain.application.service;

import yowyob.comops.api.blockchain.application.port.in.AnchorDocumentCommand;
import yowyob.comops.api.blockchain.application.port.in.CreateBlockchainTransactionCommand;
import yowyob.comops.api.blockchain.application.port.in.CreateWalletCommand;
import yowyob.comops.api.blockchain.application.port.in.ManageBlockchainUseCase;
import yowyob.comops.api.blockchain.application.port.in.SignPayloadCommand;
import yowyob.comops.api.blockchain.application.port.out.BlockchainBlockRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainTransactionRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainWalletRepository;
import yowyob.comops.api.blockchain.domain.BlockchainException;
import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransactionStatus;
import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;
import yowyob.comops.api.blockchain.domain.model.ChainValidationReport;
import yowyob.comops.api.blockchain.domain.model.GeneratedWallet;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BlockchainService implements ManageBlockchainUseCase {

    private static final int DEFAULT_DIFFICULTY = 3;
    private static final int MAX_DIFFICULTY = 5;
    private static final int BLOCK_TRANSACTION_LIMIT = 250;

    private final BlockchainBlockRepository blockRepository;
    private final BlockchainTransactionRepository transactionRepository;
    private final BlockchainWalletRepository walletRepository;

    public BlockchainService(BlockchainBlockRepository blockRepository,
            BlockchainTransactionRepository transactionRepository,
            BlockchainWalletRepository walletRepository) {
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    public Mono<GeneratedWallet> createWallet(CreateWalletCommand command) {
        return context().flatMap(context -> {
            UUID organizationId = requireOrganization(command.organizationId(), context);
            BlockchainCrypto.GeneratedKeyMaterial keys = BlockchainCrypto.generateKeyMaterial();
            BlockchainWallet wallet = BlockchainWallet.create(context.tenantId(), organizationId, command.label(),
                    keys.publicKey(), keys.fingerprint());
            return walletRepository.save(wallet).map(saved -> new GeneratedWallet(saved, keys.privateKey()));
        });
    }

    @Override
    public Mono<String> buildSigningPayload(CreateBlockchainTransactionCommand command) {
        return Mono.fromSupplier(() -> signingPayload(command));
    }

    @Override
    public Mono<String> signPayload(SignPayloadCommand command) {
        return Mono.fromSupplier(() -> BlockchainCrypto.sign(command.privateKey(), command.payload()));
    }

    @Override
    public Mono<BlockchainTransaction> submitTransaction(CreateBlockchainTransactionCommand command) {
        return context().flatMap(context -> {
            UUID organizationId = requireOrganization(command.organizationId(), context);
            String payloadHash = payloadHash(command.payload(), command.payloadHash());
            String signingPayload = BlockchainCrypto.signingPayload(command.chainCode(), command.transactionType(),
                    command.sourceService(), command.sourceReference(), payloadHash, command.senderPublicKey());
            if (!BlockchainCrypto.verify(command.senderPublicKey(), signingPayload, command.signature())) {
                return Mono.error(new BlockchainException("Invalid blockchain transaction signature."));
            }
            String transactionHash = BlockchainCrypto.transactionHash(signingPayload, command.signature());
            BlockchainTransaction transaction = BlockchainTransaction.pending(context.tenantId(), organizationId,
                    command.chainCode(), command.transactionType(), command.sourceService(), command.sourceReference(),
                    command.payload(), payloadHash, command.senderPublicKey(), command.signature(), transactionHash);
            return transactionRepository.save(transaction);
        });
    }

    @Override
    public Mono<BlockchainTransaction> anchorDocument(AnchorDocumentCommand command) {
        return context().flatMap(context -> {
            UUID organizationId = requireOrganization(command.organizationId(), context);
            String payloadHash = requireText(command.documentHash(), "documentHash");
            String sender = "SYSTEM_ANCHOR";
            String signingPayload = BlockchainCrypto.signingPayload(command.chainCode(), "DOCUMENT_ANCHOR",
                    command.sourceService(), command.sourceReference(), payloadHash, sender);
            String signature = BlockchainCrypto.sha256Hex(signingPayload);
            String transactionHash = BlockchainCrypto.transactionHash(signingPayload, signature);
            BlockchainTransaction transaction = BlockchainTransaction.pending(context.tenantId(), organizationId,
                    command.chainCode(), "DOCUMENT_ANCHOR", command.sourceService(), command.sourceReference(),
                    command.metadata(), payloadHash, sender, signature, transactionHash);
            return transactionRepository.save(transaction);
        });
    }

    @Override
    public Mono<BlockchainBlock> minePendingBlock(UUID organizationId, String chainCode, String minedBy, int difficulty) {
        return context().flatMap(context -> {
            UUID resolvedOrganizationId = requireOrganization(organizationId, context);
            String normalizedChain = BlockchainTransaction.normalizeChain(chainCode);
            int resolvedDifficulty = normalizeDifficulty(difficulty);
            return ensureGenesis(context.tenantId(), resolvedOrganizationId, normalizedChain, resolvedDifficulty)
                    .then(blockRepository.findLatest(context.tenantId(), resolvedOrganizationId, normalizedChain))
                    .flatMap(latest -> transactionRepository
                            .findPending(context.tenantId(), resolvedOrganizationId, normalizedChain,
                                    BLOCK_TRANSACTION_LIMIT)
                            .collectList()
                            .flatMap(transactions -> mineAfterLatest(context, resolvedOrganizationId, normalizedChain,
                                    latest, transactions, minedBy, resolvedDifficulty)));
        });
    }

    @Override
    public Mono<ChainValidationReport> validateChain(UUID organizationId, String chainCode) {
        return context().flatMap(context -> {
            UUID resolvedOrganizationId = requireOrganization(organizationId, context);
            String normalizedChain = BlockchainTransaction.normalizeChain(chainCode);
            return blockRepository.findAll(context.tenantId(), resolvedOrganizationId, normalizedChain).collectList()
                    .flatMap(blocks -> validateBlocks(normalizedChain, blocks));
        });
    }

    @Override
    public Flux<BlockchainBlock> listBlocks(UUID organizationId, String chainCode) {
        return context().flatMapMany(context -> blockRepository.findAll(context.tenantId(),
                requireOrganization(organizationId, context), BlockchainTransaction.normalizeChain(chainCode)));
    }

    @Override
    public Flux<BlockchainTransaction> listTransactions(UUID organizationId, String chainCode) {
        return context().flatMapMany(context -> transactionRepository.findAll(context.tenantId(),
                requireOrganization(organizationId, context), BlockchainTransaction.normalizeChain(chainCode)));
    }

    @Override
    public Flux<BlockchainTransaction> listBlockTransactions(UUID blockId) {
        return transactionRepository.findByBlockId(blockId);
    }

    @Override
    public Flux<BlockchainWallet> listWallets(UUID organizationId) {
        return context().flatMapMany(context -> walletRepository.findAll(context.tenantId(),
                requireOrganization(organizationId, context)));
    }

    private Mono<BlockchainBlock> ensureGenesis(UUID tenantId, UUID organizationId, String chainCode, int difficulty) {
        return blockRepository.findLatest(tenantId, organizationId, chainCode)
                .switchIfEmpty(Mono.defer(() -> {
                    String hash = BlockchainCrypto.blockHash(0, BlockchainBlock.GENESIS_PREVIOUS_HASH,
                            BlockchainBlock.GENESIS_PREVIOUS_HASH, 0, difficulty, 0, "SYSTEM");
                    return blockRepository.save(BlockchainBlock.genesis(tenantId, organizationId, chainCode, hash,
                            difficulty, Instant.now()));
                }));
    }

    private Mono<BlockchainBlock> mineAfterLatest(TenantContext context, UUID organizationId, String chainCode,
            BlockchainBlock latest, List<BlockchainTransaction> transactions, String minedBy, int difficulty) {
        if (transactions.isEmpty()) {
            return Mono.error(new BlockchainException("No pending blockchain transactions to mine."));
        }
        List<String> transactionHashes = transactions.stream().map(BlockchainTransaction::transactionHash).toList();
        String merkleRoot = BlockchainCrypto.merkleRoot(transactionHashes);
        String miner = minedBy == null || minedBy.isBlank() ? "SYSTEM" : minedBy.trim();
        long height = latest.height() + 1;
        long nonce = 0;
        String hash;
        do {
            hash = BlockchainCrypto.blockHash(height, latest.blockHash(), merkleRoot, nonce, difficulty,
                    transactions.size(), miner);
            nonce++;
        } while (!BlockchainCrypto.meetsDifficulty(hash, difficulty));
        Instant minedAt = Instant.now();
        BlockchainBlock block = BlockchainBlock.mined(context.tenantId(), organizationId, chainCode, height,
                latest.blockHash(), merkleRoot, hash, nonce - 1, difficulty, transactions.size(), miner, minedAt);
        return blockRepository.save(block)
                .flatMap(saved -> Flux.fromIterable(transactions)
                        .concatMap(transaction -> transactionRepository.save(transaction.mined(saved.id(),
                                saved.height(), minedAt)))
                        .then(Mono.just(saved)));
    }

    private Mono<ChainValidationReport> validateBlocks(String chainCode, List<BlockchainBlock> blocks) {
        List<BlockchainBlock> ordered = blocks.stream()
                .sorted(Comparator.comparingLong(BlockchainBlock::height))
                .toList();
        List<String> errors = new ArrayList<>();
        if (ordered.isEmpty()) {
            return Mono.just(new ChainValidationReport(chainCode, true, 0, 0, null, errors));
        }
        return Flux.fromIterable(ordered)
                .concatMap(block -> transactionRepository.findByBlockId(block.id()).collectList()
                        .map(transactions -> validateBlock(block, transactions, ordered, errors)))
                .then(Mono.fromSupplier(() -> new ChainValidationReport(chainCode, errors.isEmpty(), ordered.size(),
                        ordered.stream().mapToLong(BlockchainBlock::transactionCount).sum(),
                        ordered.getLast().blockHash(), errors)));
    }

    private boolean validateBlock(BlockchainBlock block, List<BlockchainTransaction> transactions,
            List<BlockchainBlock> ordered, List<String> errors) {
        if (block.height() == 0) {
            if (!BlockchainBlock.GENESIS_PREVIOUS_HASH.equals(block.previousHash())) {
                errors.add("Genesis block previous hash is invalid.");
            }
            String recalculatedGenesis = BlockchainCrypto.blockHash(block.height(), block.previousHash(),
                    block.merkleRoot(), block.nonce(), block.difficulty(), block.transactionCount(), block.minedBy());
            if (!recalculatedGenesis.equals(block.blockHash())) {
                errors.add("Genesis block hash does not match its content.");
            }
            return true;
        }
        BlockchainBlock previous = ordered.stream()
                .filter(candidate -> candidate.height() == block.height() - 1)
                .findFirst()
                .orElse(null);
        if (previous == null || !previous.blockHash().equals(block.previousHash())) {
            errors.add("Block " + block.height() + " is not linked to the previous block.");
        }
        String merkleRoot = BlockchainCrypto.merkleRoot(transactions.stream()
                .map(BlockchainTransaction::transactionHash)
                .toList());
        if (!merkleRoot.equals(block.merkleRoot())) {
            errors.add("Block " + block.height() + " has an invalid Merkle root.");
        }
        String recalculated = BlockchainCrypto.blockHash(block.height(), block.previousHash(), block.merkleRoot(),
                block.nonce(), block.difficulty(), block.transactionCount(), block.minedBy());
        if (!recalculated.equals(block.blockHash())) {
            errors.add("Block " + block.height() + " hash does not match its content.");
        }
        if (!BlockchainCrypto.meetsDifficulty(block.blockHash(), block.difficulty())) {
            errors.add("Block " + block.height() + " does not satisfy proof-of-work difficulty.");
        }
        if (transactions.size() != block.transactionCount()) {
            errors.add("Block " + block.height() + " transaction count is inconsistent.");
        }
        transactions.stream()
                .filter(tx -> tx.status() != BlockchainTransactionStatus.MINED)
                .forEach(tx -> errors.add("Transaction " + tx.id() + " is not marked as mined."));
        transactions.forEach(transaction -> validateTransaction(transaction, errors));
        return true;
    }

    private void validateTransaction(BlockchainTransaction transaction, List<String> errors) {
        String signingPayload = BlockchainCrypto.signingPayload(transaction.chainCode(), transaction.transactionType(),
                transaction.sourceService(), transaction.sourceReference(), transaction.payloadHash(),
                transaction.senderPublicKey());
        String recalculatedHash = BlockchainCrypto.transactionHash(signingPayload, transaction.signature());
        if (!recalculatedHash.equals(transaction.transactionHash())) {
            errors.add("Transaction " + transaction.id() + " hash does not match its content.");
        }
        if (isSystemSender(transaction.senderPublicKey())) {
            String expectedSignature = BlockchainCrypto.sha256Hex(signingPayload);
            if (!expectedSignature.equals(transaction.signature())) {
                errors.add("System transaction " + transaction.id() + " has an invalid system signature.");
            }
            return;
        }
        if (!BlockchainCrypto.verify(transaction.senderPublicKey(), signingPayload, transaction.signature())) {
            errors.add("Transaction " + transaction.id() + " has an invalid ECDSA signature.");
        }
    }

    private boolean isSystemSender(String senderPublicKey) {
        return "SYSTEM_ANCHOR".equals(senderPublicKey) || "COMOPS_OUTBOX".equals(senderPublicKey);
    }

    private Mono<TenantContext> context() {
        return ReactiveRequestContextHolder.getRequiredContext();
    }

    private UUID requireOrganization(UUID organizationId, TenantContext context) {
        if (organizationId != null) {
            return organizationId;
        }
        if (context.organizationId() != null) {
            return context.organizationId();
        }
        throw new BlockchainException("organizationId is required.");
    }

    private String signingPayload(CreateBlockchainTransactionCommand command) {
        return BlockchainCrypto.signingPayload(command.chainCode(), command.transactionType(), command.sourceService(),
                command.sourceReference(), payloadHash(command.payload(), command.payloadHash()),
                command.senderPublicKey());
    }

    private String payloadHash(String payload, String providedHash) {
        if (providedHash != null && !providedHash.isBlank()) {
            return providedHash.trim().toLowerCase();
        }
        return BlockchainCrypto.sha256Hex(payload);
    }

    private int normalizeDifficulty(int difficulty) {
        if (difficulty <= 0) {
            return DEFAULT_DIFFICULTY;
        }
        return Math.min(difficulty, MAX_DIFFICULTY);
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BlockchainException(field + " is required.");
        }
        return value.trim();
    }
}
