package shinstyle.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import shinstyle.kafka.model.OrderEvent;

import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC = "orders";

    public void sendOrder(OrderEvent order) {
        kafkaTemplate.send(TOPIC, order.getOrderId(), order)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        log.error("Failed to send message: {}", order.getOrderId(), e);
                    } else {
                        log.info("Message sent successfully: {}, partition: {}",
                                order.getOrderId(), result.getRecordMetadata().partition());
                    }
                });
    }

    public void sendOrderSync(OrderEvent order) throws ExecutionException, InterruptedException {
        try {
            SendResult<String, OrderEvent> result = kafkaTemplate.send(TOPIC, order.getOrderId(), order).get();
            log.info("Message sent successfully: {}, partition: {}",
                    order.getOrderId(), result.getRecordMetadata().partition());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error sending message synchronously", e);
            throw e;
        }
    }
}
