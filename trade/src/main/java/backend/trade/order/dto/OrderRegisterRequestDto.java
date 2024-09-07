package backend.trade.order.dto;

import backend.trade.order.model.Invoice;
import backend.trade.order.model.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRegisterRequestDto {
    private Long userId;
    private Invoice invoice;
    private ItemType itemType;
    private double quantity;
    private String shippingAddress;
}
