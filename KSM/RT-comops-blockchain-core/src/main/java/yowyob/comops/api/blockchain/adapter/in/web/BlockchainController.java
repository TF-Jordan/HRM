package yowyob.comops.api.blockchain.adapter.in.web;

import yowyob.comops.api.blockchain.application.port.in.AnchorDocumentCommand;
import yowyob.comops.api.blockchain.application.port.in.CreateBlockchainTransactionCommand;
import yowyob.comops.api.blockchain.application.port.in.CreateWalletCommand;
import yowyob.comops.api.blockchain.application.port.in.ManageBlockchainUseCase;
import yowyob.comops.api.blockchain.application.port.in.SignPayloadCommand;
import yowyob.comops.api.blockchain.application.service.BlockchainCrypto;
import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;
import yowyob.comops.api.blockchain.domain.model.ChainValidationReport;
import yowyob.comops.api.blockchain.domain.model.GeneratedWallet;
import yowyob.comops.api.common.domain.model.ApiResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/blockchain")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class BlockchainController {

    private final ManageBlockchainUseCase blockchain;

    public BlockchainController(ManageBlockchainUseCase blockchain) {
        this.blockchain = blockchain;
    }

    @PostMapping("/wallets")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:wallet:create')")
    public Mono<ResponseEntity<ApiResponse<GeneratedWalletResponse>>> createWallet(
            @RequestBody Mono<CreateWalletRequest> requestMono) {
        return requestMono
                .flatMap(request -> blockchain.createWallet(new CreateWalletCommand(request.organizationId(),
                        request.label())))
                .map(GeneratedWalletResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Blockchain wallet generated.")));
    }

    @GetMapping("/wallets")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:wallet:read')")
    public Mono<ResponseEntity<ApiResponse<List<WalletResponse>>>> listWallets(@RequestParam UUID organizationId) {
        return blockchain.listWallets(organizationId)
                .map(WalletResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Blockchain wallets fetched.")));
    }

    @PostMapping("/transactions/signing-payload")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:transaction:sign')")
    public Mono<ResponseEntity<ApiResponse<SigningPayloadResponse>>> signingPayload(
            @RequestBody Mono<TransactionRequest> requestMono) {
        return requestMono
                .flatMap(request -> blockchain.buildSigningPayload(request.toCommand()))
                .map(payload -> ResponseEntity.ok(ApiResponse.success(new SigningPayloadResponse(payload),
                        "Signing payload generated.")));
    }

    @PostMapping("/crypto/sign")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:transaction:sign')")
    public Mono<ResponseEntity<ApiResponse<SignatureResponse>>> signPayload(@RequestBody Mono<SignPayloadRequest> requestMono) {
        return requestMono
                .flatMap(request -> blockchain.signPayload(new SignPayloadCommand(request.privateKey(), request.payload())))
                .map(signature -> ResponseEntity.ok(ApiResponse.success(new SignatureResponse(signature),
                        "Payload signed.")));
    }

    @PostMapping("/transactions")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:transaction:create')")
    public Mono<ResponseEntity<ApiResponse<TransactionResponse>>> submitTransaction(
            @RequestBody Mono<TransactionRequest> requestMono) {
        return requestMono
                .flatMap(request -> blockchain.submitTransaction(request.toCommand()))
                .map(TransactionResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Blockchain transaction submitted.")));
    }

    @PostMapping("/anchors")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:anchor:create')")
    public Mono<ResponseEntity<ApiResponse<TransactionResponse>>> anchorDocument(
            @RequestBody Mono<AnchorDocumentRequest> requestMono) {
        return requestMono
                .flatMap(request -> blockchain.anchorDocument(new AnchorDocumentCommand(request.organizationId(),
                        request.chainCode(), request.sourceService(), request.sourceReference(), request.documentHash(),
                        request.metadata())))
                .map(TransactionResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Document anchored on blockchain.")));
    }

    @PostMapping("/mine")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:block:mine')")
    public Mono<ResponseEntity<ApiResponse<BlockResponse>>> mine(@RequestBody Mono<MineBlockRequest> requestMono) {
        return requestMono
                .flatMap(request -> blockchain.minePendingBlock(request.organizationId(), request.chainCode(),
                        request.minedBy(), request.difficulty()))
                .map(BlockResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Blockchain block mined.")));
    }

    @GetMapping("/blocks")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:block:read')")
    public Mono<ResponseEntity<ApiResponse<List<BlockResponse>>>> listBlocks(
            @RequestParam UUID organizationId,
            @RequestParam(defaultValue = "COMOPS_MAIN") String chainCode) {
        return blockchain.listBlocks(organizationId, chainCode)
                .map(BlockResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Blockchain blocks fetched.")));
    }

    @GetMapping("/blocks/{blockId}/transactions")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:transaction:read')")
    public Mono<ResponseEntity<ApiResponse<List<TransactionResponse>>>> listBlockTransactions(@PathVariable UUID blockId) {
        return blockchain.listBlockTransactions(blockId)
                .map(TransactionResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Block transactions fetched.")));
    }

    @GetMapping("/transactions")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:transaction:read')")
    public Mono<ResponseEntity<ApiResponse<List<TransactionResponse>>>> listTransactions(
            @RequestParam UUID organizationId,
            @RequestParam(defaultValue = "COMOPS_MAIN") String chainCode) {
        return blockchain.listTransactions(organizationId, chainCode)
                .map(TransactionResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Blockchain transactions fetched.")));
    }

    @GetMapping("/validate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'blockchain:chain:validate')")
    public Mono<ResponseEntity<ApiResponse<ChainValidationReport>>> validate(
            @RequestParam UUID organizationId,
            @RequestParam(defaultValue = "COMOPS_MAIN") String chainCode) {
        return blockchain.validateChain(organizationId, chainCode)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Blockchain validation completed.")));
    }

    public record CreateWalletRequest(UUID organizationId, String label) {
    }

    public record TransactionRequest(UUID organizationId, String chainCode, String transactionType,
            String sourceService, String sourceReference, String payload, String payloadHash, String senderPublicKey,
            String signature) {
        CreateBlockchainTransactionCommand toCommand() {
            return new CreateBlockchainTransactionCommand(organizationId, chainCode, transactionType, sourceService,
                    sourceReference, payload, payloadHash, senderPublicKey, signature);
        }
    }

    public record SignPayloadRequest(String privateKey, String payload) {
    }

    public record AnchorDocumentRequest(UUID organizationId, String chainCode, String sourceService,
            String sourceReference, String documentHash, String metadata) {
    }

    public record MineBlockRequest(UUID organizationId, String chainCode, String minedBy, int difficulty) {
    }

    public record SigningPayloadResponse(String payload) {
    }

    public record SignatureResponse(String signature) {
    }

    public record GeneratedWalletResponse(UUID id, UUID organizationId, String label, String publicKey,
            String privateKey, String fingerprint, Instant createdAt) {
        static GeneratedWalletResponse from(GeneratedWallet generated) {
            BlockchainWallet wallet = generated.wallet();
            return new GeneratedWalletResponse(wallet.id(), wallet.organizationId(), wallet.label(),
                    wallet.publicKey(), generated.privateKey(), wallet.fingerprint(), wallet.createdAt());
        }
    }

    public record WalletResponse(UUID id, UUID organizationId, String label, String publicKey, String fingerprint,
            Instant createdAt, boolean active) {
        static WalletResponse from(BlockchainWallet wallet) {
            return new WalletResponse(wallet.id(), wallet.organizationId(), wallet.label(), wallet.publicKey(),
                    wallet.fingerprint(), wallet.createdAt(), wallet.active());
        }
    }

    public record TransactionResponse(UUID id, UUID organizationId, String chainCode, String transactionType,
            String sourceService, String sourceReference, String payloadHash, String senderFingerprint,
            String transactionHash, String status, UUID blockId, Long blockHeight, Instant createdAt, Instant minedAt) {
        static TransactionResponse from(BlockchainTransaction transaction) {
            String fingerprint = isSystemSender(transaction.senderPublicKey()) ? transaction.senderPublicKey()
                    : BlockchainCrypto.fingerprint(transaction.senderPublicKey());
            return new TransactionResponse(transaction.id(), transaction.organizationId(), transaction.chainCode(),
                    transaction.transactionType(), transaction.sourceService(), transaction.sourceReference(),
                    transaction.payloadHash(), fingerprint, transaction.transactionHash(), transaction.status().name(),
                    transaction.blockId(), transaction.blockHeight(), transaction.createdAt(), transaction.minedAt());
        }

        private static boolean isSystemSender(String senderPublicKey) {
            return "SYSTEM_ANCHOR".equals(senderPublicKey) || "COMOPS_OUTBOX".equals(senderPublicKey);
        }
    }

    public record BlockResponse(UUID id, UUID organizationId, String chainCode, long height, String previousHash,
            String merkleRoot, String blockHash, long nonce, int difficulty, int transactionCount, String minedBy,
            Instant minedAt) {
        static BlockResponse from(BlockchainBlock block) {
            return new BlockResponse(block.id(), block.organizationId(), block.chainCode(), block.height(),
                    block.previousHash(), block.merkleRoot(), block.blockHash(), block.nonce(), block.difficulty(),
                    block.transactionCount(), block.minedBy(), block.minedAt());
        }
    }
}
