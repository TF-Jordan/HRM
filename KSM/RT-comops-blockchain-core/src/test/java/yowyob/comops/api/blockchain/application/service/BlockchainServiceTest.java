package yowyob.comops.api.blockchain.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.blockchain.application.port.in.CreateBlockchainTransactionCommand;
import yowyob.comops.api.blockchain.application.port.in.CreateWalletCommand;
import yowyob.comops.api.blockchain.application.port.out.BlockchainBlockRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainTransactionRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainWalletRepository;
import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransactionStatus;
import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;
import yowyob.comops.api.blockchain.domain.model.GeneratedWallet;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BlockchainServiceTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();
    private final TenantContext context = new TenantContext(tenantId, organizationId, null, UUID.randomUUID(), null);
    private final BlockchainBlockRepository blockRepository = new BlockRepo();
    private final BlockchainTransactionRepository transactionRepository = new TransactionRepo();
    private final BlockchainWalletRepository walletRepository = new WalletRepo();
    private final BlockchainService service = new BlockchainService(blockRepository, transactionRepository,
            walletRepository);

    @Test
    void signsMinesAndValidatesARealBlock() {
        GeneratedWallet wallet = service.createWallet(new CreateWalletCommand(organizationId, "Treasury signer"))
                .contextWrite(ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, context))
                .block();

        assertThat(wallet).isNotNull();
        String payload = "{\"invoice\":\"FAC-001\",\"amount\":12500}";
        String payloadHash = BlockchainCrypto.sha256Hex(payload);
        String signingPayload = BlockchainCrypto.signingPayload("COMOPS_MAIN", "INVOICE_SETTLEMENT", "BILLING",
                "FAC-001", payloadHash, wallet.wallet().publicKey());
        String signature = BlockchainCrypto.sign(wallet.privateKey(), signingPayload);

        CreateBlockchainTransactionCommand command = new CreateBlockchainTransactionCommand(organizationId,
                "COMOPS_MAIN", "INVOICE_SETTLEMENT", "BILLING", "FAC-001", payload, payloadHash,
                wallet.wallet().publicKey(), signature);

        StepVerifier.create(service.submitTransaction(command)
                        .contextWrite(ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, context)))
                .assertNext(transaction -> {
                    assertThat(transaction.status()).isEqualTo(BlockchainTransactionStatus.PENDING);
                    assertThat(transaction.transactionHash()).hasSize(64);
                })
                .verifyComplete();

        StepVerifier.create(service.minePendingBlock(organizationId, "COMOPS_MAIN", "test-miner", 1)
                        .contextWrite(ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, context)))
                .assertNext(block -> {
                    assertThat(block.height()).isEqualTo(1);
                    assertThat(block.blockHash()).startsWith("0");
                    assertThat(block.transactionCount()).isEqualTo(1);
                })
                .verifyComplete();

        StepVerifier.create(service.validateChain(organizationId, "COMOPS_MAIN")
                        .contextWrite(ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, context)))
                .assertNext(report -> {
                    assertThat(report.valid()).isTrue();
                    assertThat(report.checkedBlocks()).isEqualTo(2);
                    assertThat(report.checkedTransactions()).isEqualTo(1);
                })
                .verifyComplete();
    }

    private static final class BlockRepo implements BlockchainBlockRepository {
        private final Map<UUID, BlockchainBlock> blocks = new ConcurrentHashMap<>();

        @Override
        public Mono<BlockchainBlock> save(BlockchainBlock block) {
            blocks.put(block.id(), block);
            return Mono.just(block);
        }

        @Override
        public Mono<BlockchainBlock> findLatest(UUID tenantId, UUID organizationId, String chainCode) {
            return findAll(tenantId, organizationId, chainCode)
                    .sort(Comparator.comparingLong(BlockchainBlock::height).reversed())
                    .next();
        }

        @Override
        public Flux<BlockchainBlock> findAll(UUID tenantId, UUID organizationId, String chainCode) {
            return Flux.fromIterable(blocks.values())
                    .filter(block -> block.tenantId().equals(tenantId)
                            && block.organizationId().equals(organizationId)
                            && block.chainCode().equals(chainCode))
                    .sort(Comparator.comparingLong(BlockchainBlock::height));
        }
    }

    private static final class TransactionRepo implements BlockchainTransactionRepository {
        private final Map<UUID, BlockchainTransaction> transactions = new ConcurrentHashMap<>();

        @Override
        public Mono<BlockchainTransaction> save(BlockchainTransaction transaction) {
            transactions.put(transaction.id(), transaction);
            return Mono.just(transaction);
        }

        @Override
        public Flux<BlockchainTransaction> findPending(UUID tenantId, UUID organizationId, String chainCode, int limit) {
            return findAll(tenantId, organizationId, chainCode)
                    .filter(transaction -> transaction.status() == BlockchainTransactionStatus.PENDING)
                    .sort(Comparator.comparing(BlockchainTransaction::createdAt))
                    .take(limit);
        }

        @Override
        public Flux<BlockchainTransaction> findAll(UUID tenantId, UUID organizationId, String chainCode) {
            return Flux.fromIterable(transactions.values())
                    .filter(transaction -> transaction.tenantId().equals(tenantId)
                            && transaction.organizationId().equals(organizationId)
                            && transaction.chainCode().equals(chainCode));
        }

        @Override
        public Flux<BlockchainTransaction> findByBlockId(UUID blockId) {
            return Flux.fromIterable(transactions.values())
                    .filter(transaction -> blockId.equals(transaction.blockId()));
        }
    }

    private static final class WalletRepo implements BlockchainWalletRepository {
        private final Map<UUID, BlockchainWallet> wallets = new ConcurrentHashMap<>();

        @Override
        public Mono<BlockchainWallet> save(BlockchainWallet wallet) {
            wallets.put(wallet.id(), wallet);
            return Mono.just(wallet);
        }

        @Override
        public Flux<BlockchainWallet> findAll(UUID tenantId, UUID organizationId) {
            return Flux.fromIterable(wallets.values())
                    .filter(wallet -> wallet.tenantId().equals(tenantId)
                            && wallet.organizationId().equals(organizationId));
        }
    }
}
