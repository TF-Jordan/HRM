package yowyob.comops.api.blockchain.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BlockchainLedgerProperties.class)
public class BlockchainCoreConfiguration {
}
