package shinstyle.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import shinstyle.kafka.model.OrderEvent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class OrderProducerTest {

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private Consumer<String, OrderEvent> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "localhost:9092",
                "test-group-1",
                "true"
        );

        consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderEvent.class)
        ).createConsumer();

        consumer.subscribe(List.of("orders")); // 테스트용 토픽
        consumer.poll(Duration.ofMillis(100)); // 초기 데이터를 읽지 않도록 빠르게 폴링
        consumer.seekToBeginning(consumer.assignment()); // 오프셋 초기화
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void testSendOrder() {
        // Given
        OrderEvent order = createTestOrder();

        // When
        orderProducer.sendOrder(order);

        // Then
         ConsumerRecord<String, OrderEvent> record = KafkaTestUtils.getSingleRecord(consumer, "orders");
//        ConsumerRecords<String, OrderEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5), 1);
//        records.records("orders").forEach(a -> log.info("record: {}", a));


        assertThat(record).isNotNull();
        assertThat(record.value().getOrderId()).isEqualTo(order.getOrderId());
    }

    private OrderEvent createTestOrder() {
        List<OrderEvent.OrderItem> items = List.of(new OrderEvent.OrderItem("prod-1", 2, BigDecimal.valueOf(20.00)));
        return new OrderEvent("order-123", "cust-456", items, BigDecimal.valueOf(40.00), LocalDateTime.now());
    }
}