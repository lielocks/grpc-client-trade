package backend.trade.common.exception;

import org.springframework.http.HttpStatus;

public enum CustomError {

    // 인증
    INVALID_ACCESS_TOKEN(1000, "유효하지 않은 액세스 토큰입니다.", HttpStatus.BAD_REQUEST.value()),
    USER_NOT_AUTHENTICATED(1001, "유효하지 않은 사용자입니다.", HttpStatus.BAD_REQUEST.value()),

    // 주문
    ORDER_NOT_FOUND(2001, "해당 주문은 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),
    STATUS_NOT_AVAILABLE(2002, "해당 주문의 상태를 다시 확인해주세요.", HttpStatus.BAD_REQUEST.value()),
    STATUS_NOT_FOR_SELL(2003, "판매 타입에 해당하지 않는 주문 상태입니다.", HttpStatus.BAD_REQUEST.value()),
    STATUS_NOT_FOR_PURCHASE(2004, "구매 타입에 해당하지 않는 주문 상태입니다.", HttpStatus.BAD_REQUEST.value()),
    FORBIDDEN_ORDER(2005, "해당 주문에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),

    // 공통
    SERVER_ERROR(5000, "알수 없는 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    INVALID_FIELD_TYPE(5001, "필드 타입이 잘못되었습니다.", HttpStatus.BAD_REQUEST.value()),
    ILLEGAL_STATE(5002, "잘못된 상태입니다.", HttpStatus.BAD_REQUEST.value()),
    METHOD_NOT_ALLOWED(5003, "지원되지 않는 요청 방법입니다.", HttpStatus.METHOD_NOT_ALLOWED.value());

    private int errorCode;
    private String message;
    private int statusCode;

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }


    CustomError(int errorCode, String message, int statusCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.statusCode = statusCode;
    }

    CustomError(int errorCode, String message, HttpStatus status) {
        this.errorCode = errorCode;
        this.message = message;
        this.statusCode = status.value();
    }
}
