package backend.trade.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Table(name = "order_table")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    private String id;

    private LocalDateTime orderDate;

    private Long userId;  // JWT 토큰 검증 후 추출한 사용자 ID

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // Enum: ORDER_COMPLETED, PAYMENT_COMPLETED, SHIPPED for purchase; ORDER_COMPLETED, PAYMENT_RECEIVED, RECEIVED for sale

    @Enumerated(EnumType.STRING)
    private ItemType itemType; // Enum: GOLD_999, GOLD_9999

    @Enumerated(EnumType.STRING)
    private Invoice invoice; // Enum: SELL, PURCHASE

    private Double quantity; // 단위는 gram 소수점 두자리, 단위 표시 단위 g 를 붙이는게 필요시 프론트단에서 처리

    private String shippingAddress;

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

