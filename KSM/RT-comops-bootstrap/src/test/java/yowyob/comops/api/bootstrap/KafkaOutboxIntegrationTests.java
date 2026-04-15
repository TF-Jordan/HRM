package yowyob.comops.api.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.kernel.application.port.in.RelayOutboxEventsUseCase;
import yowyob.comops.api.kernel.application.port.out.DomainEventProjectionRepository;
import yowyob.comops.api.kernel.application.port.out.OutboxEventRepository;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.DomainEventProjection;
import yowyob.comops.api.kernel.domain.model.OutboxEvent;
import yowyob.comops.api.kernel.domain.model.OutboxEventStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {                "iwm.outbox.delivery.type=kafka",
                "iwm.outbox.consumers.mode=kafka",
                "iwm.outbox.relay.enabled=false",
                "iwm.outbox.delivery.kafka.create-per-aggregate-topic=false",
                "iwm.outbox.delivery.kafka.topic-prefix=iwm.events.business",
                "iwm.outbox.consumers.kafka.topics=iwm.events.business",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.properties.metadata.max.age.ms=1000"
        })
@AutoConfigureWebTestClient
@ActiveProfiles("test-memory")
@EmbeddedKafka(partitions = 1,
        topics = {"iwm.events.business", "iwm.events.dead-letter"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@EnabledIfSystemProperty(named = "iwm.tests.kafka.enabled", matches = "true")
class KafkaOutboxIntegrationTests {

    private static final UUID TENANT_ID = UUID.fromString("24000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_ID = UUID.fromString("24000000-0000-0000-0000-000000000010");

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RelayOutboxEventsUseCase relayOutboxEventsUseCase;

    @Autowired
    private DomainEventProjectionRepository domainEventProjectionRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void relayPublishesToKafkaAndConsumersPersistDomainProjections() {
        OutboxEvent salesEvent = OutboxEvent.create(BusinessEvent.now(
                TENANT_ID,
                ORGANIZATION_ID,
                "SALES_ORDER_CONFIRMED",
                "SALES_ORDER",
                UUID.fromString("24000000-0000-0000-0000-000000000100"),
                Map.of("orderNumber", "SO-KAFKA-0001", "status", "CONFIRMED")));
        OutboxEvent inventoryEvent = OutboxEvent.create(BusinessEvent.now(
                TENANT_ID,
                ORGANIZATION_ID,
                "STOCK_MOVEMENT_RECORDED",
                "STOCK_MOVEMENT",
                UUID.fromString("24000000-0000-0000-0000-000000000101"),
                Map.of("referenceNumber", "MOV-KAFKA-0001", "status", "OUTBOUND", "movementType", "OUTBOUND")));

        outboxEventRepository.save(salesEvent).block();
        outboxEventRepository.save(inventoryEvent).block();

        Integer relayedCount = relayOutboxEventsUseCase.relayBatch(10)
                .blockOptional()
                .orElse(0);

        assertThat(relayedCount).isEqualTo(2);

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    List<ConsumerRecord<String, String>> rawRecords = consumeRawTopicRecords();
                    assertThat(rawRecords)
                            .hasSize(2)
                            .extracting(ConsumerRecord::topic)
                            .containsOnly("iwm.events.business");
                    assertThat(rawRecords)
                            .extracting(record -> record.headers().lastHeader("iwm-event-type").value())
                            .isNotEmpty();
                });

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    List<ConsumerRecord<String, String>> deadLetterRecords = consumeDeadLetterRecords();
                    assertThat(deadLetterRecords)
                            .withFailMessage("An event was dead-lettered instead of being projected: %s", deadLetterRecords)
                            .isEmpty();
                });

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    List<DomainEventProjection> projections = domainEventProjectionRepository.findByTenantId(TENANT_ID)
                            .collectList()
                            .blockOptional()
                            .orElse(List.of());
                    assertThat(projections)
                            .extracting(DomainEventProjection::domainType)
                            .contains("SALES", "INVENTORY");
                });

        List<OutboxEvent> outboxEvents = outboxEventRepository.findByTenantId(TENANT_ID)
                .collectList()
                .blockOptional()
                .orElse(List.of());
        assertThat(outboxEvents)
                .hasSize(2)
                .allMatch(event -> event.status() == OutboxEventStatus.PUBLISHED)
                .allMatch(event -> event.publishedAt() != null);
    }

    private List<ConsumerRecord<String, String>> consumeRawTopicRecords() {
        return consumeRecords("iwm.events.business");
    }

    private List<ConsumerRecord<String, String>> consumeDeadLetterRecords() {
        return consumeRecords("iwm.events.dead-letter");
    }

    private List<ConsumerRecord<String, String>> consumeRecords(String... topics) {
        Map<String, Object> properties = KafkaTestUtils.consumerProps(
                "iwm-kafka-contract-reader-" + UUID.randomUUID(), "false", embeddedKafkaBroker);
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);
        try (Consumer<String, String> consumer =
                new DefaultKafkaConsumerFactory<String, String>(properties).createConsumer()) {
            embeddedKafkaBroker.consumeFromEmbeddedTopics(consumer, false, topics);
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
            List<ConsumerRecord<String, String>> collected = new ArrayList<>();
            for (String topic : topics) {
                records.records(topic).forEach(collected::add);
            }
            return collected;
        }
    }
}
