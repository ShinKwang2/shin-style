package shinstyle.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import shinstyle.kafka.model.OrderEvent;

@Slf4j
@Service
public class DeadLetterConsumer {

    @KafkaListener(topics = "orders.DLT", groupId = "dlt-group")
    public void listenDLT(@Payload OrderEvent order, Exception e) {
        log.error("Received failed order in DLT: {}, Error: {}",
                order.getOrderId(), e.getMessage());

    }

}
