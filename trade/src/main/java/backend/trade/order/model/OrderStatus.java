package backend.trade.order.model;

/**
 * 구매 주문 : ORDER_COMPLETED 주문 완료 -> PAYMENT_COMPLETED 입금 완료 -> SHIPPED 발송 완료
 * 판매 주문 : ORDER_COMPLETED 주문 완료 -> PAYMENT_RECEIVED 송금 완료 -> RECEIVED 수령 완료
 */
public enum OrderStatus {
    ORDER_COMPLETED,    // 주문 완료
    PAYMENT_COMPLETED,  // 입금 완료 (구매)
    PAYMENT_RECEIVED,   // 송금 완료 (판매)
    SHIPPED,            // 발송 완료 (구매)
    RECEIVED            // 수령 완료 (판매)
}
