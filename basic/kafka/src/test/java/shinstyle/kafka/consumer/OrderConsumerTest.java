package shinstyle.kafka.consumer;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import shinstyle.kafka.model.OrderEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderConsumerTest {

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @SpyBean
    private OrderConsumer orderConsumer;

    @Test
    void testOrderProcessing() {
        // Given
        OrderEvent order = createTestOrder();

        // When
        kafkaTemplate.send("orders", order.getOrderId(), order);

        // Then
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 조건이 충족될 때까지 대기
                    BDDMockito.verify(orderConsumer, BDDMockito.times(1)).processOrder(order);
                });
    }

    private OrderEvent createTestOrder() {
        List<OrderEvent.OrderItem> items = List.of(new OrderEvent.OrderItem("prod-1", 2, BigDecimal.valueOf(20.00)));
        return new OrderEvent("order-123", "cust-456", items, BigDecimal.valueOf(40.00), LocalDateTime.now());
    }
}