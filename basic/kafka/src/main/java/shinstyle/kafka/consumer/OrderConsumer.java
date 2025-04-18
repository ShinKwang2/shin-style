package shinstyle.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import shinstyle.kafka.model.OrderEvent;

@Slf4j
@Service
public class OrderConsumer {

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void listen(@Payload OrderEvent order,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("Received order: {}, partition: {}, offset: {}",
                    order.getOrderId(), partition, offset);
            processOrder(order);
        } catch (Exception e) {
            log.error("Error processing order: {}", order.getOrderId(), e);
            handleError(order, e);
        }
    }

    protected void processOrder(OrderEvent order) {
        // 주문 처리 로직
        log.info("Processing order: {}", order.getOrderId());
    }

    protected void handleError(OrderEvent order, Exception e) {
        // 에러 처리 로직
    }
}
