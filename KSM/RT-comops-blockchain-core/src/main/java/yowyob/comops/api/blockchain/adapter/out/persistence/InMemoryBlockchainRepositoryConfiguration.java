package yowyob.comops.api.blockchain.adapter.out.persistence;

import yowyob.comops.api.blockchain.application.port.out.BlockchainBlockRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainTransactionRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainWalletRepository;
import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransactionStatus;
import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@Profile("test-memory")
public class InMemoryBlockchainRepositoryConfiguration {

    @Bean
    BlockchainBlockRepository blockchainBlockRepository() {
        return new InMemoryBlockRepository();
    }

    @Bean
    BlockchainTransactionRepository blockchainTransactionRepository() {
        return new InMemoryTransactionRepository();
    }

    @Bean
    BlockchainWalletRepository blockchainWalletRepository() {
        return new InMemoryWalletRepository();
    }

    private static final class InMemoryBlockRepository implements BlockchainBlockRepository {
        private final Map<UUID, BlockchainBlock> blocks = new ConcurrentHashMap<>();

        @Override
        public Mono<BlockchainBlock> save(BlockchainBlock block) {
            blocks.put(block.id(), block);
            return Mono.just(block);
        }

        @Override
        public Mono<BlockchainBlock> findLatest(UUID tenantId, UUID organizationId, String chainCode) {
            return Flux.fromIterable(blocks.values())
                    .filter(block -> block.tenantId().equals(tenantId)
                            && block.organizationId().equals(organizationId)
                            && block.chainCode().equals(chainCode))
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

    private static final class InMemoryTransactionRepository implements BlockchainTransactionRepository {
        private final Map<UUID, BlockchainTransaction> transactions = new ConcurrentHashMap<>();

        @Override
        public Mono<BlockchainTransaction> save(BlockchainTransaction transaction) {
            transactions.put(transaction.id(), transaction);
            return Mono.just(transaction);
        }

        @Override
        public Flux<BlockchainTransaction> findPending(UUID tenantId, UUID organizationId, String chainCode, int limit) {
            return Flux.fromIterable(transactions.values())
                    .filter(transaction -> transaction.tenantId().equals(tenantId)
                            && transaction.organizationId().equals(organizationId)
                            && transaction.chainCode().equals(chainCode)
                            && transaction.status() == BlockchainTransactionStatus.PENDING)
                    .sort(Comparator.comparing(BlockchainTransaction::createdAt))
                    .take(limit);
        }

        @Override
        public Flux<BlockchainTransaction> findAll(UUID tenantId, UUID organizationId, String chainCode) {
            return Flux.fromIterable(transactions.values())
                    .filter(transaction -> transaction.tenantId().equals(tenantId)
                            && transaction.organizationId().equals(organizationId)
                            && transaction.chainCode().equals(chainCode))
                    .sort(Comparator.comparing(BlockchainTransaction::createdAt).reversed());
        }

        @Override
        public Flux<BlockchainTransaction> findByBlockId(UUID blockId) {
            return Flux.fromIterable(transactions.values())
                    .filter(transaction -> blockId.equals(transaction.blockId()))
                    .sort(Comparator.comparing(BlockchainTransaction::createdAt));
        }
    }

    private static final class InMemoryWalletRepository implements BlockchainWalletRepository {
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
                            && wallet.organizationId().equals(organizationId)
                            && wallet.active())
                    .sort(Comparator.comparing(BlockchainWallet::createdAt).reversed());
        }
    }
}
