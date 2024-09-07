package backend.trade.order.dto;

import backend.trade.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateDto {
    private Long userId;
    private String orderId;
    private OrderStatus newStatus;
}