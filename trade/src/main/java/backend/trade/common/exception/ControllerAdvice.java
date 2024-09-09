package backend.trade.common.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.hibernate.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();

        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[")
                    .append(fieldError.getField())
                    .append("](은)는 ");
            if (fieldError.getCode() != null && fieldError.getCode().contains("type")) {
                builder.append("타입이 잘못 되었습니다.");
            } else {
                builder.append(fieldError.getDefaultMessage());
            }
            builder.append(" 입력된 값: [")
                    .append(fieldError.getRejectedValue())
                    .append("]");
        }

        CustomError error = CustomError.INVALID_FIELD_TYPE;
        return new ResponseEntity<>(ErrorDto.createErrorDto(error),
                HttpStatus.valueOf(error.getStatusCode()));
    }

    @ExceptionHandler({TypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> handleTypeException(Exception e) {
        log.error("error !", e);
        CustomError error = CustomError.INVALID_FIELD_TYPE;
        return new ResponseEntity<>(ErrorDto.createErrorDto(error),
                HttpStatus.valueOf(error.getStatusCode()));
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<?> handleIllegalStateException(RuntimeException e) {
        log.error("error !", e);
        CustomError error = CustomError.ILLEGAL_STATE;
        return new ResponseEntity<>(ErrorDto.createErrorDto(error),
                HttpStatus.valueOf(error.getStatusCode()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.error("error !", e);
        CustomError error = CustomError.METHOD_NOT_ALLOWED;
        return new ResponseEntity<>(ErrorDto.createErrorDto(error),
                HttpStatus.valueOf(error.getStatusCode()));
    }

    // JwtException 처리
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwtException(JwtException e) {
        log.error("error !", e);
        CustomError error = CustomError.INVALID_ACCESS_TOKEN;
        return new ResponseEntity<>(ErrorDto.createErrorDto(error),
                HttpStatus.valueOf(error.getStatusCode()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customException(CustomException e) {
        return new ResponseEntity<>(ErrorDto.createErrorDto(e.getCustomError()),
                HttpStatus.valueOf(e.getCustomError().getStatusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> commonException(Exception e) {
        log.error("error !", e);
        CustomError serverError = CustomError.SERVER_ERROR;
        return new ResponseEntity<>(ErrorDto.createErrorDto(serverError),
                HttpStatus.valueOf(serverError.getStatusCode()));
    }
}
