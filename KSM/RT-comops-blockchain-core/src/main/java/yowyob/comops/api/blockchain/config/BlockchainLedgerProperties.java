package yowyob.comops.api.blockchain.config;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iwm.blockchain.ledger")
public class BlockchainLedgerProperties {

    private boolean enabled = true;
    private boolean autoMine = true;
    private String chainCode = "COMOPS_GLOBAL";
    private String miner = "comops-outbox-ledger";
    private int difficulty = 1;
    private UUID systemOrganizationId = new UUID(0L, 0L);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoMine() {
        return autoMine;
    }

    public void setAutoMine(boolean autoMine) {
        this.autoMine = autoMine;
    }

    public String getChainCode() {
        return chainCode;
    }

    public void setChainCode(String chainCode) {
        this.chainCode = chainCode;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public UUID getSystemOrganizationId() {
        return systemOrganizationId;
    }

    public void setSystemOrganizationId(UUID systemOrganizationId) {
        this.systemOrganizationId = systemOrganizationId;
    }
}
