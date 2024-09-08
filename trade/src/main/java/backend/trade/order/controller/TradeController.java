package backend.trade.order.controller;

import backend.trade.order.dto.OrderPageRequestDto;
import backend.trade.order.dto.OrderRegisterRequestDto;
import backend.trade.order.dto.OrderStatusUpdateDto;
import backend.trade.common.grpc.AuthClientService;
import backend.trade.order.model.Order;
import backend.trade.order.service.OrderService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class TradeController {

    private final AuthClientService authClientService;
    private final OrderService orderService;

    /**
     * gRPC 를 타고 token 정상적으로 검증되는지 확인
     * @param authorizationHeader
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyToken(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        try {
            if (authClientService.verifyToken(token)) {
                Long userId = authClientService.getUserIdFromToken(token);
                return new ResponseEntity<>("Token is VALID. User ID: " + userId, HttpStatus.OK);
            } else {
                log.warn("Token is INVALID OR EXPIRED");
                return new ResponseEntity<>("Token is INVALID OR EXPIRED", HttpStatus.UNAUTHORIZED);
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Verify 중 에러 :: {}", e.getMessage());
            return new ResponseEntity<>("INVALID TOKEN :: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Order> createOrder(@RequestHeader("Authorization") String token, @RequestBody OrderRegisterRequestDto orderRequest) {
        Order order = orderService.createOrder(token, orderRequest);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/update")
    public ResponseEntity<Order> updateOrderStatus(@RequestHeader("Authorization") String token, @RequestBody OrderStatusUpdateDto updateRequest) {
        Order updatedOrder = orderService.updateOrderStatus(token, updateRequest);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPagedOrders(@RequestHeader("Authorization") String token, @RequestBody OrderPageRequestDto requestDto) {
        LocalDateTime date = orderService.parseDateString(requestDto.getDateString());

        Page<Order> orderPage = orderService.countPage(requestDto.getOffset(), requestDto.getLimit());
        Order pagedOrder = orderService.getPagedOrders(token, date, requestDto.getInvoice());

        Map<String, Object> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Success to search invoices");
        response.put("data", pagedOrder);
        response.put("links", orderService.buildPaginationLinks(orderPage, date, requestDto.getLimit(), requestDto.getOffset(), requestDto.getInvoice()));

        return ResponseEntity.ok(response);
    }
}
