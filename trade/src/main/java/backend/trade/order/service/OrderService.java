package backend.trade.order.service;

import backend.trade.common.exception.CustomError;
import backend.trade.common.exception.CustomException;
import backend.trade.order.dto.OrderRegisterRequestDto;
import backend.trade.order.dto.OrderStatusUpdateDto;
import backend.trade.common.grpc.AuthClientService;
import backend.trade.order.model.Invoice;
import backend.trade.order.model.Order;
import backend.trade.order.model.OrderStatus;
import backend.trade.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuthClientService authClientService;

    private String generateOrderNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "ORDER-" + LocalDateTime.now().format(formatter);
    }

    /**
     * grcp 서버에서 token claim id 가져오기 및 요청한 user 권한에 맞는 invoice 인지 검증
     * @param authorizationHeader
     * @param requestUserId
     * @return userId
     */
    public Long extractAndVerifyToken(String authorizationHeader, Long requestUserId) {
        String token = authorizationHeader.substring(7);
        Long userId = authClientService.getUserIdFromToken(token);
        if (!authClientService.verifyToken(token) || !userId.equals(requestUserId)) {
            throw new CustomException(CustomError.USER_NOT_AUTHENTICATED);
        }
        return userId;
    }

    public LocalDateTime parseDateString(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    @Transactional
    public Order createOrder(String token, OrderRegisterRequestDto orderRequest) {
        Long userId = extractAndVerifyToken(token, orderRequest.getUserId());
        String orderNumber = generateOrderNumber();

        double roundedQuantity = BigDecimal.valueOf(orderRequest.getQuantity())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        Order order = Order.builder()
                .id(orderNumber)
                .userId(userId)
                .invoice(orderRequest.getInvoice())
                .orderDate(LocalDateTime.now())
                .itemType(orderRequest.getItemType())
                .quantity(roundedQuantity)
                .shippingAddress(orderRequest.getShippingAddress())
                .status(OrderStatus.ORDER_COMPLETED)
                .build();

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(String token, OrderStatusUpdateDto updateRequest) {
        extractAndVerifyToken(token, updateRequest.getUserId());

        Order order = orderRepository.findById(updateRequest.getOrderId())
                        .orElseThrow(() -> new CustomException(CustomError.ORDER_NOT_FOUND));

        validateOrderStatusTransition(order.getInvoice(), order.getStatus(), updateRequest.getNewStatus());
        order.setStatus(updateRequest.getNewStatus());

        return orderRepository.save(order);
    }

    /**
     * 주문 상태 전환 검증
     * PURCHASE : ORDER_COMPLETED 주문 완료 -> PAYMENT_COMPLETED 입금 완료 -> SHIPPED 발송 완료
     * SELL : ORDER_COMPLETED 주문 완료 -> PAYMENT_RECEIVED 송금 완료 -> RECEIVED 수령 완료
     * @param invoice
     * @param currentStatus
     * @param newStatus
     */
    private void validateOrderStatusTransition(Invoice invoice, OrderStatus currentStatus, OrderStatus newStatus) {
        if (invoice == Invoice.PURCHASE) {
            switch (currentStatus) {
                case ORDER_COMPLETED:
                    if (newStatus == OrderStatus.PAYMENT_COMPLETED) return;
                    break;
                case PAYMENT_COMPLETED:
                    if (newStatus == OrderStatus.SHIPPED) return;
                    break;
                default:
                    throw new CustomException(CustomError.STATUS_NOT_FOR_PURCHASE);
            }
        } else if (invoice == Invoice.SELL) {
            switch (currentStatus) {
                case ORDER_COMPLETED:
                    if (newStatus == OrderStatus.PAYMENT_RECEIVED) return;
                    break;
                case PAYMENT_RECEIVED:
                    if (newStatus == OrderStatus.RECEIVED) return;
                    break;
                default:
                    throw new CustomException(CustomError.STATUS_NOT_FOR_SELL);
            }
        }

        throw new CustomException(CustomError.STATUS_NOT_AVAILABLE);
    }


    @Transactional(readOnly = true)
    public Order getPagedOrders(String token, LocalDateTime date, Invoice invoice) {
        Order order = orderRepository.findByOrderDateAndInvoice(date, invoice)
                        .orElseThrow(() -> new CustomException(CustomError.ORDER_NOT_FOUND));
        extractAndVerifyToken(token, order.getUserId());
        return order;
    }

    public Map<String, Object> buildPaginationLinks(Page<Order> orderPage, LocalDateTime date, int limit, int offset, Invoice invoice) {
        Map<String, Object> links = new HashMap<>();
        String baseUrl = "/api/orders?";

        links.put("self", Map.of("href", buildUrl(baseUrl, date, limit, offset, invoice)));
        links.put("first", Map.of("href", buildUrl(baseUrl, date, limit, 0, invoice)));
        links.put("last", Map.of("href", buildUrl(baseUrl, date, limit, orderPage.getTotalPages() - 1, invoice)));
        links.put("previous", offset > 0 ? Map.of("href", buildUrl(baseUrl, date, limit, offset - 1, invoice)) : null);
        links.put("next", offset + 1 < orderPage.getTotalPages() ? Map.of("href", buildUrl(baseUrl, date, limit, offset + 1, invoice)) : null);

        return links;
    }

    private String buildUrl(String baseUrl, LocalDateTime date, int limit, int offset, Invoice invoice) {
        return baseUrl + "date=" + date + "&limit=" + limit + "&offset=" + offset + "&invoice=" + invoice;
    }

    public Page<Order> countPage(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return orderRepository.findAll(pageable);
    }
}
