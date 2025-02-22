package shinstyle.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderEvent {

    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private LocalDateTime orderTime;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class OrderItem {
        private String productId;
        private int quantity;
        private BigDecimal price;
    }
}
