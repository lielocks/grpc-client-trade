package backend.trade.order.controller;

import backend.trade.order.dto.OrderDeleteRequestDto;
import backend.trade.order.dto.OrderPageRequestDto;
import backend.trade.order.dto.OrderRegisterRequestDto;
import backend.trade.order.dto.OrderStatusUpdateDto;
import backend.trade.common.grpc.AuthClientService;
import backend.trade.order.model.Order;
import backend.trade.order.service.OrderService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "인증 서버에 토큰 전달 후 검증",
            description = "gRPC 인증 서버에 header 의 token 이 전달되고 정상적으로 검증이 되면 user id 를 전달받는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰이 유효합니다.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 엑세스 토큰입니다.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Authorization 헤더가 잘못되었습니다.", content = @Content(mediaType = "application/json"))
    })
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
    @Operation(summary = "주문 등록",
            description = "헤더의 토큰을 통해 사용자를 인증한 후 주문을 등록하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문이 성공적으로 등록되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1001", description = "유효하지 않은 사용자입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1000", description = "유효하지 않은 액세스 토큰입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5003", description = "지원되지 않는 요청 방법입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5000", description = "알 수 없는 문제가 발생했습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Order> createOrder(@RequestHeader("Authorization") String token, @RequestBody OrderRegisterRequestDto orderRequest) {
        Order order = orderService.createOrder(token, orderRequest);
        return ResponseEntity.ok(order);
    }


    @PatchMapping("/update")
    @Operation(summary = "주문 상태 업데이트",
            description = "헤더의 토큰을 통해 사용자를 인증한 후 주문 상태를 업데이트하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 업데이트되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1001", description = "유효하지 않은 사용자입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1000", description = "유효하지 않은 액세스 토큰입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "2002", description = "해당 주문의 상태를 다시 확인해주세요.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "2003", description = "판매 타입에 해당하지 않는 주문 상태입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "2004", description = "구매 타입에 해당하지 않는 주문 상태입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5003", description = "지원되지 않는 요청 방법입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5000", description = "알 수 없는 문제가 발생했습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Order> updateOrderStatus(@RequestHeader("Authorization") String token, @RequestBody OrderStatusUpdateDto updateRequest) {
        Order updatedOrder = orderService.updateOrderStatus(token, updateRequest);
        return ResponseEntity.ok(updatedOrder);
    }


    @GetMapping("/list")
    @Operation(summary = "주문 리스트",
            description = "헤더의 토큰을 통해 사용자를 인증한 후 원하는 주문 정보를 확인하고 limit 과 offset 지정 후 해당하는 페이지 리스트의 정보를 확인하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 리스트를 성공적으로 불러왔습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1001", description = "유효하지 않은 사용자입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1000", description = "유효하지 않은 액세스 토큰입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5003", description = "지원되지 않는 요청 방법입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5000", description = "알 수 없는 문제가 발생했습니다.",
                    content = @Content(mediaType = "application/json"))
    })
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

    @DeleteMapping("/delete")
    @Operation(summary = "주문 삭제",
            description = "헤더의 토큰을 통해 사용자를 인증한 후 자신의 주문 내역을 삭제하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문이 성공적으로 삭제되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1001", description = "유효하지 않은 사용자입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "1000", description = "유효하지 않은 액세스 토큰입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "2005", description = "해당 주문을 삭제하실 수 있는 권한이 없습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5003", description = "지원되지 않는 요청 방법입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "5000", description = "알 수 없는 문제가 발생했습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> updateOrderStatus(@RequestHeader("Authorization") String token,
                                               @RequestBody OrderDeleteRequestDto deleteRequestDto) {
        orderService.deleteOrder(token, deleteRequestDto);
        return ResponseEntity.ok("ok");
    }
}
