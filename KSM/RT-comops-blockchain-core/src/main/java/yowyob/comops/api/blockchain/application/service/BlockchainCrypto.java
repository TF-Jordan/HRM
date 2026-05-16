package yowyob.comops.api.blockchain.application.service;

import yowyob.comops.api.blockchain.domain.BlockchainException;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

public final class BlockchainCrypto {

    private static final String KEY_ALGORITHM = "EC";
    private static final String CURVE = "secp256r1";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    private BlockchainCrypto() {
    }

    public static GeneratedKeyMaterial generateKeyMaterial() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            generator.initialize(new ECGenParameterSpec(CURVE), SecureRandom.getInstanceStrong());
            KeyPair pair = generator.generateKeyPair();
            String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
            return new GeneratedKeyMaterial(publicKey, privateKey, fingerprint(publicKey));
        } catch (Exception e) {
            throw new BlockchainException("Unable to generate blockchain wallet keys: " + e.getMessage());
        }
    }

    public static String signingPayload(String chainCode, String transactionType, String sourceService,
            String sourceReference, String payloadHash, String senderPublicKey) {
        return normalize(chainCode) + "|" + normalize(transactionType) + "|" + normalize(sourceService) + "|"
                + normalize(sourceReference) + "|" + normalize(payloadHash) + "|" + normalize(senderPublicKey);
    }

    public static String transactionHash(String signingPayload, String signature) {
        return sha256Hex(signingPayload + "|" + normalize(signature));
    }

    public static String blockHash(long height, String previousHash, String merkleRoot, long nonce, int difficulty,
            int transactionCount, String minedBy) {
        return sha256Hex(height + "|" + normalize(previousHash) + "|" + normalize(merkleRoot) + "|" + nonce + "|"
                + difficulty + "|" + transactionCount + "|" + normalize(minedBy));
    }

    public static String sign(String privateKeyBase64, String payload) {
        try {
            PrivateKey privateKey = decodePrivateKey(privateKeyBase64);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new BlockchainException("Unable to sign blockchain payload: " + e.getMessage());
        }
    }

    public static boolean verify(String publicKeyBase64, String payload, String signatureBase64) {
        try {
            PublicKey publicKey = decodePublicKey(publicKeyBase64);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            return false;
        }
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalize(value).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BlockchainException("Unable to compute SHA-256 hash: " + e.getMessage());
        }
    }

    public static String merkleRoot(List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return "0".repeat(64);
        }
        List<String> level = hashes.stream().sorted(Comparator.naturalOrder()).toList();
        while (level.size() > 1) {
            List<String> next = new ArrayList<>();
            for (int i = 0; i < level.size(); i += 2) {
                String left = level.get(i);
                String right = i + 1 < level.size() ? level.get(i + 1) : left;
                next.add(sha256Hex(left + right));
            }
            level = next;
        }
        return level.getFirst();
    }

    public static boolean meetsDifficulty(String hash, int difficulty) {
        return hash != null && hash.startsWith("0".repeat(Math.max(0, difficulty)));
    }

    public static String fingerprint(String publicKeyBase64) {
        return sha256Hex(publicKeyBase64).substring(0, 24);
    }

    private static PublicKey decodePublicKey(String publicKeyBase64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(publicKeyBase64);
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));
    }

    private static PrivateKey decodePrivateKey(String privateKeyBase64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(privateKeyBase64);
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record GeneratedKeyMaterial(String publicKey, String privateKey, String fingerprint) {
    }
}
