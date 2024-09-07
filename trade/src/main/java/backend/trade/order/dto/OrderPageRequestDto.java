package backend.trade.order.dto;

import backend.trade.order.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPageRequestDto {
    private Long userId;
    private String dateString;
    private int limit;
    private int offset;
    private Invoice invoice;
}
